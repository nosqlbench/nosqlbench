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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.EmitterOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import io.nosqlbench.engine.core.lifecycle.scenario.container.InvokableResult;
import io.nosqlbench.nb.api.components.core.NBComponent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.core.progress.ActivityMetricProgressMeter;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityapi.simrate.RateLimiters;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.adapters.api.activityimpl.OpDispenser;
import io.nosqlbench.adapters.api.activityimpl.OpMapper;
import io.nosqlbench.nb.api.components.status.NBStatusComponent;
import io.nosqlbench.nb.api.labels.NBLabels;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IntPredicateDispenser;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.simrate.RateLimiter;
import io.nosqlbench.adapters.api.activityconfig.OpsLoader;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.adapters.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.activityapi.simrate.StrideRateSpec;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.activityimpl.uniform.opwrappers.DryRunOpDispenserWrapper;
import io.nosqlbench.adapters.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A default implementation of an Activity, suitable for building upon.
 */
public class SimpleActivity extends NBStatusComponent implements Activity, InvokableResult {
    private static final Logger logger = LogManager.getLogger("ACTIVITY");

    protected ActivityDef activityDef;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private MotorDispenser<?> motorDispenser;
    private InputDispenser inputDispenser;
    private ActionDispenser actionDispenser;
    private OutputDispenser markerDispenser;
    private IntPredicateDispenser resultFilterDispenser;
    private RunState runState = RunState.Uninitialized;
    private RateLimiter strideLimiter;
    private RateLimiter cycleLimiter;
    private ActivityInstrumentation activityInstrumentation;
    private PrintWriter console;
    private long startedAtMillis;
    private int nameEnumerator;
    private ErrorMetrics errorMetrics;
    private NBErrorHandler errorHandler;
    private ActivityMetricProgressMeter progressMeter;
    private String workloadSource = "unspecified";
    private final RunStateTally tally = new RunStateTally();

    public SimpleActivity(NBComponent parent, ActivityDef activityDef) {
        super(parent, NBLabels.forKV("activity", activityDef.getAlias()).and(activityDef.auxLabels()));
        this.activityDef = activityDef;
        if (activityDef.getAlias().equals(ActivityDef.DEFAULT_ALIAS)) {
            Optional<String> workloadOpt = activityDef.getParams().getOptionalString(
                "workload",
                "yaml"
            );
            if (workloadOpt.isPresent()) {
                activityDef.getParams().set("alias", workloadOpt.get());
            } else {
                activityDef.getParams().set("alias",
                    activityDef.getActivityType().toUpperCase(Locale.ROOT)
                        + nameEnumerator);
                nameEnumerator++;
            }
        }
    }

    public SimpleActivity(NBComponent parent, String activityDefString) {
        this(parent, ActivityDef.parseActivityDef(activityDefString));
    }

    @Override
    public synchronized void initActivity() {
        initOrUpdateRateLimiters(this.activityDef);
    }

    public synchronized NBErrorHandler getErrorHandler() {
        if (null == this.errorHandler) {
            errorHandler = new NBErrorHandler(
                () -> activityDef.getParams().getOptionalString("errors").orElse("stop"),
                this::getExceptionMetrics);
        }
        return errorHandler;
    }

    @Override
    public synchronized RunState getRunState() {
        return runState;
    }

    @Override
    public synchronized void setRunState(RunState runState) {
        this.runState = runState;
        if (RunState.Running == runState) {
            this.startedAtMillis = System.currentTimeMillis();
        }
    }

    @Override
    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    @Override
    public final MotorDispenser<?> getMotorDispenserDelegate() {
        return motorDispenser;
    }

    @Override
    public final void setMotorDispenserDelegate(MotorDispenser<?> motorDispenser) {
        this.motorDispenser = motorDispenser;
    }

    @Override
    public final InputDispenser getInputDispenserDelegate() {
        return inputDispenser;
    }

    @Override
    public final void setInputDispenserDelegate(InputDispenser inputDispenser) {
        this.inputDispenser = inputDispenser;
    }

    @Override
    public final ActionDispenser getActionDispenserDelegate() {
        return actionDispenser;
    }

    @Override
    public final void setActionDispenserDelegate(ActionDispenser actionDispenser) {
        this.actionDispenser = actionDispenser;
    }

