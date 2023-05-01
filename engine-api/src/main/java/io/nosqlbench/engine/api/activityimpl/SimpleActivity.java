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

import com.codahale.metrics.Timer;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.errors.OpConfigError;
import io.nosqlbench.engine.api.activityapi.core.*;
import io.nosqlbench.engine.api.activityapi.core.progress.ActivityMetricProgressMeter;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressCapable;
import io.nosqlbench.engine.api.activityapi.core.progress.ProgressMeterDisplay;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.IntPredicateDispenser;
import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityapi.planning.SequencePlanner;
import io.nosqlbench.engine.api.activityapi.planning.SequencerType;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiters;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateSpec;
import io.nosqlbench.engine.api.activityconfig.OpsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplateFormat;
import io.nosqlbench.engine.api.activityconfig.yaml.OpsDocList;
import io.nosqlbench.engine.api.activityimpl.motor.RunStateTally;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DryRunOpDispenserWrapper;
import io.nosqlbench.engine.api.activityimpl.uniform.decorators.SyntheticOpTemplateProvider;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.AnnotatedType;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A default implementation of an Activity, suitable for building upon.
 */
public class SimpleActivity implements Activity, NBLabeledElement {
    private static final Logger logger = LogManager.getLogger("ACTIVITY");
    private final NBLabeledElement parentLabels;

    protected ActivityDef activityDef;
    private final List<AutoCloseable> closeables = new ArrayList<>();
    private MotorDispenser motorDispenser;
    private InputDispenser inputDispenser;
    private ActionDispenser actionDispenser;
    private OutputDispenser markerDispenser;
    private IntPredicateDispenser resultFilterDispenser;
    private RunState runState = RunState.Uninitialized;
    private RateLimiter strideLimiter;
    private RateLimiter cycleLimiter;
    private RateLimiter phaseLimiter;
    private ActivityController activityController;
    private ActivityInstrumentation activityInstrumentation;
    private PrintWriter console;
    private long startedAtMillis;
    private int nameEnumerator;
    private ErrorMetrics errorMetrics;
    private NBErrorHandler errorHandler;
    private ActivityMetricProgressMeter progressMeter;
    private String workloadSource = "unspecified";
    private final RunStateTally tally = new RunStateTally();

    public SimpleActivity(final ActivityDef activityDef, final NBLabeledElement parentLabels) {
        this.activityDef = activityDef;
        this.parentLabels = parentLabels;
        if (activityDef.getAlias().equals(ActivityDef.DEFAULT_ALIAS)) {
            final Optional<String> workloadOpt = activityDef.getParams().getOptionalString(
                "workload",
                "yaml"
            );
            if (workloadOpt.isPresent()) activityDef.getParams().set("alias", workloadOpt.get());
            else {
                activityDef.getParams().set("alias",
                    activityDef.getActivityType().toUpperCase(Locale.ROOT)
                        + this.nameEnumerator);
                this.nameEnumerator++;
            }
        }
    }

    public SimpleActivity(final String activityDefString, final NBLabeledElement parentLabels) {
        this(ActivityDef.parseActivityDef(activityDefString),parentLabels);
    }

    @Override
    public synchronized void initActivity() {
        this.initOrUpdateRateLimiters(activityDef);
    }

    public synchronized NBErrorHandler getErrorHandler() {
        if (null == errorHandler) this.errorHandler = new NBErrorHandler(
            () -> this.activityDef.getParams().getOptionalString("errors").orElse("stop"),
            () -> this.getExceptionMetrics());
        return this.errorHandler;
    }

    @Override
    public synchronized RunState getRunState() {
        return this.runState;
    }

    @Override
    public synchronized void setRunState(final RunState runState) {
        this.runState = runState;
        if (RunState.Running == runState) startedAtMillis = System.currentTimeMillis();
    }

    @Override
    public long getStartedAtMillis() {
        return this.startedAtMillis;
    }

    @Override
    public final MotorDispenser getMotorDispenserDelegate() {
        return this.motorDispenser;
    }

