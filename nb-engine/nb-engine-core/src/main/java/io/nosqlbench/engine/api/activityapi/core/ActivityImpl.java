/*
 * Copyright (c) 2022-2024 nosqlbench
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

package io.nosqlbench.engine.api.activityapi.core;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import io.nosqlbench.engine.api.activityapi.core.progress.ActivityMetricProgressMeter;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityapi.core.progress.StateCapable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IntPredicateDispenser;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityapi.simrate.*;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.engine.api.activityimpl.OpFunctionComposition;
import io.nosqlbench.engine.api.activityimpl.Dryrun;
import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;
import io.nosqlbench.nb.annotations.ServiceSelector;
import io.nosqlbench.nb.api.advisor.NBAdvisorOutput;
import io.nosqlbench.nb.api.advisor.NBAdvisorPoint;
import io.nosqlbench.nb.api.advisor.conditions.Conditions;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.events.NBEvent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.nb.api.components.status.NBStatusComponent;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.lifecycle.Shutdownable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * Unified Activity implementation combining SimpleActivity and StandardActivity functionality.
 * This is the single concrete implementation of all activity functionality in NoSQLBench.
 */
public class ActivityImpl extends NBStatusComponent
    implements Comparable<Activity>, ActivityDefObserver, ProgressCapable, StateCapable,
               NBComponent, SyntheticOpTemplateProvider, InvokableResult {

    private static final Logger logger = LogManager.getLogger("ACTIVITY");

    // Core fields from SimpleActivity
    protected ActivityDef activityDef;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private MotorDispenser<?> motorDispenser;
    private InputDispenser inputDispenser;
    private ActionDispenser actionDispenser;
    private OutputDispenser markerDispenser;
    private IntPredicateDispenser resultFilterDispenser;
    private RunState runState = RunState.Uninitialized;
    private ThreadLocal<RateLimiter> strideLimiterSource;
    private ThreadLocal<RateLimiter> cycleLimiterSource;
    private ActivityInstrumentation activityInstrumentation;
    private PrintWriter console;
    private long startedAtMillis;
    private int nameEnumerator;
    private ErrorMetrics errorMetrics;
    private NBErrorHandler errorHandler;
    private ActivityMetricProgressMeter progressMeter;
    private String workloadSource = "unspecified";
    private final RunStateTally tally = new RunStateTally();

    // Fields from StandardActivity
    private OpSequence<OpDispenser<? extends CycleOp<?>>> sequence;
    private final ConcurrentHashMap<String, DriverAdapter<CycleOp<?>, Space>> adapters = new ConcurrentHashMap<>();

    /**
     * Create a new Activity with the specified parent component and activity definition.
     * This constructor handles both simple and standard activity initialization patterns.
     */
    public ActivityImpl(NBComponent parent, ActivityDef activityDef) {
        super(parent, NBLabels.forKV("activity", activityDef.getAlias()).and(activityDef.auxLabels()));
        this.activityDef = activityDef;

        // Initialize alias if needed (from SimpleActivity)
        if (activityDef.getAlias().equals(ActivityDef.DEFAULT_ALIAS)) {
            Optional<String> workloadOpt = activityDef.getParams().getOptionalString("workload", "yaml");
            if (workloadOpt.isPresent()) {
                activityDef.getParams().set("alias", workloadOpt.get());
            } else {
                activityDef.getParams().set("alias",
                    activityDef.getActivityDriver().toUpperCase(Locale.ROOT) + nameEnumerator);
                nameEnumerator++;
            }
        }

        // Initialize StandardActivity components only if needed (when workload/ops are provided)
        String defaultDriverName = activityDef.getActivityDriver();
        boolean hasWorkload = activityDef.getParams().getOptionalString("yaml", "workload").isPresent() ||
                              activityDef.getParams().getOptionalString("op").isPresent() ||
                              activityDef.getParams().getOptionalString("stmt", "statement").isPresent();

        if (defaultDriverName != null && !defaultDriverName.isEmpty() && hasWorkload) {
            initializeStandardComponents(defaultDriverName);
        }
    }

    /**
     * Initialize components needed for StandardActivity functionality.
     * This is called when a driver is specified.
     */
    private void initializeStandardComponents(String defaultDriverName) {
        NBAdvisorPoint<String> paramsAdvisor = create().advisor(b -> b.name("Workload"));
        paramsAdvisor.add(Conditions.ValidNameError);

        List<DriverAdapter<CycleOp<?>, Space>> adapterlist = new ArrayList<>();
        List<ParsedOp> pops = new ArrayList<>();
        ConcurrentHashMap<String, OpMapper<? extends CycleOp<?>, ? extends Space>> mappers = new ConcurrentHashMap<>();

        NBConfigModel activityModel = activityDef.getConfigModel();
        NBConfigModel yamlmodel;
        NBConfigModel adapterModel;

        DriverAdapter<CycleOp<?>, Space> defaultAdapter = getDriverAdapter(defaultDriverName);
        DriverAdapter<CycleOp<?>, Space> adapter;

        OpsDocList workload;
        Optional<String> yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
        if (yaml_loc.isPresent()) {
            Map<String, Object> disposable = new LinkedHashMap<>(activityDef.getParams());
            workload = loadStmtsDocList();
            yamlmodel = workload.getConfigModel();
        } else {
            yamlmodel = ConfigModel.of(ActivityImpl.class).asReadOnly();
        }
        yamlmodel.log();

        NBConfigModel supersetConfig = ConfigModel.of(ActivityImpl.class).add(yamlmodel);

        // Load the op templates
        List<OpTemplate> opTemplates = loadOpTemplates(defaultAdapter);
        NBConfigModel combinedAdapterModel = ConfigModel.of(ActivityImpl.class);

        for (OpTemplate ot : opTemplates) {
            logger.debug(() -> "ActivityImpl.opTemplate = " + ot);
            String driverName = ot.getOptionalStringParam("driver", String.class)
                .or(() -> ot.getOptionalStringParam("type", String.class))
                .orElse(defaultDriverName);

            if (!adapters.containsKey(driverName)) {
                adapter = defaultDriverName.equals(driverName) ? defaultAdapter : getDriverAdapter(driverName);
                NBConfigModel combinedModel = yamlmodel;
                NBConfiguration combinedConfig = combinedModel.matchConfig(activityDef.getParams());

                if (adapter instanceof NBConfigurable configurable) {
                    adapterModel = configurable.getConfigModel();
                    combinedAdapterModel.add(adapterModel);
                    supersetConfig.add(adapterModel);
                    combinedModel = adapterModel.add(yamlmodel);
                    combinedConfig = combinedModel.matchConfig(activityDef.getParams());
                    configurable.applyConfig(combinedConfig);
                }

                adapters.put(driverName, adapter);
                mappers.put(driverName, adapter.getOpMapper());
            } else {
                adapter = adapters.get(driverName);
            }

            paramsAdvisor.validateAll(ot.getParams().keySet());
            paramsAdvisor.validateAll(ot.getTags().keySet());
            paramsAdvisor.validateAll(ot.getBindings().keySet());
            adapterlist.add(adapter);

            ParsedOp pop = new ParsedOp(ot, adapter.getConfiguration(), List.of(adapter.getPreprocessor()), this);
            logger.debug("ActivityImpl.pop=" + pop);
            Optional<String> discard = pop.takeOptionalStaticValue("driver", String.class);
            pops.add(pop);
        }

        logger.debug(() -> "ActivityImpl.opTemplate loop complete");

        paramsAdvisor.setName("Workload", "Check parameters, template, and binding names")
            .logName().evaluate();

        paramsAdvisor.clear().setName("Superset", "Check overall parameters");
        paramsAdvisor.validateAll(supersetConfig.getNamedParams().keySet());
        paramsAdvisor.logName().evaluate();

        combinedAdapterModel.assertNoConflicts(yamlmodel.getNamedParams(), "Template");
        combinedAdapterModel.log();
        supersetConfig.assertValidConfig(activityDef.getParams().getStringStringMap());
        supersetConfig.log();

        if (0 == mappers.keySet().stream().filter(n -> n.equals(defaultDriverName)).count()) {
            logger.warn(() -> "All op templates used a different driver than the default '" + defaultDriverName + "'");
        }

        try {
            sequence = createOpSourceFromParsedOps(adapterlist, pops);
        } catch (Exception e) {
            if (e instanceof OpConfigError) {
                throw e;
            }
            throw new OpConfigError("Error mapping workload template to operations: " + e.getMessage(), null, e);
        }

        // Create metrics
        create().gauge(
            "ops_pending",
            () -> this.getProgressMeter().getSummary().pending(),
            MetricCategory.Core,
            "The current number of operations which have not been dispatched for processing yet."
        );
        create().gauge(
            "ops_active",
            () -> this.getProgressMeter().getSummary().current(),
            MetricCategory.Core,
            "The current number of operations which have been dispatched for processing, but which have not yet completed."
        );
        create().gauge(
            "ops_complete",
            () -> this.getProgressMeter().getSummary().complete(),
            MetricCategory.Core,
            "The current number of operations which have been completed"
        );
    }

    private DriverAdapter<CycleOp<?>, Space> getDriverAdapter(String driverName) {
        return Optional.of(driverName)
            .flatMap(name -> ServiceSelector.of(name, ServiceLoader.load(DriverAdapterLoader.class)).get())
            .map(l -> l.load(this, NBLabels.forKV()))
            .orElseThrow(() -> new OpConfigError("Unable to load '" + driverName + "' driver adapter.\n" +
                "If this is a valid driver then you may need to rebuild NoSqlBench to include this driver adapter. " +
                "Change '<activeByDefault>false</activeByDefault>' for the driver in " +
                "'./nb-adapters/pom.xml' and './nb-adapters/nb-adapters-included/pom.xml'."));
    }

    // Activity interface implementation methods

    public void registerAutoCloseable(AutoCloseable closeable) {
        this.closeables.add(closeable);
    }

    public ActivityDef getActivityDef() {
        return activityDef;
    }

    public String getAlias() {
        return this.getActivityDef().getAlias();
    }

    public ParameterMap getParams() {
        return this.getActivityDef().getParams();
    }

    public synchronized void initActivity() {
        initOrUpdateRateLimiters(this.activityDef);
        if (sequence != null) {
            setDefaultsFromOpSequence(sequence);
        }
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

    public MotorDispenser<?> getMotorDispenserDelegate() {
        return motorDispenser;
    }

    public void setMotorDispenserDelegate(MotorDispenser<?> motorDispenser) {
        this.motorDispenser = motorDispenser;
    }

    public InputDispenser getInputDispenserDelegate() {
        return inputDispenser;
    }

    public void setInputDispenserDelegate(InputDispenser inputDispenser) {
        this.inputDispenser = inputDispenser;
    }

    public ActionDispenser getActionDispenserDelegate() {
        return actionDispenser;
    }

    public void setActionDispenserDelegate(ActionDispenser actionDispenser) {
        this.actionDispenser = actionDispenser;
    }

    public IntPredicateDispenser getResultFilterDispenserDelegate() {
        return resultFilterDispenser;
    }

    public void setResultFilterDispenserDelegate(IntPredicateDispenser resultFilterDispenser) {
        this.resultFilterDispenser = resultFilterDispenser;
    }

    public OutputDispenser getMarkerDispenserDelegate() {
        return this.markerDispenser;
    }

    public void setOutputDispenserDelegate(OutputDispenser outputDispenser) {
        this.markerDispenser = outputDispenser;
    }

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

    public void shutdownActivity() {
        // Shutdown adapters from StandardActivity
        for (Map.Entry<String, DriverAdapter<CycleOp<?>, Space>> entry : adapters.entrySet()) {
            String adapterName = entry.getKey();
            DriverAdapter<?, ?> adapter = entry.getValue();
            if (adapter instanceof AutoCloseable autoCloseable) {
                try {
                    autoCloseable.close();
                } catch (Exception e) {
                    logger.error("Error closing adapter " + adapterName + ": " + e.getMessage(), e);
                }
            }
        }
    }

    public String getCycleSummary() {
        return this.getActivityDef().getCycleSummary();
    }

    public RateLimiter getCycleLimiter() {
        if (cycleLimiterSource != null) {
            return cycleLimiterSource.get();
        } else {
            return null;
        }
    }

    public synchronized RateLimiter getStrideLimiter() {
        if (strideLimiterSource != null) {
            return strideLimiterSource.get();
        } else {
            return null;
        }
    }

    public synchronized ActivityInstrumentation getInstrumentation() {
        if (null == this.activityInstrumentation) {
            activityInstrumentation = new ComponentActivityInstrumentation(this);
        }
        return activityInstrumentation;
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

    public int getMaxTries() {
        return this.activityDef.getParams().getOptionalInteger("maxtries").orElse(10);
    }

    public int getHdrDigits() {
        return this.getParams().getOptionalInteger("hdr_digits").orElse(4);
    }

    public RunStateTally getRunStateTally() {
        return tally;
    }

    public int compareTo(Activity o) {
        return getAlias().compareTo(o.getAlias());
    }

    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
        // Update from StandardActivity
        for (DriverAdapter<?, ?> adapter : adapters.values()) {
            if (adapter instanceof NBReconfigurable configurable) {
                NBConfigModel cfgModel = configurable.getReconfigModel();
                NBConfiguration cfg = cfgModel.matchConfig(activityDef.getParams());
                NBReconfigurable.applyMatching(cfg, List.of(configurable));
            }
        }
    }

    public synchronized ProgressMeterDisplay getProgressMeter() {
        if (null == this.progressMeter) {
            this.progressMeter = new ActivityMetricProgressMeter((Activity) this);
        }
        return this.progressMeter;
    }

    public Map<String, String> asResult() {
        return Map.of("activity", this.getAlias());
    }

    @Override
    public List<OpTemplate> getSyntheticOpTemplates(OpsDocList opsDocList, Map<String, Object> cfg) {
        List<OpTemplate> opTemplates = new ArrayList<>();
        for (DriverAdapter<?, ?> adapter : adapters.values()) {
            if (adapter instanceof SyntheticOpTemplateProvider sotp) {
                List<OpTemplate> newTemplates = sotp.getSyntheticOpTemplates(opsDocList, cfg);
                opTemplates.addAll(newTemplates);
            }
        }
        return opTemplates;
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

    // Helper methods from SimpleActivity

    public synchronized NBErrorHandler getErrorHandler() {
        if (null == this.errorHandler) {
            errorHandler = new NBErrorHandler(
                () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
                this::getExceptionMetrics);
        }
        return errorHandler;
    }

    public synchronized void initOrUpdateRateLimiters(ActivityDef activityDef) {
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

    public synchronized void setDefaultsFromOpSequence(OpSequence<?> seq) {
        Optional<String> strideOpt = getParams().getOptionalString("stride");
        if (strideOpt.isEmpty()) {
            String stride = String.valueOf(seq.getSequence().length);
            logger.info(() -> "defaulting stride to " + stride + " (the sequence length)");
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
                activityDef.getParams().setSilently("threads", threads);
            } else if (spec.toLowerCase().matches("\\d+x")) {
                String multiplier = spec.substring(0, spec.length() - 1);
                int threads = processors * Integer.parseInt(multiplier);
                logger.info(() -> "setting threads to " + threads + " (" + multiplier + "x)");
                activityDef.getParams().setSilently("threads", threads);
            } else if (spec.toLowerCase().matches("\\d+")) {
                logger.info(() -> "setting threads to " + spec + " (direct)");
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

    protected <O extends LongFunction> OpSequence<OpDispenser<? extends CycleOp<?>>> createOpSourceFromParsedOps(
        List<DriverAdapter<CycleOp<?>, Space>> adapters,
        List<ParsedOp> pops
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
                    dispenser = OpFunctionComposition.wrapOptionally(adapter, dispenser, pop, dryrun);

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

    protected List<OpTemplate> loadOpTemplates(DriverAdapter<?, ?> defaultDriverAdapter) {
        String tagfilter = activityDef.getParams().getOptionalString("tags").orElse("");

        OpsDocList opsDocList = loadStmtsDocList();

        List<OpTemplate> unfilteredOps = opsDocList.getOps(false);
        List<OpTemplate> filteredOps = opsDocList.getOps(tagfilter, true);

        if (filteredOps.isEmpty()) {
            if (!unfilteredOps.isEmpty()) {
                String message = "There were no active op templates with tag filter '" + tagfilter + "', since all " +
                    unfilteredOps.size() + " were filtered out. Examine the session log for details";
                NBAdvisorOutput.test(message);
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

    protected OpsDocList loadStmtsDocList() {
        try {
            String op = activityDef.getParams().getOptionalString("op").orElse(null);
            String stmt = activityDef.getParams().getOptionalString("stmt", "statement").orElse(null);
            String workload = activityDef.getParams().getOptionalString("workload").orElse(null);

            if ((op != null ? 1 : 0) + (stmt != null ? 1 : 0) + (workload != null ? 1 : 0) > 1) {
                throw new OpConfigError("Only op, statement, or workload may be provided, not more than one.");
            }

            logger.debug("loadStmtsDocList #1");
            if (workload != null && OpsLoader.isJson(workload)) {
                workloadSource = "commandline: (workload/json):" + workload;
                return OpsLoader.loadString(workload, OpTemplateFormat.json, activityDef.getParams(), null);
            } else if (workload != null && OpsLoader.isYaml(workload)) {
                workloadSource = "commandline: (workload/yaml):" + workload;
                return OpsLoader.loadString(workload, OpTemplateFormat.yaml, activityDef.getParams(), null);
            } else if (workload != null) {
                return OpsLoader.loadPath(workload, activityDef.getParams(), "activities");
            }

            logger.debug("loadStmtsDocList #2");
            if (stmt != null) {
                workloadSource = "commandline: (stmt/inline): '" + stmt + "'";
                return OpsLoader.loadString(stmt, OpTemplateFormat.inline, activityDef.getParams(), null);
            }

            logger.debug("loadStmtsDocList #3");
            if (op != null && OpsLoader.isJson(op)) {
                workloadSource = "commandline: (op/json): '" + op + "'";
                return OpsLoader.loadString(op, OpTemplateFormat.json, activityDef.getParams(), null);
            } else if (op != null) {
                workloadSource = "commandline: (op/inline): '" + op + "'";
                return OpsLoader.loadString(op, OpTemplateFormat.inline, activityDef.getParams(), null);
            }

            return OpsDocList.none();

        } catch (Exception e) {
            throw new OpConfigError("Error loading op templates: " + e, workloadSource, e);
        }
    }

    public OpSequence<OpDispenser<? extends CycleOp<?>>> getOpSequence() {
        return sequence;
    }

    public String toString() {
        return (activityDef != null ? activityDef.getAlias() : "unset_alias") + ':' + this.runState + ':' + this.tally;
    }
}