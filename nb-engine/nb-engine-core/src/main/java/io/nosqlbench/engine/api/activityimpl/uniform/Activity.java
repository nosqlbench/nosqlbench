/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.engine.api.activityimpl.uniform;

import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpLookup;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.core.progress.ActivityMetricProgressMeter;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityapi.core.progress.StateCapable;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityapi.simrate.*;
import io.nosqlbench.engine.api.activityimpl.Dryrun;
import io.nosqlbench.engine.api.activityimpl.OpFunctionComposition;
import io.nosqlbench.engine.api.activityimpl.OpLookupService;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;
import io.nosqlbench.nb.api.advisor.NBAdvisorOutput;
import io.nosqlbench.nb.api.components.status.NBStatusComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricCounter;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricHistogram;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;
import io.nosqlbench.nb.api.lifecycle.Shutdownable;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.components.events.NBEvent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.tagging.TagFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.LongFunction;

/**
 This is a typed activity which is expected to become the standard
 core of all new activity types. Extant NB drivers should also migrate
 to this when possible.
 @param <R>
 A type of runnable which wraps the operations for this type of driver.
 @param <S>
 The context type for the activity, AKA the 'space' for a named driver instance and its
 associated object graph */
public class Activity<R extends java.util.function.LongFunction, S>
    extends NBStatusComponent implements InvokableResult, SyntheticOpTemplateProvider, ActivityDefObserver, StateCapable,
    ProgressCapable, Comparable<Activity> {
    private static final Logger logger = LogManager.getLogger("ACTIVITY");
    private final OpSequence<OpDispenser<? extends CycleOp<?>>> sequence;
    private final ConcurrentHashMap<String, DriverAdapter<CycleOp<?>, Space>> adapters = new ConcurrentHashMap<>();
    protected final ActivityDef activityDef;

    public final NBMetricCounter pendingOpsCounter;
    public final NBMetricHistogram triesHistogram;
    public final NBMetricTimer bindTimer;
    public final NBMetricTimer executeTimer;
    public final NBMetricTimer resultTimer;
    public final NBMetricTimer resultSuccessTimer;
    public final NBMetricTimer cycleServiceTimer;
    public final NBMetricTimer inputTimer;
    public final NBMetricTimer stridesServiceTimer;
    public final NBMetricTimer stridesResponseTimer;
    public final NBMetricTimer cycleResponseTimer;

    private ActivityMetricProgressMeter progressMeter;
    private String workloadSource = "unspecified";
    private RunState runState = RunState.Uninitialized;
    private long startedAtMillis;
    private final RunStateTally tally = new RunStateTally();
    private ThreadLocal<RateLimiter> strideLimiterSource;
    private ThreadLocal<RateLimiter> cycleLimiterSource;
    private int nameEnumerator;
    private NBErrorHandler errorHandler;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private PrintWriter console;
    private ErrorMetrics errorMetrics;
    private ActivityWiring wiring;

    private static final String WAIT_TIME = "_waittime";
    private static final String RESPONSE_TIME = "_responsetime";
    private static final String SERVICE_TIME = "_servicetime";

    public Activity(NBComponent parent, ActivityDef activityDef, ActivityWiring wiring) {

        super(parent, NBLabels.forKV("activity", activityDef.getAlias()).and(activityDef.auxLabels()));
        this.activityDef = activityDef;
        this.wiring = wiring;

        this.pendingOpsCounter = create().counter(
            "pending_ops",
            MetricCategory.Core,
            "Indicate the number of operations which have been started, but which have not been completed." +
                " This starts "
        );

        /// The bind timer keeps track of how long it takes for NoSQLBench to create an instance
        /// of an executable operation, given the cycle. This is usually done by using an
        /// {@link OpSequence} in conjunction with
        /// an {@link OpDispenser}. This is named for "binding
        /// a cycle to an operation".
        int hdrdigits = getHdrDigits();
        this.bindTimer = create().timer(
            "bind", hdrdigits, MetricCategory.Core,
            "Time the step within a cycle which binds generated data to an op template to synthesize an executable operation."
        );

        /// The execute timer keeps track of how long it takes to submit an operation to be executed
        /// to an underlying native driver. For asynchronous APIs, such as those which return a
        /// {@link Future}, this is simply the amount of time it takes to acquire the future.
        ///     /// When possible, APIs should be used via their async methods, even if you are implementing
        /// a {@link SyncAction}. This allows the execute timer to measure the hand-off to the underlying API,
        /// and the result timer to measure the blocking calls to aquire the result.
        this.executeTimer = create().timer(
            "execute",
            hdrdigits,
            MetricCategory.Core,
            "Time how long it takes to submit a request and receive a result, including reading the result in the client."
        );

        /// The cycles service timer measures how long it takes to complete a cycle of work.
        this.cycleServiceTimer = create().timer(
            "cycles" + SERVICE_TIME, hdrdigits, MetricCategory.Core,
            "service timer for a cycle, including all of bind, execute, result and result_success;" + " service timers measure the time between submitting a request and receiving the response"
        );


        /// The result timer keeps track of how long it takes a native driver to service a request once submitted.
        /// This timer, in contrast to the result-success timer ({@link #getOrCreateResultSuccessTimer()}),
        /// is used to track all operations. That is, no matter
        /// whether the operation succeeds or not, it should be tracked with this timer. The scope of this timer should
        /// cover each attempt at an operation through a native driver. Retries are not to be combined in this measurement.
        this.resultTimer = create().timer(
            "result",
            hdrdigits,
            MetricCategory.Core,
            "Time how long it takes to submit a request, receive a result, including binding, reading results, " +
                "and optionally verifying them, including all operations whether successful or not, for each attempted request."
        );

        /// The result-success timer keeps track of operations which had no exception. The measurements for this timer should
        /// be exactly the same values as used for the result timer ({@link #getOrCreateResultTimer()}, except that
        /// attempts to complete an operation which yield an exception should be excluded from the results. These two metrics
        /// together provide a very high level sanity check against the error-specific metrics which can be reported by
        /// the error handler logic.
        this.resultSuccessTimer = create().timer(
            "result_success",
            hdrdigits,
            MetricCategory.Core,
            "The execution time of successful operations, which includes submitting the operation, waiting for a response, and reading the result"
        );

        /// The input timer measures how long it takes to get the cycle value to be used for
        /// an operation.
        this.inputTimer = create().timer(
            "read_input", getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3),
            MetricCategory.Internals,
            "measures overhead of acquiring a cycle range for an activity thread"
        );

        /// The strides service timer measures how long it takes to complete a stride of work.
        this.stridesServiceTimer = create().timer(
            "strides", getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3),
            MetricCategory.Core,
            "service timer for a stride, which is the same as the op sequence length by default"
        );

        if (null != getStrideLimiter()) {

            ///  The strides response timer measures the total response time from the scheduled
            ///  time a stride should start to when it completed. Stride scheduling is only defined
            ///  when it is implied by a stride rate limiter, so this method should return null if
            ///  there is no strides rate limiter.
            this.stridesResponseTimer = create().timer(
                "strides" + RESPONSE_TIME, hdrdigits, MetricCategory.Core,
                "response timer for a stride, which is the same as the op sequence length by default;" + " response timers include scheduling delays which occur when an activity falls behind its target rate"
            );
        } else {
            stridesResponseTimer=null;
        }


        /**
         * The cycles response timer measures the total response time from the scheduled
         * time an operation should start to when it is completed. Cycle scheduling is only defined
         * when it is implied by a cycle rate limiter, so this method should return null if
         * there is no cycles rate limiter.
         * @return a new or existing {@link Timer} if appropriate, else null
         */
        if (null != getCycleLimiter()) {
            this.cycleResponseTimer = create().timer(
                "cycles" + RESPONSE_TIME, hdrdigits, MetricCategory.Core,
                "response timer for a cycle, including all of bind, execute, result and result_success;" + " response timers include scheduling delays which occur when an activity falls behind its target rate"
            );
        } else {
            cycleResponseTimer=null;
        }



        if (activityDef.getAlias().equals(ActivityDef.DEFAULT_ALIAS)) {
            Optional<String> workloadOpt = activityDef.getParams().getOptionalString(
                "workload",
                "yaml"
            );
            if (workloadOpt.isPresent()) {
                activityDef.getParams().set("alias", workloadOpt.get());
            } else {
                activityDef.getParams().set("alias",
                                            activityDef.getActivityDriver().toUpperCase(Locale.ROOT)
                                                + nameEnumerator);
                nameEnumerator++;
            }
        }

        OpsDocList workload;

        Optional<String> yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
        NBConfigModel yamlmodel;
        if (yaml_loc.isPresent()) {
            Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
            workload = OpsLoader.loadPath(yaml_loc.get(), disposable, "activities");
            yamlmodel = workload.getConfigModel();
        } else {
            yamlmodel = ConfigModel.of(Activity.class).asReadOnly();
        }

        Optional<String> defaultDriverName = activityDef.getParams().getOptionalString("driver");

        Optional<DriverAdapter<?, ?>> defaultAdapter = defaultDriverName.flatMap(
            name -> ServiceSelector.of(
                name, ServiceLoader.load(DriverAdapterLoader.class)).get()).map(
            l -> l.load(this, NBLabels.forKV()));

        if (defaultDriverName.isPresent() && defaultAdapter.isEmpty()) {
            throw new BasicError(
                "Unable to load '" + defaultDriverName.get() + "' driver adapter.\n" + "Rebuild NB5 to include this driver adapter. " + "Change '<activeByDefault>false</activeByDefault>' for the driver in " + "'./nb-adapters/pom.xml' and './nb-adapters/nb-adapters-included/pom.xml' first.");
        }

        // HERE, op templates are loaded before drivers are loaded