    @Override
    public final void setMotorDispenserDelegate(final MotorDispenser motorDispenser) {
        this.motorDispenser = motorDispenser;
    }

    @Override
    public final InputDispenser getInputDispenserDelegate() {
        return this.inputDispenser;
    }

    @Override
    public final void setInputDispenserDelegate(final InputDispenser inputDispenser) {
        this.inputDispenser = inputDispenser;
    }

    @Override
    public final ActionDispenser getActionDispenserDelegate() {
        return this.actionDispenser;
    }

    @Override
    public final void setActionDispenserDelegate(final ActionDispenser actionDispenser) {
        this.actionDispenser = actionDispenser;
    }

    @Override
    public IntPredicateDispenser getResultFilterDispenserDelegate() {
        return this.resultFilterDispenser;
    }

    @Override
    public void setResultFilterDispenserDelegate(final IntPredicateDispenser resultFilterDispenser) {
        this.resultFilterDispenser = resultFilterDispenser;
    }

    @Override
    public OutputDispenser getMarkerDispenserDelegate() {
        return markerDispenser;
    }

    @Override
    public void setOutputDispenserDelegate(final OutputDispenser outputDispenser) {
        markerDispenser = outputDispenser;
    }

    @Override
    public ActivityDef getActivityDef() {
        return this.activityDef;
    }

    public String toString() {
        return this.getAlias() + ':' + runState + ':' + tally.toString();
    }

    @Override
    public int compareTo(final Activity o) {
        return this.getAlias().compareTo(o.getAlias());
    }

    @Override
    public ActivityController getActivityController() {
        return this.activityController;
    }

    @Override
    public void setActivityController(final ActivityController activityController) {
        this.activityController = activityController;

    }

    @Override
    public void registerAutoCloseable(final AutoCloseable closeable) {
        closeables.add(closeable);
    }

    @Override
    public void closeAutoCloseables() {
        for (final AutoCloseable closeable : this.closeables) {
            SimpleActivity.logger.debug(() -> "CLOSING " + closeable.getClass().getCanonicalName() + ": " + closeable);
            try {
                closeable.close();
            } catch (final Exception e) {
                throw new RuntimeException("Error closing " + closeable + ": " + e, e);
            }
        }
        this.closeables.clear();
    }

    @Override
    public RateLimiter getCycleLimiter() {
        return cycleLimiter;
    }

    @Override
    public synchronized void setCycleLimiter(final RateLimiter rateLimiter) {
        cycleLimiter = rateLimiter;
    }

    @Override
    public synchronized RateLimiter getCycleRateLimiter(final Supplier<? extends RateLimiter> s) {
        if (null == cycleLimiter) this.cycleLimiter = s.get();
        return this.cycleLimiter;
    }

    @Override
    public synchronized RateLimiter getStrideLimiter() {
        return strideLimiter;
    }

    @Override
    public synchronized void setStrideLimiter(final RateLimiter rateLimiter) {
        strideLimiter = rateLimiter;
    }

    @Override
    public synchronized RateLimiter getStrideRateLimiter(final Supplier<? extends RateLimiter> s) {
        if (null == strideLimiter) this.strideLimiter = s.get();
        return this.strideLimiter;
    }

    @Override
    public RateLimiter getPhaseLimiter() {
        return this.phaseLimiter;
    }


    @Override
    public Timer getResultTimer() {
        return ActivityMetrics.timer(activityDef, "result", this.getParams().getOptionalInteger("hdr_digits").orElse(4));
    }

    @Override
    public void setPhaseLimiter(final RateLimiter rateLimiter) {
        phaseLimiter = rateLimiter;
    }

    @Override
    public synchronized RateLimiter getPhaseRateLimiter(final Supplier<? extends RateLimiter> supplier) {
        if (null == phaseLimiter) this.phaseLimiter = supplier.get();
        return this.phaseLimiter;
    }

    @Override
    public synchronized ActivityInstrumentation getInstrumentation() {
        if (null == activityInstrumentation) this.activityInstrumentation = new CoreActivityInstrumentation(this);
        return this.activityInstrumentation;
    }

