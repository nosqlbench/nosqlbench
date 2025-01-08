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

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplates;
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
import io.nosqlbench.engine.api.activityapi.input.Input;
import io.nosqlbench.engine.api.activityapi.output.Output;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityapi.simrate.*;
import io.nosqlbench.engine.api.activityimpl.Dryrun;
import io.nosqlbench.engine.api.activityimpl.OpFunctionComposition;
import io.nosqlbench.engine.api.activityimpl.input.AtomicInput;
import io.nosqlbench.engine.api.activityimpl.motor.CoreMotor;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.engine.api.activityimpl.uniform.actions.StandardAction;
import io.nosqlbench.engine.core.lifecycle.commands.CMD_await;
import io.nosqlbench.engine.core.lifecycle.commands.CMD_start;
import io.nosqlbench.engine.core.lifecycle.commands.CMD_stop;
import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;
import io.nosqlbench.engine.core.lifecycle.session.NBSession;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.events.NBEvent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.nb.api.components.status.NBStatusComponent;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityConfig;
import io.nosqlbench.nb.api.engine.activityimpl.CyclesSpec;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.lifecycle.Shutdownable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.LongFunction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/// An [[Activity]] is a flywheel of operations. Each activity consumes ordinals
/// from a specified interval, maps them to executable operations via _op synthesis_ as determined
/// by the op templates supplied by the user, and executes those operations.
///
/// Activities each run within a named state-sharing partition called an activity container. If
/// no container name is specified, then a singular `default` container is used for all
/// activities. For named steps of a named scenario, the container name is set to the step name.
///
/// Activities and their containers are hosted within an [NBSession]. Activities run asynchronously
/// with respect to their container and session. There are a few session commands which
/// are used to manage activities, such as [start][CMD_start], [stop][CMD_stop], and
/// [await][CMD_await], for example.
///
/// The config parameters for an activity are standard, and custom behaviors afforded to activities
/// work the same across all op types.
public class Activity<R extends java.util.function.LongFunction, S> extends NBStatusComponent
    implements InvokableResult,
               SyntheticOpTemplateProvider,
               StateCapable,
               ProgressCapable,
               Comparable<Activity>,
               MotorDispenser,
               NBConfigurable,
               NBReconfigurable
{
    private static final Logger logger = LogManager.getLogger("ACTIVITY");

    private final OpSequence<OpDispenser<? extends CycleOp<?>>> sequence;
    private final ConcurrentHashMap<String, DriverAdapter<CycleOp<?>, Space>> adapters =
        new ConcurrentHashMap<>();

    public final ActivityMetrics metrics;
    private ActivityMetricProgressMeter progressMeter;
    private String workloadSource = "unspecified";
    private RunState runState = RunState.Uninitialized;
    private long startedAtMillis;
    private final RunStateTally tally = new RunStateTally();
    private ThreadLocal<RateLimiter> strideLimiterSource;
    private ThreadLocal<RateLimiter> cycleLimiterSource;
    private NBErrorHandler errorHandler;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private ErrorMetrics errorMetrics;
    private Input input;
    private StandardAction<?, ?> action;
    private ActivityConfig config;

    public Activity(NBComponent parent, ActivityConfig config) {
        super(parent, NBLabels.forKV("activity", config.getAlias()).and(config.auxLabels()));
        this.applyConfig(config);
        this.sequence = initSequence();
        this.metrics = new ActivityMetrics(this);
    }

    public static ActivityConfig configFor(String s) {
        return configFor(ParameterMap.parseParams(s).orElseThrow());
    }

    private OpSequence<OpDispenser<? extends CycleOp<?>>> initSequence() {
        Optional<String> yaml_loc = config.getOptional("yaml", "workload");

        OpTemplates opTemplatesRef = loadOpTemplates();

        /// How to load a named [DriverAdapter] with component parentage and labels, given
        /// the driver name and the activity (cross-driver) configuration
        AdapterResolver adapterResolver = new AdapterResolver();
        ConcurrentHashMap<String, DriverAdapter> adapterCache = new ConcurrentHashMap<>();

        /// Promote the driver adapter function into a cached version
        Function<String, DriverAdapter<? extends CycleOp<?>, Space>> adapterF =
            (name) -> adapterCache.computeIfAbsent(
                name,
                cacheName -> adapterResolver.apply(this, name, this.config)
            );

        /// How to get a parsed op, given an op template and an activity.
        /// A parsed op depends on back-fill from the activity params, assuming those params
        /// are included in the activity's [[NBConfiguration]], but the params are also filtered
        /// through the associated [[DriverAdapter]]'s configuration.
        ParsedOpResolver parsedOpF = new ParsedOpResolver();

        /// How to get an op dispenser, given an adapter and a parsed op
        /// The cached [Space] mechanism is included within the adapter's API, and is specific to each parsed op,
        /// since this is a dynamic op field
        DispenserResolver dispenserResolver = new DispenserResolver();

        OpResolverBank orb = new OpResolverBank(
            this,
            adapterResolver,
            opTemplatesRef,
            config.get("tags"),
            dispenserResolver,
            parsedOpF,
            config
        );
        List<? extends OpDispenser<?>> dispensers = orb.resolveDispensers();

        if (config.get("dryrun", Dryrun.class) == Dryrun.mapper) {
            System.out.println(Diagnostics.summarizeMappedOps(dispensers));
            System.exit(1);
        }

        SequencerType sequencerType =
            config.getOptional("seq").map(SequencerType::valueOf).orElse(SequencerType.bucket);

        SequencePlanner<OpDispenser<? extends CycleOp<?>>> planner =
            new SequencePlanner<>(sequencerType);

        for (OpDispenser<?> dispenser : dispensers) {
            planner.addOp(dispenser, d -> d.getRatio());
        }
        OpSequence<OpDispenser<? extends CycleOp<?>>> sequence = planner.resolve();

        if (config.get("dryrun", Dryrun.class) == Dryrun.sequence) {
            System.out.println(Diagnostics.summarizeSequencedOps(sequence));
            System.exit(1);
        }

        return sequence;
    }

    private void initOpsMetrics() {
        create().gauge(
            "ops_pending",
            () -> this.getProgressMeter().getSummary().pending(),
            MetricCategory.Core,
            "The current number of operations which have not been dispatched for"
            + " processing yet."
        );
        create().gauge(
            "ops_active",
            () -> this.getProgressMeter().getSummary().current(),
            MetricCategory.Core,
            "The current number of operations which have been dispatched for"
            + " processing, but which have not yet completed."
        );
        create().gauge(
            "ops_complete",
            () -> this.getProgressMeter().getSummary().complete(),
            MetricCategory.Core,
            "The current number of operations which have been completed"
        );
    }

    protected <O extends LongFunction> OpSequence<OpDispenser<? extends CycleOp<?>>> createOpSourceFromParsedOps2(List<DriverAdapter<CycleOp<?>, Space>> adapters,
        List<ParsedOp> pops,
        OpLookup opLookup
    )
    {
        try {

            List<Long> ratios = new ArrayList<>(pops.size());

            for (ParsedOp pop : pops) {
                long ratio = pop.takeStaticConfigOr("ratio", 1);
                ratios.add(ratio);
            }

            SequencerType sequencerType =
                config.getOptional("seq").map(SequencerType::valueOf).orElse(SequencerType.bucket);
            SequencePlanner<OpDispenser<? extends CycleOp<?>>> planner =
                new SequencePlanner<>(sequencerType);

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
                    OpDispenser<? extends CycleOp<?>> dispenser =
                        opMapper.apply(this, pop, spaceFunc);
                    String dryrunSpec = pop.takeStaticConfigOr("dryrun", "none");
                    Dryrun dryrun = pop.takeEnumFromFieldOr(Dryrun.class, Dryrun.none, "dryrun");

                    dispenser = OpFunctionComposition.wrapOptionally(
                        adapter,
                        dispenser,
                        pop,
                        dryrun,
                        opLookup
                    );

                    planner.addOp((OpDispenser<? extends CycleOp<?>>) dispenser, ratio);
                } catch (Exception e) {
                    throw new OpConfigError(
                        "Error while mapping op from template named '" + pop.getName() + "': "
                        + e.getMessage(), e
                    );
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

    public void initActivity() {
        initOrUpdateRateLimiters();
        setDefaultsFromOpSequence(sequence);
    }

    public OpSequence<OpDispenser<? extends CycleOp<?>>> getOpSequence() {
        return sequence;
    }

    @Override
    public OpTemplates getSyntheticOpTemplates(OpTemplates opsDocList, Map<String, Object> cfg) {
        OpTemplates accumulator = new OpTemplates();
        List<OpTemplates> combined = new ArrayList<>();
        for (DriverAdapter<?, ?> adapter : adapters.values()) {
            if (adapter instanceof SyntheticOpTemplateProvider sotp) {
                OpTemplates newTemplates = sotp.getSyntheticOpTemplates(opsDocList, cfg);
                accumulator = accumulator.and(newTemplates);
            }
        }
        return accumulator;
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
                    case SetThreads st -> config.update(ActivityConfig.FIELD_THREADS, st.threads);
                    case CycleRateSpec crs -> createOrUpdateCycleLimiter(crs);
                    case StrideRateSpec srs -> createOrUpdateStrideLimiter(srs);
                    default -> super.onEvent(event);
                }
            }
            default -> super.onEvent(event);
        }
    }

    /// Modify the provided activity config with defaults for stride and cycles, if they haven't
    /// been provided, based on the length of the sequence as determined by the provided ratios.
    /// Also, modify the activity config with reasonable defaults when requested.
    /// @param seq
    ///     - The {@link OpSequence} to derive the defaults from
    private synchronized void setDefaultsFromOpSequence(OpSequence<?> seq) {
        Map<String, Object> updates = new LinkedHashMap<>(config.getMap());

        updates.computeIfAbsent(
            "stride", k -> {
                String stride = String.valueOf(seq.getSequence().length);
                logger.info(() -> "defaulting stride to " + stride + " (the sequence length)");
                return stride;
            }
        );

        updates.computeIfAbsent(
            "cycles", k -> {
                String cycles = (String) updates.get("stride");
                logger.info(() -> "defaulting cycles to " + cycles + " (the stride length)");
                return cycles;
            }
        );

        long cycles = CyclesSpec.parse(updates.get("cycles").toString()).cycle_count();
        long stride = Long.parseLong(updates.get("stride").toString());
        if (cycles < stride) {
            throw new RuntimeException("""
                The specified cycles (CYCLES) are less than the stride (STRIDE)
                 This means there aren't enough cycles to cause a stride to be
                 executed. If this was intended, then set stride low enough to allow it
                """.replaceAll("CYCLES", String.valueOf(cycles))
                .replaceAll("STRIDE", String.valueOf(stride)));
        }

        Optional<String> threadSpec =
            Optional.ofNullable(updates.get("threads")).map(String::valueOf);

        if (threadSpec.isPresent()) {
            String spec = threadSpec.get();
            int processors = Runtime.getRuntime().availableProcessors();
            int threads = 0;
            if ("auto".equalsIgnoreCase(spec)) {
                threads = processors * 10;
                if (threads > cycles) {
                    threads = (int) cycles;
                    logger.info(
                        "setting threads to {} (auto) [10xCORES, cycle count limited]",
                        threads
                    );
                } else {
                    logger.info("setting threads to {} (auto) [10xCORES]", threads);
                }
            } else if (spec.toLowerCase().matches("\\d+x")) {
                String multiplier = spec.substring(0, spec.length() - 1);
                threads = processors * Integer.parseInt(multiplier);
                int finalThreads = threads;
                logger.info(() -> "setting threads to " + finalThreads + " (" + multiplier + "x)");
            } else if (spec.toLowerCase().matches("\\d+")) {
                logger.info(() -> "setting threads to " + spec + " (direct)");
            } else {
                throw new RuntimeException("Unrecognized format for threads:" + spec);
            }
            updates.put("threads", threads);


            if (threads > cycles) {
                int finalThreads1 = threads;
                logger.warn(() -> "threads=" + finalThreads1 + " and cycles="
                                  + updates.get("cycles").toString()
                                  + ", you should have more cycles than threads.");
            }

        } else if (1000 < cycles) {
            logger.warn(() -> "For testing at scale, it is highly recommended that you "
                              + "set threads to a value higher than the default of 1."
                              + " hint: you can use threads=auto for reasonable default, or"
                              + " consult the topic on threads with `help threads` for"
                              + " more information.");
        }

        if (0 < cycles && seq.getOps().isEmpty()) {
            throw new BasicError(
                "You have configured a zero-length sequence and non-zero cycles. It is not"
                + " possible to continue with this activity.");
        }
    }

    protected OpTemplates loadOpTemplates() {
        OpsDocList opsDocs = null;
        try {
            String op = config.getOptional("op").orElse(null);
            String stmt = config.getOptional("stmt", "statement").orElse(null);
            String workload = config.getOptional("workload").orElse(null);

            // If the user has specified more than one way of loading operations via
            // activity parameters, then throw an error saying so
            if ((op != null ? 1 : 0) + (stmt != null ? 1 : 0) + (workload != null ? 1 : 0) > 1) {
                throw new OpConfigError(
                    "Only op, statement, or workload may be provided, not more than one.");
            }

            // If the workload is literally in JSON format that starts with '{' and parsed to JSON,
            // then it is parsed into the workload template structure
            // instead of being loaded from a file.
            if (workload != null && OpsLoader.isJson(workload)) {
                workloadSource = "commandline: (workload/json):" + workload;
                opsDocs =
                    OpsLoader.loadString(workload, OpTemplateFormat.json, config.getMap(), null);
                return new OpTemplates(opsDocs);
            }

            // If the workload is literally in a multiline format that parses to YAML,
            // then it is parsed into the workload template structure
            // instead of being loaded from a file.
            if (workload != null && OpsLoader.isYaml(workload)) {
                workloadSource = "commandline: (workload/yaml):" + workload;
                opsDocs =
                    OpsLoader.loadString(workload, OpTemplateFormat.yaml, config.getMap(), null);
                return new OpTemplates(opsDocs);
            }

            // We try to load the workload from a file assuming YAML format,
            // if the workload parameter is defined and not loadable by other means
            if (workload != null) {
                opsDocs = OpsLoader.loadPath(workload, config.getMap(), "activities");
                return new OpTemplates(opsDocs);
            }

            // We take the stmt parameter
            // if defined
            // then wrap it in default workload template structure
            // as an op with an op field 'stmt'
            if (stmt != null) {
                workloadSource = "commandline: (stmt/inline): '" + stmt + "'";
                opsDocs =
                    OpsLoader.loadString(stmt, OpTemplateFormat.inline, config.getMap(), null);
                return new OpTemplates(opsDocs);
            }

            // We take the op parameter
            // if defined and in JSON format
            // the wrap it in default workload structure
            // as a set of op fields in JSON format
            if (op != null && OpsLoader.isJson(op)) {
                workloadSource = "commandline: (op/json): '" + op + "'";
                opsDocs = OpsLoader.loadString(op, OpTemplateFormat.json, config.getMap(), null);
            }

            // We take the op parameter
            // if defined and not loadable via other means
            // then assume it is yaml
            // then wrap it in default workload structure
            // as a set of op fields in YAML format
            if (op != null) {
                workloadSource = "commandline: (op/inline): '" + op + "'";
                opsDocs = OpsLoader.loadString(op, OpTemplateFormat.inline, config.getMap(), null);
                return new OpTemplates(opsDocs);
            }

            // If no methods of loading op templates were provided
            // via op, stmt, or workload parameters, then we also check
            // for synthetic ops, as in stdout making default formats from
            // binding names
            if (config.getOptional("driver").isPresent()) {
                String driverName = config.get("driver");
                DriverAdapter<? extends CycleOp<?>, Space> defaultDriverAdapter =
                    AdapterResolver.loadNamedAdapter(this, driverName);
                if (defaultDriverAdapter instanceof SyntheticOpTemplateProvider sotp) {
                    var filteredOps =
                        sotp.getSyntheticOpTemplates(new OpTemplates(), config.getMap());
                    Objects.requireNonNull(filteredOps);
                    if (filteredOps.isEmpty()) {
                        throw new BasicError("""
                            Attempted to create synthetic ops from driver ADAPTERNAME
                            but no ops were created. You must provide either a workload
                            or an op parameter. Activities require op templates.
                            """.replaceAll("ADAPTERNAME", defaultDriverAdapter.getAdapterName()));
                    }
                }
            }

            // All possible methods of loading a op templates have failed
            throw new BasicError("""
                No op templates were provided. You must provide one of these activity parameters:
                1) workload=some.yaml
                2) op='inline template'
                3) driver=stdout (or any other drive that can synthesize ops)\
                """);

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

    public String toString() {
        return config.getAlias() + ':' + this.runState + ':' + this.tally;
    }

    public synchronized void initOrUpdateRateLimiters() {

        //        cycleratePerThread =
        // activityDef.getParams().takeBoolOrDefault("cyclerate_per_thread", false);

        config.getOptional("striderate").map(StrideRateSpec::new)
            .ifPresent(sr -> this.onEvent(new ParamChange<>(sr)));

        config.getOptional("cyclerate", "targetrate", "rate").map(CycleRateSpec::new)
            .ifPresent(sr -> this.onEvent(new ParamChange<>(sr)));
    }

    public void createOrUpdateStrideLimiter(SimRateSpec spec) {
        strideLimiterSource =
            ThreadLocalRateLimiters.createOrUpdate(this, strideLimiterSource, spec);
    }

    /// Get the current cycle rate limiter for this activity.
    /// The cycle rate limiter is used to throttle the rate at which
    /// cycles are dispatched across all threads in the activity
    /// @return the cycle {@link RateLimiter}
    public RateLimiter getCycleLimiter() {
        if (cycleLimiterSource != null) {
            return cycleLimiterSource.get();
        } else {
            return null;
        }
    }

    /// Get the current stride rate limiter for this activity.
    /// The stride rate limiter is used to throttle the rate at which
    /// new strides are dispatched across all threads in an activity.
    /// @return The stride {@link RateLimiter}
    public synchronized RateLimiter getStrideLimiter() {
        if (strideLimiterSource != null) {
            return strideLimiterSource.get();
        } else {
            return null;
        }
    }

    public RunStateTally getRunStateTally() {
        return tally;
    }

    @Override
    public Map<String, String> asResult() {
        return Map.of("activity", config.getAlias());
    }

    /// Activities with retryable operations (when specified with the retry error handler for some
    /// types of error), should allow the user to specify how many retries are allowed before
    /// giving up on the operation.
    /// @return The number of allowable retries
    public int getMaxTries() {
        return config.getOptional(Integer.class, "maxtries").orElse(10);
    }

    public synchronized NBErrorHandler getErrorHandler() {
        if (null == this.errorHandler) {
            errorHandler = new NBErrorHandler(
                () -> config.getOptional("errors").orElse("stop"),
                this::getExceptionMetrics
            );
        }
        return errorHandler;
    }

    public void closeAutoCloseables() {
        for (AutoCloseable closeable : closeables) {
            logger.debug(() -> "CLOSING " + closeable.getClass().getCanonicalName() + ": "
                               + closeable);
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
        return getAlias().compareTo(o.getAlias());
    }

    //    public void registerAutoCloseable(AutoCloseable closeable) {
    //        this.closeables.add(closeable);
    //    }
    //
    public synchronized ErrorMetrics getExceptionMetrics() {
        if (null == this.errorMetrics) {
            errorMetrics = new ErrorMetrics(this);
        }
        return errorMetrics;
    }

    public String getAlias() {
        return config.getAlias();
    }

    @Override
    public Motor getMotor(ActivityConfig activityConfig, int slot) {
        return new CoreMotor(this, slot, getInput(), getAction(), getOutput());
    }

    public synchronized Input getInput() {
        if (input == null) {
            this.input = new AtomicInput(this);
        }
        return this.input;
    }

    public synchronized SyncAction getAction() {
        if (this.action == null) {
            this.action = new StandardAction(this);
        }
        return this.action;
    }

    public synchronized Output getOutput() {
        // TODO: Implement this as optional, only composing the optional behavior if required
        return null;
    }

    private void createOrUpdateCycleLimiter(SimRateSpec spec) {
        cycleLimiterSource = ThreadLocalRateLimiters.createOrUpdate(this, cycleLimiterSource, spec);
    }

    public ActivityConfig getConfig() {
        return this.config;
    }

    public static ActivityConfig configFor(Map<String, ?> params) {
        return new ActivityConfig(configModel.apply(params));
    }

    private static NBConfigModel configModel =
        ConfigModel.of(Activity.class).add(Param.optional("alias")).add(Param.optional(
                "labels",
                String.class,
                "Labels which will apply to metrics and annotations for this activity only"
            )).add(Param.defaultTo(
                "strict",
                true,
                "strict op field mode, which requires that provided op fields are recognized and used"
            )).add(Param.optional("op", String.class, "op template in statement form"))
            .add(Param.optional(
                List.of("stmt", "statement"),
                String.class,
                "op template in statement " + "form"
            )).add(Param.defaultTo("tags", "", "tag filter to be used to filter operations"))
            .add(Param.defaultTo("errors", "stop", "error handler configuration"))
            .add(Param.defaultTo("threads", "1").setRegex("\\d+|\\d+x|auto")
                .setDescription("number of concurrent operations, controlled by threadpool"))
            .add(Param.optional("stride").setRegex("\\d+"))
            .add(Param.optional("striderate", String.class, "rate limit for strides per second"))
            .add(Param.defaultTo("cycles", "1")
                .setRegex("\\d+[KMBGTPE]?|\\d+[KMBGTPE]?\\.\\" + ".\\d+[KMBGTPE]?")
                .setDescription("cycle interval to use")).add(Param.defaultTo("recycles", "1")
                .setDescription("allow cycles to be re-used this many " + "times")).add(Param.optional(
                List.of("cyclerate", "targetrate", "rate"),
                String.class,
                "rate limit for cycles per second"
            )).add(Param.optional("seq", String.class, "sequencing algorithm"))
            .add(Param.optional("instrument", Boolean.class))
            .add(Param.optional(
                List.of("workload", "yaml"),
                String.class,
                "location of workload yaml file"
            )).add(Param.optional("driver", String.class))
            .add(Param.defaultTo("dryrun", "none").setRegex("(op|jsonnet|emit|none)"))
            .add(Param.optional("maxtries", Integer.class)).add(Param.defaultTo(
                "input",
                "type=atomicseq",
                "The type of cycle input to use for this " + "activity"
            )).add(Param.optional(List.of("if", "inputfilter"), String.class, "an input filter"))
            .add(Param.optional("output", String.class)).asReadOnly();

    @Override
    public NBConfigModel getConfigModel() {
        return configModel;
    }

    @Override
    public void applyConfig(NBConfiguration config) {

        Optional<String> directAlias = config.getOptional("alias");
        NBConfigurable.applyMatchingCollection(config, adapters.values());

        this.config = new ActivityConfig(config);
    }

    @Override
    public void applyReconfig(NBConfiguration reconf) {
        this.config = new ActivityConfig(getReconfigModel().apply(reconf.getMap()));
    }

    @Override
    public NBConfigModel getReconfigModel() {
        return ConfigModel.of(Activity.class)
            .add(Param.optional("threads").setRegex("\\d+|\\d+x|auto")
                .setDescription("number of concurrent operations, controlled by threadpool"))
            .add(Param.optional("striderate", String.class, "rate limit for strides per second"))
            .add(Param.optional(
                List.of("cyclerate", "targetrate", "rate"),
                String.class,
                "rate limit for cycles per second"
            )).asReadOnly();
    }


    public CyclesSpec getCyclesSpec() {
        return CyclesSpec.parse(config.get(ActivityConfig.FIELD_CYCLES));
    }
}