//        List<OpTemplate> opTemplates = loadOpTemplates(defaultAdapter.orElse(null), false);
        List<DriverAdapter<CycleOp<?>, Space>> adapterlist = new ArrayList<>();
        NBConfigModel supersetConfig = ConfigModel.of(Activity.class).add(yamlmodel);
        Optional<String> defaultDriverOption = defaultDriverName;
        ConcurrentHashMap<String, OpMapper<? extends CycleOp<?>, ? extends Space>> mappers = new ConcurrentHashMap<>();

        List<ParsedOp> allParsedOps = loadOpTemplates(
            defaultAdapter.orElse(null), false, false).stream().map(ot -> upconvert(
            ot, defaultDriverOption, yamlmodel, supersetConfig, mappers, adapterlist)).toList();

        OpLookup lookup = new OpLookupService(() -> allParsedOps);

        TagFilter ts = new TagFilter(activityDef.getParams().getOptionalString("tags").orElse(""));
        List<ParsedOp> activeParsedOps = ts.filter(allParsedOps);

        if (defaultDriverOption.isPresent()) {
            long matchingDefault = mappers.keySet().stream().filter(
                n -> n.equals(defaultDriverOption.get())).count();
            if (0 == matchingDefault) {
                logger.warn(
                    "All op templates used a different driver than the default '{}'",
                    defaultDriverOption.get()
                );
            }
        }

        try {
            sequence = createOpSourceFromParsedOps(adapterlist, activeParsedOps, lookup);
        } catch (Exception e) {
            if (e instanceof OpConfigError) {
                throw e;
            }
            throw new OpConfigError(
                "Error mapping workload template to operations: " + e.getMessage(), null, e);
        }

        create().gauge(
            "ops_pending", () -> this.getProgressMeter().getSummary().pending(),
            MetricCategory.Core,
            "The current number of operations which have not been dispatched for processing yet."
        );
        create().gauge(
            "ops_active", () -> this.getProgressMeter().getSummary().current(), MetricCategory.Core,
            "The current number of operations which have been dispatched for processing, but which have not yet completed."
        );
        create().gauge(
            "ops_complete", () -> this.getProgressMeter().getSummary().complete(),
            MetricCategory.Core, "The current number of operations which have been completed"
        );

        /// The tries histogram tracks how many tries it takes to complete an operation successfully, or not. This histogram
        /// does not encode whether operations were successful or not. Ideally, if every attempt to complete an operation succeeds
        /// on its first try, the data in this histogram should all be 1. In practice, systems which are running near their
        /// capacity will see a few retried operations, and systems that are substantially over-driven will see many retried
        /// operations. As the retries value increases the further down the percentile scale you go, you can detect system loading
        /// patterns which are in excess of the real-time capability of the target system.
        /// This metric should be measured around every retry loop for a native operation.
        this.triesHistogram = create().histogram(
            "tries",
            hdrdigits,
            MetricCategory.Core,
            "A histogram of all tries for an activity. Perfect results mean all quantiles return 1." +
                " Slight saturation is indicated by p99 or p95 returning higher values." +
                " Lower quantiles returning more than 1, or higher values at high quantiles indicate incremental overload."
        );

    }


    protected <O extends LongFunction> OpSequence<OpDispenser<? extends CycleOp<?>>> createOpSourceFromParsedOps(
        List<DriverAdapter<CycleOp<?>, Space>> adapters, List<ParsedOp> pops, OpLookup opLookup) {
        return createOpSourceFromParsedOps2(adapters, pops, opLookup);
    }

    protected <O extends LongFunction> OpSequence<OpDispenser<? extends CycleOp<?>>> createOpSourceFromParsedOps2(
//        Map<String, DriverAdapter<?,?>> adapterCache,
//        Map<String, OpMapper<? extends Op>> mapperCache,
        List<DriverAdapter<CycleOp<?>, Space>> adapters,
        List<ParsedOp> pops,
        OpLookup opLookup
    ) {
        try {

            List<Long> ratios = new ArrayList<>(pops.size());

            for (ParsedOp pop : pops) {
                long ratio = pop.takeStaticConfigOr("ratio", 1);
                ratios.add(ratio);
            }

            SequencerType sequencerType = getParams()
                .getOptionalString("seq")
                .map(SequencerType::valueOf)
                .orElse(SequencerType.bucket);
            SequencePlanner<OpDispenser<? extends CycleOp<?>>> planner = new SequencePlanner<>(sequencerType);

            for (int i = 0; i < pops.size(); i++) {
                long ratio = ratios.get(i);
                ParsedOp pop = pops.get(i);

                try {
                    if (0 == ratio) {
                        logger.info(() -> "skipped mapping op '" + pop.getName() + '\'');
                        continue;
                    }

                    DriverAdapter<CycleOp<?>, Space> adapter = adapters.get(i);
                    OpMapper<CycleOp<?>, Space> opMapper = adapter.getOpMapper();
                    LongFunction<Space> spaceFunc = adapter.getSpaceFunc(pop);
                    OpDispenser<? extends CycleOp<?>> dispenser = opMapper.apply(this, pop, spaceFunc);
                    String dryrunSpec = pop.takeStaticConfigOr("dryrun", "none");
                    Dryrun dryrun = pop.takeEnumFromFieldOr(Dryrun.class, Dryrun.none, "dryrun");

                    dispenser = OpFunctionComposition.wrapOptionally(
                        adapter,
                        dispenser,
                        pop,
                        dryrun,
                        opLookup
                    );

//                if (strict) {
//                    optemplate.assertConsumed();
//                }
                    planner.addOp((OpDispenser<? extends CycleOp<?>>) dispenser, ratio);
                } catch (Exception e) {
                    throw new OpConfigError("Error while mapping op from template named '" + pop.getName() + "': " + e.getMessage(), e);
                }
            }


            return planner.resolve();

        } catch (Exception e) {
            if (e instanceof OpConfigError oce) {
                throw oce;
            } else {
                throw new OpConfigError(e.getMessage(), workloadSource, e);
            }
        }


    }

    public ParameterMap getParams() {
        return activityDef.getParams();
    }


    private ParsedOp upconvert(
        OpTemplate ot, Optional<String> defaultDriverOption, NBConfigModel yamlmodel,
        NBConfigModel supersetConfig,
        ConcurrentHashMap<String, OpMapper<? extends CycleOp<?>, ? extends Space>> mappers,
        List<DriverAdapter<CycleOp<?>, Space>> adapterlist
    ) {
        //            ParsedOp incompleteOpDef = new ParsedOp(ot, NBConfiguration.empty(), List.of(), this);
        String driverName = ot.getOptionalStringParam("driver", String.class).or(
            () -> ot.getOptionalStringParam("type", String.class)).or(
            () -> defaultDriverOption).orElseThrow(
            () -> new OpConfigError("Unable to identify driver name for op template:\n" + ot));

        DriverAdapter<CycleOp<?>, Space> adapter = adapters.computeIfAbsent(
            driverName, dn -> loadAdapter(
                dn, yamlmodel, supersetConfig, mappers));
        supersetConfig.assertValidConfig(activityDef.getParams().getStringStringMap());
        adapterlist.add(adapter);

        ParsedOp pop = new ParsedOp(
            ot, adapter.getConfiguration(), List.of(adapter.getPreprocessor()), this);
        Optional<String> discard = pop.takeOptionalStaticValue("driver", String.class);

        return pop;
    }

    private DriverAdapter<CycleOp<?>, Space> loadAdapter(
        String driverName, NBConfigModel yamlmodel, NBConfigModel supersetConfig,
        ConcurrentHashMap<String, OpMapper<? extends CycleOp<?>, ? extends Space>> mappers
    ) {
        DriverAdapter<CycleOp<?>, Space> adapter = Optional.of(driverName).flatMap(
            name -> ServiceSelector.of(
                name, ServiceLoader.load(DriverAdapterLoader.class)).get()).map(
            l -> l.load(this, NBLabels.forKV())).orElseThrow(
            () -> new OpConfigError("driver adapter not present for name '" + driverName + "'"));

        NBConfigModel combinedModel = yamlmodel;
        NBConfiguration combinedConfig = combinedModel.matchConfig(activityDef.getParams());

        if (adapter instanceof NBConfigurable configurable) {
            NBConfigModel adapterModel = configurable.getConfigModel();
            supersetConfig.add(adapterModel);

            combinedModel = adapterModel.add(yamlmodel);
            combinedConfig = combinedModel.matchConfig(activityDef.getParams());
            configurable.applyConfig(combinedConfig);
        }
        mappers.put(driverName, adapter.getOpMapper());
        return adapter;
    }


    public void initActivity() {
        initOrUpdateRateLimiters(this.activityDef);
        setDefaultsFromOpSequence(sequence);
    }


    public OpSequence<OpDispenser<? extends CycleOp<?>>> getOpSequence() {
        return sequence;
    }