    @Override
    public synchronized PrintWriter getConsoleOut() {
        if (null == this.console) console = new PrintWriter(System.out, false, StandardCharsets.UTF_8);
        return console;
    }

    @Override
    public synchronized InputStream getConsoleIn() {
        return System.in;
    }

    @Override
    public void setConsoleOut(final PrintWriter writer) {
        console = writer;
    }

    @Override
    public synchronized ErrorMetrics getExceptionMetrics() {
        if (null == errorMetrics) this.errorMetrics = new ErrorMetrics(activityDef);
        return this.errorMetrics;
    }

    @Override
    public synchronized void onActivityDefUpdate(final ActivityDef activityDef) {
        this.initOrUpdateRateLimiters(activityDef);
    }

    public synchronized void initOrUpdateRateLimiters(final ActivityDef activityDef) {

        activityDef.getParams().getOptionalNamedParameter("striderate")
            .map(RateSpec::new)
            .ifPresent(spec -> this.strideLimiter = RateLimiters.createOrUpdate(activityDef, "strides", this.strideLimiter, spec));

        activityDef.getParams().getOptionalNamedParameter("cyclerate", "targetrate", "rate")
            .map(RateSpec::new).ifPresent(
                spec -> this.cycleLimiter = RateLimiters.createOrUpdate(activityDef, "cycles", this.cycleLimiter, spec));

        activityDef.getParams().getOptionalNamedParameter("phaserate")
            .map(RateSpec::new)
            .ifPresent(spec -> this.phaseLimiter = RateLimiters.createOrUpdate(activityDef, "phases", this.phaseLimiter, spec));

    }

    /**
     * Modify the provided ActivityDef with defaults for stride and cycles, if they haven't been provided, based on the
     * length of the sequence as determined by the provided ratios. Also, modify the ActivityDef with reasonable
     * defaults when requested.
     *
     * @param seq - The {@link OpSequence} to derive the defaults from
     */
    public synchronized void setDefaultsFromOpSequence(final OpSequence<?> seq) {
        final Optional<String> strideOpt = this.getParams().getOptionalString("stride");
        if (strideOpt.isEmpty()) {
            final String stride = String.valueOf(seq.getSequence().length);
            SimpleActivity.logger.info(() -> "defaulting stride to " + stride + " (the sequence length)");
//            getParams().set("stride", stride);
            this.getParams().setSilently("stride", stride);
        }

        final Optional<String> cyclesOpt = this.getParams().getOptionalString("cycles");
        if (cyclesOpt.isEmpty()) {
            final String cycles = this.getParams().getOptionalString("stride").orElseThrow();
            SimpleActivity.logger.info(() -> "defaulting cycles to " + cycles + " (the stride length)");
//            getParams().set("cycles", getParams().getOptionalString("stride").orElseThrow());
            this.getParams().setSilently("cycles", this.getParams().getOptionalString("stride").orElseThrow());
        } else {
            if (0 == getActivityDef().getCycleCount()) throw new RuntimeException(
                "You specified cycles, but the range specified means zero cycles: " + this.getParams().get("cycles")
            );
            final long stride = this.getParams().getOptionalLong("stride").orElseThrow();
            final long cycles = activityDef.getCycleCount();
            if (cycles < stride) throw new RuntimeException(
                "The specified cycles (" + cycles + ") are less than the stride (" + stride + "). This means there aren't enough cycles to cause a stride to be executed." +
                    " If this was intended, then set stride low enough to allow it."
            );
        }

        final long cycleCount = activityDef.getCycleCount();
        final long stride = activityDef.getParams().getOptionalLong("stride").orElseThrow();

        if ((0 < stride) && (0 != (cycleCount % stride)))
            SimpleActivity.logger.warn(() -> "The stride does not evenly divide cycles. Only full strides will be executed," +
                "leaving some cycles unused. (stride=" + stride + ", cycles=" + cycleCount + ')');

        final Optional<String> threadSpec = this.activityDef.getParams().getOptionalString("threads");
        if (threadSpec.isPresent()) {
            final String spec = threadSpec.get();
            final int processors = Runtime.getRuntime().availableProcessors();
            if ("auto".equalsIgnoreCase(spec)) {
                int threads = processors * 10;
                if (threads > this.activityDef.getCycleCount()) {
                    threads = (int) this.activityDef.getCycleCount();
                    SimpleActivity.logger.info("setting threads to {} (auto) [10xCORES, cycle count limited]", threads);
                } else SimpleActivity.logger.info("setting threads to {} (auto) [10xCORES]", threads);
//                activityDef.setThreads(threads);
                this.activityDef.getParams().setSilently("threads", threads);
            } else if (spec.toLowerCase().matches("\\d+x")) {
                final String multiplier = spec.substring(0, spec.length() - 1);
                final int threads = processors * Integer.parseInt(multiplier);
                SimpleActivity.logger.info(() -> "setting threads to " + threads + " (" + multiplier + "x)");
//                activityDef.setThreads(threads);
                this.activityDef.getParams().setSilently("threads", threads);
            } else if (spec.toLowerCase().matches("\\d+")) {
                SimpleActivity.logger.info(() -> "setting threads to " + spec + " (direct)");
//                activityDef.setThreads(Integer.parseInt(spec));
                this.activityDef.getParams().setSilently("threads", Integer.parseInt(spec));
            }

            if (this.activityDef.getThreads() > this.activityDef.getCycleCount())
                SimpleActivity.logger.warn(() -> "threads=" + this.activityDef.getThreads() + " and cycles=" + this.activityDef.getCycleSummary()
                    + ", you should have more cycles than threads.");

        } else if (1000 < cycleCount)
            SimpleActivity.logger.warn(() -> "For testing at scale, it is highly recommended that you " +
                "set threads to a value higher than the default of 1." +
                " hint: you can use threads=auto for reasonable default, or" +
                " consult the topic on threads with `help threads` for" +
                " more information.");

        if ((0 < activityDef.getCycleCount()) && (0 == seq.getOps().size()))
            throw new BasicError("You have configured a zero-length sequence and non-zero cycles. Tt is not possible to continue with this activity.");
    }

