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
package io.nosqlbench.engine.core.lifecycle.scenario;

import com.codahale.metrics.MetricRegistry;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import io.nosqlbench.api.annotations.Annotation;
import io.nosqlbench.api.annotations.Layer;
import io.nosqlbench.api.config.NBLabeledElement;
import io.nosqlbench.api.config.NBLabels;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.metadata.ScenarioMetadata;
import io.nosqlbench.api.metadata.ScenarioMetadataAware;
import io.nosqlbench.api.metadata.SystemId;
import io.nosqlbench.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityProgressIndicator;
import io.nosqlbench.api.extensions.SandboxExtensionFinder;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScenarioContext;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScriptParams;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.ActivityBindings;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotMetricRegistryBindings;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotScenarioController;
import io.nosqlbench.nb.annotations.Maturity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine.Builder;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Scenario implements Callable<ExecutionMetricsResult>, NBLabeledElement {

    private final String commandLine;
    private final String reportSummaryTo;
    private final Path logsPath;
    private final Maturity minMaturity;
    private Logger logger = LogManager.getLogger("SCENARIO");

    private State state = State.Scheduled;
    private volatile ScenarioShutdownHook scenarioShutdownHook;
    private Exception error;
    private ScenarioMetadata scenarioMetadata;

    private ExecutionMetricsResult result;
    private final NBLabeledElement parentComponent;

    public Optional<ExecutionMetricsResult> getResultIfComplete() {
        return Optional.ofNullable(result);
    }


    @Override
    public NBLabels getLabels() {
        return this.parentComponent.getLabels().andTypes("scenario", this.scenarioName);
    }

    public enum State {
        Scheduled,
        Running,
        Errored,
        Interrupted,
        Finished
    }
    private final List<String> scripts = new ArrayList<>();
    private ScriptEngine scriptEngine;
    private ScenarioController scenarioController;
    private ActivityProgressIndicator activityProgressIndicator;
    private String progressInterval = "console:1m";
    private ScenarioContext scriptEnv;
    private final String scenarioName;
    private ScriptParams scenarioScriptParams;
    private final String scriptfile;
    private Engine engine = Engine.Graalvm;
    private final boolean wantsStackTraces;
    private final boolean wantsCompiledScript;
    private long startedAtMillis = -1L;
    private long endedAtMillis = -1L;

    public enum Engine {
        Graalvm
    }

    public Scenario(
        final String scenarioName,
        final String scriptfile,
        final Engine engine,
        final String progressInterval,
        final boolean wantsStackTraces,
        final boolean wantsCompiledScript,
        final String reportSummaryTo,
        final String commandLine,
        final Path logsPath,
        final Maturity minMaturity,
        NBLabeledElement parentComponent) {

        this.scenarioName = scenarioName;
        this.scriptfile = scriptfile;
        this.engine = engine;
        this.progressInterval = progressInterval;
        this.wantsStackTraces = wantsStackTraces;
        this.wantsCompiledScript = wantsCompiledScript;
        this.reportSummaryTo = reportSummaryTo;
        this.commandLine = commandLine;
        this.logsPath = logsPath;
        this.minMaturity = minMaturity;
        this.parentComponent = parentComponent;
    }

    public static Scenario forTesting(final String name, final Engine engine, final String reportSummaryTo, final Maturity minMaturity) {
        return new Scenario(name, null, engine, "console:10s", true, true, reportSummaryTo, "", Path.of("logs"), minMaturity, NBLabeledElement.forKV("test-name", "name"));
    }

    //    public Scenario(final String name, final Engine engine, final String reportSummaryTo, final Maturity minMaturity) {
//        scenarioName = name;
//        this.reportSummaryTo = reportSummaryTo;
//        this.engine = engine;
//        commandLine = "";
//        this.minMaturity = minMaturity;
//        logsPath = Path.of("logs");
//    }
//
    public Scenario setLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Scenario addScriptText(final String scriptText) {
        this.scripts.add(scriptText);
        return this;
    }


    public Scenario addScriptFiles(final String... args) {
        for (final String scriptFile : args) {
            final Path scriptPath = Paths.get(scriptFile);
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(scriptPath);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            final ByteBuffer bb = ByteBuffer.wrap(bytes);
            final Charset utf8 = StandardCharsets.UTF_8;
            final String scriptData = utf8.decode(bb).toString();
            this.addScriptText(scriptData);
        }
        return this;
    }

    private void initializeScriptingEngine(final ScenarioController scenarioController) {

        this.logger.debug("Using engine {}", this.engine.toString());
        final MetricRegistry metricRegistry = ActivityMetrics.getMetricRegistry();

        final Context.Builder contextSettings = Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowNativeAccess(true)
            .allowCreateThread(true)
            .allowIO(true)
            .allowHostClassLookup(s -> true)
            .allowHostClassLoading(true)
            .allowCreateProcess(true)
            .allowAllAccess(true)
            .allowEnvironmentAccess(EnvironmentAccess.INHERIT)
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .option("js.ecmascript-version", "2022")
            .option("js.nashorn-compat", "true");

        final Builder engineBuilder = org.graalvm.polyglot.Engine.newBuilder();
        engineBuilder.option("engine.WarnInterpreterOnly", "false");
        final org.graalvm.polyglot.Engine polyglotEngine = engineBuilder.build();

        // TODO: add in, out, err for this scenario
        scriptEngine = GraalJSScriptEngine.create(polyglotEngine, contextSettings);


        if (!"disabled".equals(progressInterval))
            this.activityProgressIndicator = new ActivityProgressIndicator(scenarioController, this.progressInterval);

        this.scriptEnv = new ScenarioContext(scenarioName, scenarioController);
        this.scriptEngine.setContext(this.scriptEnv);

        this.scriptEngine.put("params", this.scenarioScriptParams);
        this.scriptEngine.put("scenario", new PolyglotScenarioController(scenarioController));
        this.scriptEngine.put("metrics", new PolyglotMetricRegistryBindings(metricRegistry));
        this.scriptEngine.put("activities", new ActivityBindings(scenarioController));

        for (final ScriptingPluginInfo<?> extensionDescriptor : SandboxExtensionFinder.findAll()) {
            if (!extensionDescriptor.isAutoLoading()) {
                this.logger.info(() -> "Not loading " + extensionDescriptor + ", autoloading is false");
                continue;
            }

            final Logger extensionLogger =
                LogManager.getLogger("extensions." + extensionDescriptor.getBaseVariableName());
            final Object extensionObject = extensionDescriptor.getExtensionObject(
                extensionLogger,
                metricRegistry,
                this.scriptEnv
            );
            ScenarioMetadataAware.apply(extensionObject, this.getScenarioMetadata());
            this.logger.trace(() -> "Adding extension object:  name=" + extensionDescriptor.getBaseVariableName() +
                " class=" + extensionObject.getClass().getSimpleName());
            this.scriptEngine.put(extensionDescriptor.getBaseVariableName(), extensionObject);
        }
    }

    private synchronized ScenarioMetadata getScenarioMetadata() {
        if (null == this.scenarioMetadata) scenarioMetadata = new ScenarioMetadata(
            startedAtMillis,
            scenarioName,
            SystemId.getNodeId(),
            SystemId.getNodeFingerprint()
        );
        return this.scenarioMetadata;
    }

    private synchronized void runScenario() {
        this.scenarioShutdownHook = new ScenarioShutdownHook(this);
        Runtime.getRuntime().addShutdownHook(this.scenarioShutdownHook);

        this.state = State.Running;
        this.startedAtMillis = System.currentTimeMillis();
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .session(scenarioName)
                .now()
                .layer(Layer.Scenario)
                .detail("engine", engine.toString())
                .build()
        );

        this.logger.debug("Running control script for {}.", scenarioName);
        this.scenarioController = new ScenarioController(this);
        try {
            this.initializeScriptingEngine(this.scenarioController);
            this.executeScenarioScripts();
            final long awaitCompletionTime = 86400 * 365 * 1000L;
            this.logger.debug("Awaiting completion of scenario and activities for {} millis.", awaitCompletionTime);
            this.scenarioController.awaitCompletion(awaitCompletionTime);
        } catch (final Exception e) {
            error = e;
        } finally {
            this.scenarioController.shutdown();
        }

        Runtime.getRuntime().removeShutdownHook(this.scenarioShutdownHook);
        final var runHook = this.scenarioShutdownHook;
        this.scenarioShutdownHook = null;
        runHook.run();
        this.logger.debug("removing scenario shutdown hook");
    }

    public void notifyException(final Thread t, final Throwable e) {
        error = new RuntimeException("in thread " + t.getName() + ", " + e, e);
    }

    private void executeScenarioScripts() {
        for (final String script : this.scripts)
            try {
                Object result = null;
                if ((scriptEngine instanceof Compilable compilableEngine) && this.wantsCompiledScript) {
                    this.logger.debug("Using direct script compilation");
                    final CompiledScript compiled = compilableEngine.compile(script);
                    this.logger.debug("-> invoking main scenario script (compiled)");
                    result = compiled.eval();
                    this.logger.debug("<- scenario script completed (compiled)");
                } else if ((null != scriptfile) && !this.scriptfile.isEmpty()) {
                    final String filename = this.scriptfile.replace("_SESSION_", this.scenarioName);
                    this.logger.debug("-> invoking main scenario script (interpreted from {})", filename);
                    final Path written = Files.write(
                        Path.of(filename),
                        script.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE
                    );
                    final BufferedReader reader = Files.newBufferedReader(written);
                    this.scriptEngine.eval(reader);
                    this.logger.debug("<- scenario control script completed (interpreted) from {})", filename);
                } else {
                    this.logger.debug("-> invoking main scenario script (interpreted)");
                    result = this.scriptEngine.eval(script);
                    this.logger.debug("<- scenario control script completed (interpreted)");
                }

                if (null != result)
                    this.logger.debug("scenario result: type({}): value:{}", result.getClass().getCanonicalName(), result);
                System.err.flush();
                System.out.flush();
            } catch (final Exception e) {
                error = e;
                state = State.Errored;
                this.logger.error("Error in scenario, shutting down. ({})", e);
                try {
                    scenarioController.forceStopScenario(5000, false);
                } catch (final Exception eInner) {
                    this.logger.debug("Found inner exception while forcing stop with rethrow=false: {}", eInner);
                } finally {
                    throw new RuntimeException(e);
                }
            } finally {
                System.out.flush();
                System.err.flush();
                this.endedAtMillis = System.currentTimeMillis();
            }
    }

    public void finish() {
        this.logger.debug("finishing scenario");
        this.endedAtMillis = System.currentTimeMillis(); //TODO: Make only one endedAtMillis assignment
        if (State.Running == this.state) state = State.Finished;

        if (null != scenarioShutdownHook) {
            // If this method was called while the shutdown hook is defined, then it means
            // that the scenario was ended before the hook was uninstalled normally.
            state = State.Interrupted;
            this.logger.warn("Scenario was interrupted by process exit, shutting down");
        } else
            this.logger.info("Scenario completed successfully, with {} logical activities.", this.scenarioController.getActivityExecutorMap().size());

        this.logger.info(() -> "scenario state: " + state);

        // We report the scenario state via annotation even for short runs
        final Annotation annotation = Annotation.newBuilder()
            .session(scenarioName)
            .interval(startedAtMillis, this.endedAtMillis)
            .layer(Layer.Scenario)
            .label("state", state.toString())
            .detail("command_line", commandLine)
            .build();

        Annotators.recordAnnotation(annotation);

    }

    public long getStartedAtMillis() {
        return this.startedAtMillis;
    }

    public long getEndedAtMillis() {
        return this.endedAtMillis;
    }

    /**
     * This should be the only way to get a ScenarioResult for a Scenario.
     * <p>
     * The lifecycle of a scenario includes the lifecycles of all of the following:
     * <OL>
     * <LI>The scenario control script, executing within a graaljs context.</LI>
     * <LI>The lifecycle of every activity which is started within the scenario.</LI>
     * </OL>
     * <p>
     * All of these run asynchronously within the scenario, however the same thread that calls
     * the scenario is the one which executes the control script. A scenario ends when all
     * of the following conditions are met:
     * <UL>
     * <LI>The scenario control script has run to completion, or experienced an exception.</LI>
     * <LI>Each activity has run to completion, experienced an exception, or all</LI>
     * </UL>
     *
     * @return
     */
    @Override
    public synchronized ExecutionMetricsResult call() {
        if (null == result) {
            try {
                this.runScenario();
            } catch (final Exception e) {
                error = e;
            } finally {
                this.logger.debug("{} scenario run", null == this.error ? "NORMAL" : "ERRORED");
            }
            if (this.scriptEnv == null) {

            }
            String iolog = error != null ? error.toString() : this.scriptEnv.getTimedLog();
            result = new ExecutionMetricsResult(startedAtMillis, endedAtMillis, iolog, this.error);
            this.result.reportMetricsSummaryToLog();
            this.doReportSummaries(this.reportSummaryTo, this.result);
        }

        return this.result;
    }

    private void doReportSummaries(final String reportSummaryTo, final ExecutionMetricsResult result) {
        final List<PrintStream> fullChannels = new ArrayList<>();
        final List<PrintStream> briefChannels = new ArrayList<>();


        final String[] destinationSpecs = reportSummaryTo.split(", *");

        for (final String spec : destinationSpecs)
            if ((null != spec) && !spec.isBlank()) {
                final String[] split = spec.split(":", 2);
                final String summaryTo = split[0];
                final long summaryWhen = (2 == split.length) ? (Long.parseLong(split[1]) * 1000L) : 0;

                PrintStream out = null;
                switch (summaryTo.toLowerCase()) {
                    case "console":
                    case "stdout":
                        out = System.out;
                        break;
                    case "stderr":
                        out = System.err;
                        break;
                    default:
                        final String outName = summaryTo
                            .replaceAll("_SESSION_", scenarioName)
                            .replaceAll("_LOGS_", this.logsPath.toString());
                        try {
                            out = new PrintStream(new FileOutputStream(outName));
                            break;
                        } catch (final FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                }


                if (result.getElapsedMillis() > summaryWhen) fullChannels.add(out);
                else {
                    this.logger.debug("Summarizing counting metrics only to {} with scenario duration of {}ms (<{})", spec, summaryWhen, summaryWhen);
                    briefChannels.add(out);
                }
            }
        fullChannels.forEach(result::reportMetricsSummaryTo);
//        briefChannels.forEach(result::reportCountsTo);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((null == o) || (this.getClass() != o.getClass())) {
            return false;
        }
        final Scenario scenario = (Scenario) o;
        return Objects.equals(this.scenarioName, scenario.scenarioName);
    }

    @Override
    public int hashCode() {
        return (null != this.scenarioName) ? scenarioName.hashCode() : 0;
    }

    public String getScenarioName() {
        return this.scenarioName;
    }

    public ScenarioController getScenarioController() {
        return this.scenarioController;
    }

    public String getScriptText() {
        return this.scripts.stream().collect(Collectors.joining());
    }

    public Optional<List<String>> getIOLog() {
        return Optional.ofNullable(this.scriptEnv).map(ScriptEnvBuffer::getTimeLogLines);
    }

    public String toString() {
        return "name:'" + scenarioName + '\'';
    }

    public void addScenarioScriptParams(final ScriptParams scenarioScriptParams) {
        this.scenarioScriptParams = scenarioScriptParams;
    }

    public void addScenarioScriptParams(final Map<String, String> scriptParams) {
        this.addScenarioScriptParams(new ScriptParams() {{
            this.putAll(scriptParams);
        }});
    }

    public State getScenarioState() {
        return this.state;
    }

    public void enableCharting() {
        final MetricRegistry metricRegistry = ActivityMetrics.getMetricRegistry();
    }

    public String getReportSummaryTo() {
        return this.reportSummaryTo;
    }
}