//    /**
//     * When an adapter needs to identify an error uniquely for the purposes of
//     * routing it to the correct error handler, or naming it in logs, or naming
//     * metrics, override this method in your activity.
//     *
//     * @return A function that can reliably and safely map an instance of Throwable to a stable name.
//     */
//    @Override
//    public final Function<Throwable, String> getErrorNameMapper() {
//        return adapter.getErrorNameMapper();
//    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {

        for (DriverAdapter<?, ?> adapter : adapters.values()) {
            if (adapter instanceof NBReconfigurable configurable) {
                NBConfigModel cfgModel = configurable.getReconfigModel();
                NBConfiguration cfg = cfgModel.matchConfig(activityDef.getParams());
                NBReconfigurable.applyMatching(cfg, List.of(configurable));
            }
        }
    }

//    @Override
//    public synchronized void onActivityDefUpdate(final ActivityDef activityDef) {
//        super.onActivityDefUpdate(activityDef);
//
//        for (final DriverAdapter adapter : this.adapters.values())
//            if (adapter instanceof NBReconfigurable reconfigurable) {
//                NBConfigModel cfgModel = reconfigurable.getReconfigModel();
//                final Optional<String> op_yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
//                if (op_yaml_loc.isPresent()) {
//                    final Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
//                    final OpsDocList workload = OpsLoader.loadPath(op_yaml_loc.get(), disposable, "activities");
//                    cfgModel = cfgModel.add(workload.getConfigModel());
//                }
//                final NBConfiguration cfg = cfgModel.apply(activityDef.getParams());
//                reconfigurable.applyReconfig(cfg);
//            }
//
//    }

    @Override
    public List<OpTemplate> getSyntheticOpTemplates(
        OpsDocList opsDocList, Map<String, Object> cfg) {
        List<OpTemplate> opTemplates = new ArrayList<>();
        for (DriverAdapter<?, ?> adapter : adapters.values()) {
            if (adapter instanceof SyntheticOpTemplateProvider sotp) {
                List<OpTemplate> newTemplates = sotp.getSyntheticOpTemplates(opsDocList, cfg);
                opTemplates.addAll(newTemplates);
            }
        }
        return opTemplates;
    }

    /**
     This is done here since driver adapters are intended to keep all of their state within
     dedicated <em>state space</em> types. Any space which implements {@link Shutdownable}
     will be closed when this activity shuts down.
     */
    public void shutdownActivity() {
        for (Map.Entry<String, DriverAdapter<CycleOp<?>, Space>> entry : adapters.entrySet()) {
            String adapterName = entry.getKey();
            DriverAdapter<?, ?> adapter = entry.getValue();
            if (adapter instanceof AutoCloseable autoCloseable) {
                adapter.close();
            }
        }
    }

    @Override
    public NBLabels getLabels() {
        return super.getLabels();
    }


    @Override
    public void onEvent(NBEvent event) {
        switch (event) {
            case ParamChange<?> pc -> {
                switch (pc.value()) {
                    case SetThreads st -> activityDef.setThreads(st.threads);
                    case CycleRateSpec crs -> createOrUpdateCycleLimiter(crs);
                    case StrideRateSpec srs -> createOrUpdateStrideLimiter(srs);
                    default -> super.onEvent(event);
                }
            }
            default -> super.onEvent(event);
        }
    }

    protected List<OpTemplate> loadOpTemplates(
        DriverAdapter<?, ?> defaultDriverAdapter,
        boolean logged,
        boolean filtered
    ) {

        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");

        OpsDocList opsDocList = loadStmtsDocList();

        List<OpTemplate> filteredOps = opsDocList.getOps(filtered?tagfilter:"", logged);

        if (filteredOps.isEmpty()) {
            // There were no ops, and it *wasn't* because they were all filtered out.
            // In this case, let's try to synthesize the ops as long as at least a default driver was provided
            // But if there were no ops, and there was no default driver provided, we can't continue
            // There were no ops, and it was because they were all filtered out
            List<OpTemplate> unfilteredOps = opsDocList.getOps(false);
            if (!unfilteredOps.isEmpty()) {
                String message = "There were no active op templates with tag filter '"+ tagfilter + "', since all " +
                    unfilteredOps.size() + " were filtered out. Examine the session log for details";
                NBAdvisorOutput.test(message);
                //throw new BasicError(message);
            }
            if (defaultDriverAdapter instanceof SyntheticOpTemplateProvider sotp) {
                filteredOps = sotp.getSyntheticOpTemplates(opsDocList, this.activityDef.getParams());
                Objects.requireNonNull(filteredOps);
                if (filteredOps.isEmpty()) {
                    throw new BasicError("Attempted to create synthetic ops from driver '" + defaultDriverAdapter.getAdapterName() + '\'' +
                                             " but no ops were created. You must provide either a workload or an op parameter. Activities require op templates.");
                }
            } else {
                throw new BasicError("""
                    No op templates were provided. You must provide one of these activity parameters:
                    1) workload=some.yaml
                    2) op='inline template'
                    3) driver=stdout (or any other drive that can synthesize ops)""");
            }
        }
        return filteredOps;
    }

    /**
     * Modify the provided ActivityDef with defaults for stride and cycles, if they haven't been provided, based on the
     * length of the sequence as determined by the provided ratios. Also, modify the ActivityDef with reasonable
     * defaults when requested.
     *
     * @param seq
     *     - The {@link OpSequence} to derive the defaults from
     */
    public synchronized void setDefaultsFromOpSequence(OpSequence<?> seq) {
        Optional<String> strideOpt = getParams().getOptionalString("stride");
        if (strideOpt.isEmpty()) {
            String stride = String.valueOf(seq.getSequence().length);
            logger.info(() -> "defaulting stride to " + stride + " (the sequence length)");
//            getParams().set("stride", stride);
            getParams().setSilently("stride", stride);
        }

        // CYCLES
        Optional<String> cyclesOpt = getParams().getOptionalString("cycles");
        if (cyclesOpt.isEmpty()) {
            String cycles = getParams().getOptionalString("stride").orElseThrow();
            logger.info(() -> "defaulting cycles to " + cycles + " (the stride length)");
            this.getActivityDef().setCycles(getParams().getOptionalString("stride").orElseThrow());
        } else {
            if (0 == activityDef.getCycleCount()) {
                throw new RuntimeException(
                    "You specified cycles, but the range specified means zero cycles: " + getParams().get("cycles")
                );
            }
            long stride = getParams().getOptionalLong("stride").orElseThrow();
            long cycles = this.activityDef.getCycleCount();
            if (cycles < stride) {
                throw new RuntimeException(
                    "The specified cycles (" + cycles + ") are less than the stride (" + stride + "). This means there aren't enough cycles to cause a stride to be executed." +
                        " If this was intended, then set stride low enough to allow it."
                );
            }
        }

        long cycleCount = this.activityDef.getCycleCount();
        long stride = this.activityDef.getParams().getOptionalLong("stride").orElseThrow();

        if (0 < stride && 0 != cycleCount % stride) {
            logger.warn(() -> "The stride does not evenly divide cycles. Only full strides will be executed," +
                "leaving some cycles unused. (stride=" + stride + ", cycles=" + cycleCount + ')');
        }

        Optional<String> threadSpec = activityDef.getParams().getOptionalString("threads");
        if (threadSpec.isPresent()) {
            String spec = threadSpec.get();
            int processors = Runtime.getRuntime().availableProcessors();
            if ("auto".equalsIgnoreCase(spec)) {
                int threads = processors * 10;
                if (threads > activityDef.getCycleCount()) {
                    threads = (int) activityDef.getCycleCount();
                    logger.info("setting threads to {} (auto) [10xCORES, cycle count limited]", threads);
                } else {
                    logger.info("setting threads to {} (auto) [10xCORES]", threads);
                }
//                activityDef.setThreads(threads);
                activityDef.getParams().setSilently("threads", threads);
            } else if (spec.toLowerCase().matches("\\d+x")) {
                String multiplier = spec.substring(0, spec.length() - 1);
                int threads = processors * Integer.parseInt(multiplier);
                logger.info(() -> "setting threads to " + threads + " (" + multiplier + "x)");
//                activityDef.setThreads(threads);
                activityDef.getParams().setSilently("threads", threads);
            } else if (spec.toLowerCase().matches("\\d+")) {
                logger.info(() -> "setting threads to " + spec + " (direct)");
//                activityDef.setThreads(Integer.parseInt(spec));
                activityDef.getParams().setSilently("threads", Integer.parseInt(spec));
            }

            if (activityDef.getThreads() > activityDef.getCycleCount()) {
                logger.warn(() -> "threads=" + activityDef.getThreads() + " and cycles=" + activityDef.getCycleSummary()
                    + ", you should have more cycles than threads.");
            }

        } else if (1000 < cycleCount) {
            logger.warn(() -> "For testing at scale, it is highly recommended that you " +
                "set threads to a value higher than the default of 1." +
                " hint: you can use threads=auto for reasonable default, or" +
                " consult the topic on threads with `help threads` for" +
                " more information.");
        }

        if (0 < this.activityDef.getCycleCount() && seq.getOps().isEmpty()) {
            throw new BasicError("You have configured a zero-length sequence and non-zero cycles. It is not possible to continue with this activity.");
        }
    }

    /**
     * Given a function that can create an op of type <O> from an OpTemplate, generate
     * an indexed sequence of ready to call operations.
     * <p>
     * This method uses the following conventions to derive the sequence:
     *
     * <OL>
     * <LI>If an 'op', 'stmt', or 'statement' parameter is provided, then it's value is
     * taken as the only provided statement.</LI>
     * <LI>If a 'yaml, or 'workload' parameter is provided, then the statements in that file
     * are taken with their ratios </LI>
     * <LI>Any provided tags filter is used to select only the op templates which have matching
     * tags. If no tags are provided, then all the found op templates are included.</LI>
     * <LI>The ratios and the 'seq' parameter are used to build a sequence of the ready operations,
     * where the sequence length is the sum of the ratios.</LI>
     * </OL>
     *
     * @param <O>
     *     A holder for an executable operation for the native driver used by this activity.
     * @param opinit
     *     A function to map an OpTemplate to the executable operation form required by
     *     the native driver for this activity.
     * @param defaultAdapter
     *     The adapter which will be used for any op templates with no explicit adapter
     * @return The sequence of operations as determined by filtering and ratios
     */
    @Deprecated(forRemoval = true)
    protected <O> OpSequence<OpDispenser<? extends O>> createOpSequence(
        Function<OpTemplate,
            OpDispenser<? extends O>> opinit, boolean strict, DriverAdapter<?, ?> defaultAdapter) {

        List<OpTemplate> stmts = loadOpTemplates(defaultAdapter,true,false);

        List<Long> ratios = new ArrayList<>(stmts.size());

        for (OpTemplate opTemplate : stmts) {
            long ratio = opTemplate.removeParamOrDefault("ratio", 1);
            ratios.add(ratio);
        }

        SequencerType sequencerType = getParams()
            .getOptionalString("seq")
            .map(SequencerType::valueOf)
            .orElse(SequencerType.bucket);

        SequencePlanner<OpDispenser<? extends O>> planner = new SequencePlanner<>(sequencerType);

        try {
            for (int i = 0; i < stmts.size(); i++) {
                long ratio = ratios.get(i);
                OpTemplate optemplate = stmts.get(i);
                OpDispenser<? extends O> driverSpecificReadyOp = opinit.apply(optemplate);
                if (strict) {
                    optemplate.assertConsumed();
                }
                planner.addOp(driverSpecificReadyOp, ratio);
            }
        } catch (Exception e) {
            throw new OpConfigError(e.getMessage(), workloadSource, e);
        }

        return planner.resolve();
    }

    protected OpsDocList loadStmtsDocList() {

        try {
            String op = activityDef.getParams().getOptionalString("op").orElse(null);
            String stmt = activityDef.getParams().getOptionalString("stmt", "statement").orElse(null);
            String workload = activityDef.getParams().getOptionalString("workload").orElse(null);

            if ((op != null ? 1 : 0) + (stmt != null ? 1 : 0) + (workload != null ? 1 : 0) > 1) {
                throw new OpConfigError("Only op, statement, or workload may be provided, not more than one.");
            }


            if (workload != null && OpsLoader.isJson(workload)) {
                workloadSource = "commandline: (workload/json):" + workload;
                return OpsLoader.loadString(workload, OpTemplateFormat.json, activityDef.getParams(), null);
            } else if (workload != null && OpsLoader.isYaml(workload)) {
                workloadSource = "commandline: (workload/yaml):" + workload;
                return OpsLoader.loadString(workload, OpTemplateFormat.yaml, activityDef.getParams(), null);
            } else if (workload != null) {
                return OpsLoader.loadPath(workload, activityDef.getParams(), "activities");
            }

            if (stmt != null) {
                workloadSource = "commandline: (stmt/inline): '" + stmt + "'";
                return OpsLoader.loadString(stmt, OpTemplateFormat.inline, activityDef.getParams(), null);
            }

            if (op != null && OpsLoader.isJson(op)) {
                workloadSource = "commandline: (op/json): '" + op + "'";
                return OpsLoader.loadString(op, OpTemplateFormat.json, activityDef.getParams(), null);
            }
            else if (op != null) {
                workloadSource = "commandline: (op/inline): '" + op + "'";
                return OpsLoader.loadString(op, OpTemplateFormat.inline, activityDef.getParams(), null);
            }
            return OpsDocList.none();

        } catch (Exception e) {
            throw new OpConfigError("Error loading op templates: " + e, workloadSource, e);
        }
    }

    @Override
    public synchronized ProgressMeterDisplay getProgressMeter() {
        if (null == this.progressMeter) {
            this.progressMeter = new ActivityMetricProgressMeter(this);
        }
        return this.progressMeter;
    }

    @Override
    public synchronized RunState getRunState() {
        return runState;
    }

    public synchronized void setRunState(RunState runState) {
        this.runState = runState;
        if (RunState.Running == runState) {
            this.startedAtMillis = System.currentTimeMillis();
        }
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public ActivityDef getActivityDef() {
        return activityDef;
    }

    public String toString() {
        return (activityDef != null ? activityDef.getAlias() : "unset_alias") + ':' + this.runState + ':' + this.tally;
    }

    public synchronized void initOrUpdateRateLimiters(ActivityDef activityDef) {

//        cycleratePerThread = activityDef.getParams().takeBoolOrDefault("cyclerate_per_thread", false);

        activityDef.getParams().getOptionalNamedParameter("striderate")
            .map(StrideRateSpec::new).ifPresent(sr -> this.onEvent(new ParamChange<>(sr)));

        activityDef.getParams().getOptionalNamedParameter("cyclerate", "targetrate", "rate")
            .map(CycleRateSpec::new).ifPresent(sr -> this.onEvent(new ParamChange<>(sr)));

    }

    public void createOrUpdateStrideLimiter(SimRateSpec spec) {
        strideLimiterSource = ThreadLocalRateLimiters.createOrUpdate(this, strideLimiterSource, spec);
    }

    public void createOrUpdateCycleLimiter(SimRateSpec spec) {
        cycleLimiterSource = ThreadLocalRateLimiters.createOrUpdate(this, cycleLimiterSource, spec);
    }

    /**
     * Get the current cycle rate limiter for this activity.
     * The cycle rate limiter is used to throttle the rate at which
     * cycles are dispatched across all threads in the activity
     * @return the cycle {@link RateLimiter}
     */
    public RateLimiter getCycleLimiter() {
        if (cycleLimiterSource!=null) {
            return cycleLimiterSource.get();
        } else {
            return null;
        }
    }
    /**
     * Get the current stride rate limiter for this activity.
     * The stride rate limiter is used to throttle the rate at which
     * new strides are dispatched across all threads in an activity.
     * @return The stride {@link RateLimiter}
     */
    public synchronized RateLimiter getStrideLimiter() {
        if (strideLimiterSource!=null) {
            return strideLimiterSource.get();
        } else {
            return null;
        }
    }

    public RunStateTally getRunStateTally() {
        return tally;
    }

    public ActivityWiring getWiring() {
        return this.wiring;
    }

    @Override
    public Map<String, String> asResult() {
        return Map.of("activity",this.getActivityDef().getAlias());
    }

    /**
     * Activities with retryable operations (when specified with the retry error handler for some
     * types of error), should allow the user to specify how many retries are allowed before
     * giving up on the operation.
     *
     * @return The number of allowable retries
     */
    public int getMaxTries() {
        return this.activityDef.getParams().getOptionalInteger("maxtries").orElse(10);
    }

    public synchronized NBErrorHandler getErrorHandler() {
        if (null == this.errorHandler) {
            errorHandler = new NBErrorHandler(
                () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
                this::getExceptionMetrics);
        }
        return errorHandler;
    }

    public void closeAutoCloseables() {
        for (AutoCloseable closeable : closeables) {
            logger.debug(() -> "CLOSING " + closeable.getClass().getCanonicalName() + ": " + closeable);
            try {
                closeable.close();
            } catch (Exception e) {
                throw new RuntimeException("Error closing " + closeable + ": " + e, e);
            }
        }
        closeables.clear();
    }

    @Override
    public int compareTo(Activity o) {
        return this.getActivityDef().getAlias().compareTo(o.getActivityDef().getAlias());
    }

    public void registerAutoCloseable(AutoCloseable closeable) {
        this.closeables.add(closeable);
    }

    public synchronized PrintWriter getConsoleOut() {
        if (null == console) {
            this.console = new PrintWriter(System.out, false, StandardCharsets.UTF_8);
        }
        return this.console;
    }

    public synchronized InputStream getConsoleIn() {
        return System.in;
    }

    public void setConsoleOut(PrintWriter writer) {
        this.console = writer;
    }

    public synchronized ErrorMetrics getExceptionMetrics() {
        if (null == this.errorMetrics) {
            errorMetrics = new ErrorMetrics(this);
        }
        return errorMetrics;
    }


    public String getAlias() {
        return getActivityDef().getAlias();
    }

    public int getHdrDigits() {
        return getComponentProp("hdr_digits").map(Integer::parseInt).orElse(3);

    }
}