    /**
     * Given a function that can create an op of type <O> from a CommandTemplate, generate
     * an indexed sequence of ready to call operations.
     *
     * This method works almost exactly like the ,
     * except that it uses the {@link CommandTemplate} semantics, which are more general and allow
     * for map-based specification of operations with bindings in each field.
     *
     * It is recommended to use the CommandTemplate form
     * than the
     *
     * @param <O>
     * @param opinit
     * @param strict
     * @return
     */
    protected <O extends Op> OpSequence<OpDispenser<? extends O>> createOpSequenceFromCommands(
        final Function<CommandTemplate, OpDispenser<O>> opinit,
        final boolean strict
    ) {
        final Function<OpTemplate, CommandTemplate> f = CommandTemplate::new;
        final Function<OpTemplate, OpDispenser<? extends O>> opTemplateOFunction = f.andThen(opinit);

        return this.createOpSequence(opTemplateOFunction, strict, Optional.empty());
    }

    protected <O extends Op> OpSequence<OpDispenser<? extends O>> createOpSourceFromParsedOps(
        final Map<String, DriverAdapter> adapterCache,
        final Map<String, OpMapper<Op>> mapperCache,
        final List<DriverAdapter> adapters,
        final List<ParsedOp> pops
    ) {
        try {

            final List<Long> ratios = new ArrayList<>(pops.size());

            for (int i = 0; i < pops.size(); i++) {
                final ParsedOp pop = pops.get(i);
                final long ratio = pop.takeStaticConfigOr("ratio", 1);
                ratios.add(ratio);
            }

            final SequencerType sequencerType = this.getParams()
                .getOptionalString("seq")
                .map(SequencerType::valueOf)
                .orElse(SequencerType.bucket);
            final SequencePlanner<OpDispenser<? extends O>> planner = new SequencePlanner<>(sequencerType);

            int dryrunCount = 0;
            for (int i = 0; i < pops.size(); i++) {
                final long ratio = ratios.get(i);
                final ParsedOp pop = pops.get(i);
                if (0 == ratio) {
                    SimpleActivity.logger.info(() -> "skipped mapping op '" + pop.getName() + '\'');
                    continue;
                }
                final String dryrunSpec = pop.takeStaticConfigOr("dryrun", "none");
                final boolean dryrun = "op".equalsIgnoreCase(dryrunSpec);

                final DriverAdapter adapter = adapters.get(i);
                final OpMapper opMapper = adapter.getOpMapper();
                OpDispenser<? extends Op> dispenser = opMapper.apply(pop);

                if (dryrun) {
                    dispenser = new DryRunOpDispenserWrapper(adapter, pop, dispenser);
                    dryrunCount++;
                }

//                if (strict) {
//                    optemplate.assertConsumed();
//                }
                planner.addOp((OpDispenser<? extends O>) dispenser, ratio);
            }
            if (0 < dryrunCount)
                SimpleActivity.logger.warn("initialized {} op templates for dry run only. These ops will be synthesized for each cycle, but will not be executed.", dryrunCount);


            return planner.resolve();

        } catch (final Exception e) {
            throw new OpConfigError(e.getMessage(), this.workloadSource, e);
        }


    }