    @Override
    public IntPredicateDispenser getResultFilterDispenserDelegate() {
        return resultFilterDispenser;
    }

    @Override
    public void setResultFilterDispenserDelegate(IntPredicateDispenser resultFilterDispenser) {
        this.resultFilterDispenser = resultFilterDispenser;
    }

    @Override
    public OutputDispenser getMarkerDispenserDelegate() {
        return this.markerDispenser;
    }

    @Override
    public void setOutputDispenserDelegate(OutputDispenser outputDispenser) {
        this.markerDispenser = outputDispenser;
    }

    @Override
    public ActivityDef getActivityDef() {
        return activityDef;
    }

    public String toString() {
        return (activityDef != null ? activityDef.getAlias() : "unset_alias") + ':' + this.runState + ':' + this.tally;
    }

    @Override
    public int compareTo(Activity o) {
        return getAlias().compareTo(o.getAlias());
    }

    @Override
    public void registerAutoCloseable(AutoCloseable closeable) {
        this.closeables.add(closeable);
    }

    @Override
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
    public RateLimiter getCycleLimiter() {
        return this.cycleLimiter;
    }

    @Override
    public synchronized void setCycleLimiter(RateLimiter rateLimiter) {
        this.cycleLimiter = rateLimiter;
    }

    @Override
    public synchronized RateLimiter getCycleRateLimiter(Supplier<? extends RateLimiter> s) {
        if (null == this.cycleLimiter) {
            cycleLimiter = s.get();
        }
        return cycleLimiter;
    }

    @Override
    public synchronized RateLimiter getStrideLimiter() {
        return this.strideLimiter;
    }

    @Override
    public synchronized void setStrideLimiter(RateLimiter rateLimiter) {
        this.strideLimiter = rateLimiter;
    }

    @Override
    public synchronized RateLimiter getStrideRateLimiter(Supplier<? extends RateLimiter> s) {
        if (null == this.strideLimiter) {
            strideLimiter = s.get();
        }
        return strideLimiter;
    }


    @Override
    public synchronized ActivityInstrumentation getInstrumentation() {
        if (null == this.activityInstrumentation) {
            activityInstrumentation = new ComponentActivityInstrumentation(this);
//            activityInstrumentation = new CoreActivityInstrumentation(this);
        }
        return activityInstrumentation;
    }

    @Override
    public synchronized PrintWriter getConsoleOut() {
        if (null == console) {
            this.console = new PrintWriter(System.out, false, StandardCharsets.UTF_8);
        }
        return this.console;
    }

    @Override
    public synchronized InputStream getConsoleIn() {
        return System.in;
    }

    @Override
    public void setConsoleOut(PrintWriter writer) {
        this.console = writer;
    }

    @Override
    public synchronized ErrorMetrics getExceptionMetrics() {
        if (null == this.errorMetrics) {
            errorMetrics = new ErrorMetrics(this);
        }
        return errorMetrics;
    }

    @Override
    public synchronized void onActivityDefUpdate(ActivityDef activityDef) {
//        initOrUpdateRateLimiters(activityDef);
    }

    public synchronized void initOrUpdateRateLimiters(ActivityDef activityDef) {

        activityDef.getParams().getOptionalNamedParameter("striderate")
            .map(StrideRateSpec::new).ifPresent(sr -> this.onEvent(new ParamChange<>(sr)));

        activityDef.getParams().getOptionalNamedParameter("cyclerate", "targetrate", "rate")
            .map(CycleRateSpec::new).ifPresent(sr -> this.onEvent(new ParamChange<>(sr)));

    }

    public void createOrUpdateStrideLimiter(SimRateSpec spec) {
        strideLimiter = RateLimiters.createOrUpdate(this, strideLimiter, spec);
    }