    protected <O extends Op> OpSequence<OpDispenser<? extends O>> createOpSourceFromCommands(
        final Function<ParsedOp, OpDispenser<? extends O>> opinit,
        final NBConfiguration cfg,
        final List<Function<Map<String, Object>, Map<String, Object>>> parsers,
        final boolean strict
    ) {
        final Function<OpTemplate, ParsedOp> f = t -> new ParsedOp(t, cfg, parsers, this);
        final Function<OpTemplate, OpDispenser<? extends O>> opTemplateOFunction = f.andThen(opinit);

        return this.createOpSequence(opTemplateOFunction, strict, Optional.empty());
    }

    protected List<ParsedOp> loadParsedOps(final NBConfiguration cfg, final Optional<DriverAdapter> defaultAdapter) {
        final List<ParsedOp> parsedOps = this.loadOpTemplates(defaultAdapter).stream().map(
            ot -> new ParsedOp(ot, cfg, List.of(), this)
        ).toList();
        return parsedOps;
    }

    protected List<OpTemplate> loadOpTemplates(final Optional<DriverAdapter> defaultDriverAdapter) {

        final String tagfilter = this.activityDef.getParams().getOptionalString("tags").orElse("");

        final OpsDocList opsDocList = this.loadStmtsDocList();

        final List<OpTemplate> unfilteredOps = opsDocList.getOps();
        List<OpTemplate> filteredOps = opsDocList.getOps(tagfilter);

        if (0 == filteredOps.size()) {
            // There were no ops, and it *wasn't* because they were all filtered out.
            // In this case, let's try to synthesize the ops as long as at least a default driver was provided
            // But if there were no ops, and there was no default driver provided, we can't continue
            // There were no ops, and it was because they were all filtered out
            if (0 < unfilteredOps.size()) throw new BasicError("There were no active op templates with tag filter '"
                + tagfilter + "', since all " + unfilteredOps.size() + " were filtered out.");
            if (defaultDriverAdapter.isPresent() && (defaultDriverAdapter.get() instanceof SyntheticOpTemplateProvider sotp)) {
                filteredOps = sotp.getSyntheticOpTemplates(opsDocList, activityDef.getParams());
                Objects.requireNonNull(filteredOps);
                if (0 == filteredOps.size())
                    throw new BasicError("Attempted to create synthetic ops from driver '" + defaultDriverAdapter.get().getAdapterName() + '\'' +
                        " but no ops were created. You must provide either a workload or an op parameter. Activities require op templates.");
            } else throw new BasicError("""
                No op templates were provided. You must provide one of these activity parameters:
                1) workload=some.yaml
                2) op='inline template'
                3) driver=stdout (or any other drive that can synthesize ops)""");
            if (0 == filteredOps.size())
                throw new BasicError("There were no active op templates with tag filter '" + tagfilter + '\'');
        }

        if (0 == filteredOps.size())
            throw new OpConfigError("No op templates found. You must provide either workload=... or op=..., or use " +
                "a default driver (driver=___). This includes " +
                ServiceLoader.load(DriverAdapter.class).stream()
                    .filter(p -> {
                        final AnnotatedType[] annotatedInterfaces = p.type().getAnnotatedInterfaces();
                        for (final AnnotatedType ai : annotatedInterfaces)
                            if (ai.getType().equals(SyntheticOpTemplateProvider.class)) return true;
                        return false;
                    })
                    .map(d -> d.get().getAdapterName())
                    .collect(Collectors.joining(",")));

        return filteredOps;
    }

    /**
     * Given a function that can create an op of type <O> from an OpTemplate, generate
     * an indexed sequence of ready to call operations.
     *
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
     * @param <O>            A holder for an executable operation for the native driver used by this activity.
     * @param opinit         A function to map an OpTemplate to the executable operation form required by
     *                       the native driver for this activity.
     * @param defaultAdapter
     * @return The sequence of operations as determined by filtering and ratios
     */
    @Deprecated(forRemoval = true)
    protected <O> OpSequence<OpDispenser<? extends O>> createOpSequence(final Function<OpTemplate, OpDispenser<? extends O>> opinit, final boolean strict, final Optional<DriverAdapter> defaultAdapter) {

        final var stmts = this.loadOpTemplates(defaultAdapter);

        final List<Long> ratios = new ArrayList<>(stmts.size());

        for (int i = 0; i < stmts.size(); i++) {
            final OpTemplate opTemplate = stmts.get(i);
            final long ratio = opTemplate.removeParamOrDefault("ratio", 1);
            ratios.add(ratio);
        }

        final SequencerType sequencerType = this.getParams()
            .getOptionalString("seq")
            .map(SequencerType::valueOf)
            .orElse(SequencerType.bucket);
        final SequencePlanner<OpDispenser<? extends O>> planner = new SequencePlanner<>(sequencerType);

        try {
            for (int i = 0; i < stmts.size(); i++) {
                final long ratio = ratios.get(i);
                final OpTemplate optemplate = stmts.get(i);
                final OpDispenser<? extends O> driverSpecificReadyOp = opinit.apply(optemplate);
                if (strict) optemplate.assertConsumed();
                planner.addOp(driverSpecificReadyOp, ratio);
            }
        } catch (final Exception e) {
            throw new OpConfigError(e.getMessage(), this.workloadSource, e);
        }

        return planner.resolve();
    }

    protected OpsDocList loadStmtsDocList() {

        try {
            final Optional<String> stmt = this.activityDef.getParams().getOptionalString("op", "stmt", "statement");
            final Optional<String> op_yaml_loc = this.activityDef.getParams().getOptionalString("yaml", "workload");
            if (stmt.isPresent()) {
                this.workloadSource = "commandline:" + stmt.get();
                return OpsLoader.loadString(stmt.get(), OpTemplateFormat.inline, this.activityDef.getParams(), null);
            }
            if (op_yaml_loc.isPresent()) {
                this.workloadSource = "yaml:" + op_yaml_loc.get();
                return OpsLoader.loadPath(op_yaml_loc.get(), this.activityDef.getParams(), "activities");
            }

            return OpsDocList.none();

        } catch (final Exception e) {
            throw new OpConfigError("Error loading op templates: " + e, this.workloadSource, e);
        }

    }

    @Override
    public synchronized ProgressMeterDisplay getProgressMeter() {
        if (null == progressMeter) progressMeter = new ActivityMetricProgressMeter(this);
        return progressMeter;
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
        return activityDef.getParams().getOptionalInteger("maxtries").orElse(10);
    }

    @Override
    public RunStateTally getRunStateTally() {
        return this.tally;
    }


    @Override
    public String getName() {
        return activityDef.getAlias();
    }

    @Override
    public Map<String, String> getLabels() {
        return this.parentLabels.getLabelsAnd("alias", this.activityDef.getAlias());
    }
}