    public void createOrUpdateCycleLimiter(SimRateSpec spec) {
        cycleLimiter = RateLimiters.createOrUpdate(this, cycleLimiter, spec);
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
//            getParams().set("cycles", getParams().getOptionalString("stride").orElseThrow());
//            getParams().setSilently("cycles", getParams().getOptionalString("stride").orElseThrow());
            this.getActivityDef().setCycles(getParams().getOptionalString("stride").orElseThrow());
//            getParams().set("cycles", getParams().getOptionalString("stride").orElseThrow());
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
            throw new BasicError("You have configured a zero-length sequence and non-zero cycles. Tt is not possible to continue with this activity.");
        }
    }


    protected <O extends Op> OpSequence<OpDispenser<? extends O>> createOpSourceFromParsedOps(
//        Map<String, DriverAdapter<?,?>> adapterCache,
//        Map<String, OpMapper<? extends Op>> mapperCache,
        List<DriverAdapter<?, ?>> adapters,
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
            SequencePlanner<OpDispenser<? extends O>> planner = new SequencePlanner<>(sequencerType);

            int dryrunCount = 0;
            for (int i = 0; i < pops.size(); i++) {
                long ratio = ratios.get(i);
                ParsedOp pop = pops.get(i);

                try {
                    if (0 == ratio) {
                        logger.info(() -> "skipped mapping op '" + pop.getName() + '\'');
                        continue;
                    }

                    DriverAdapter<?, ?> adapter = adapters.get(i);
                    OpMapper<? extends Op> opMapper = adapter.getOpMapper();
                    OpDispenser<? extends Op> dispenser = opMapper.apply(pop);

                    String dryrunSpec = pop.takeStaticConfigOr("dryrun", "none");
                    if ("op".equalsIgnoreCase(dryrunSpec)) {
                        dispenser = new DryRunOpDispenserWrapper((DriverAdapter<Op, Object>) adapter, pop, dispenser);
                        dryrunCount++;
                    } else if ("emit".equalsIgnoreCase(dryrunSpec)) {
                        dispenser = new EmitterOpDispenserWrapper(
                            (DriverAdapter<Op, Object>) adapter,
                            pop,
                            (OpDispenser<? extends CycleOp<?>>) dispenser
                        );
                    }

//                if (strict) {
//                    optemplate.assertConsumed();
//                }
                    planner.addOp((OpDispenser<? extends O>) dispenser, ratio);
                } catch (Exception e) {
                    throw new OpConfigError("Error while mapping op from template named '" + pop.getName() + "': " + e.getMessage(), e);
                }
            }
            if (0 < dryrunCount) {
                logger.warn("initialized {} op templates for dry run only. These ops will be synthesized for each cycle, but will not be executed.", dryrunCount);
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
            // There were no ops, and it *wasn't* because they were all filtered out.
            // In this case, let's try to synthesize the ops as long as at least a default driver was provided
            // But if there were no ops, and there was no default driver provided, we can't continue
            // There were no ops, and it was because they were all filtered out
            if (!unfilteredOps.isEmpty()) {
                throw new BasicError("There were no active op templates with tag filter '"
                    + tagfilter + "', since all " + unfilteredOps.size() + " were filtered out.");
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
//        if (filteredOps.isEmpty()) {
//            throw new BasicError("There were no active op templates with tag filter '" + tagfilter + '\'');
//        }

//        if (filteredOps.isEmpty()) {
//            throw new OpConfigError("No op templates found. You must provide either workload=... or op=..., or use " +
//                "a default driver (driver=___). This includes " +
//                ServiceLoader.load(DriverAdapter.class).stream()
//                    .filter(p -> {
//                        AnnotatedType[] annotatedInterfaces = p.type().getAnnotatedInterfaces();
//                        for (AnnotatedType ai : annotatedInterfaces) {
//                            if (ai.getType().equals(SyntheticOpTemplateProvider.class)) {
//                                return true;
//                            }
//                        }
//                        return false;
//                    })
//                    .map(d -> d.get().getAdapterName())
//                    .collect(Collectors.joining(",")));
//        }
//
        return filteredOps;
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
    protected <O> OpSequence<OpDispenser<? extends O>> createOpSequence(Function<OpTemplate, OpDispenser<? extends O>> opinit, boolean strict, DriverAdapter<?, ?> defaultAdapter) {

        var stmts = loadOpTemplates(defaultAdapter);

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

    /**
     * Activities with retryable operations (when specified with the retry error handler for some
     * types of error), should allow the user to specify how many retries are allowed before
     * giving up on the operation.
     *
     * @return The number of allowable retries
     */
    @Override
    public int getMaxTries() {
        return this.activityDef.getParams().getOptionalInteger("maxtries").orElse(10);
    }

    @Override
    public RunStateTally getRunStateTally() {
        return tally;
    }


    @Override
    public Map<String, String> asResult() {
        return Map.of("activity",this.getAlias());
    }
}
