/*
 * Copyright (c) 2022 nosqlbench
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
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.metadata.ScenarioMetadata;
import io.nosqlbench.api.metadata.ScenarioMetadataAware;
import io.nosqlbench.api.metadata.SystemId;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import io.nosqlbench.engine.core.annotation.Annotators;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivityProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.script.SandboxExtensionFinder;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScenarioContext;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScriptParams;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.ActivityBindings;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotMetricRegistryBindings;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotScenarioController;
import io.nosqlbench.nb.annotations.Maturity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.Context;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Scenario implements Callable<ExecutionMetricsResult> {

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

    public Optional<ExecutionMetricsResult> getResultIfComplete() {
        return Optional.ofNullable(this.result);
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
    private String scriptfile;
    private Engine engine = Engine.Graalvm;
    private boolean wantsStackTraces = false;
    private boolean wantsCompiledScript;
    private long startedAtMillis = -1L;
    private long endedAtMillis = -1L;

    public enum Engine {
        Graalvm
    }

    public Scenario(
        String scenarioName,
        String scriptfile,
        Engine engine,
        String progressInterval,
        boolean wantsStackTraces,
        boolean wantsCompiledScript,
        String reportSummaryTo,
        String commandLine,
        Path logsPath,
        Maturity minMaturity) {

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
    }

    public Scenario(String name, Engine engine, String reportSummaryTo, Maturity minMaturity) {
        this.scenarioName = name;
        this.reportSummaryTo = reportSummaryTo;
        this.engine = engine;
        this.commandLine = "";
        this.minMaturity = minMaturity;
        this.logsPath = Path.of("logs");
    }

    public Scenario setLogger(Logger logger) {
        this.logger = logger;
        return this;
    }

    public Logger getLogger() {
        return logger;
    }

    public Scenario addScriptText(String scriptText) {
        scripts.add(scriptText);
        return this;
    }


    public Scenario addScriptFiles(String... args) {
        for (String scriptFile : args) {
            Path scriptPath = Paths.get(scriptFile);
            byte[] bytes = new byte[0];
            try {
                bytes = Files.readAllBytes(scriptPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            Charset utf8 = StandardCharsets.UTF_8;
            String scriptData = utf8.decode(bb).toString();
            addScriptText(scriptData);
        }
        return this;
    }

    private void initializeScriptingEngine(ScenarioController scenarioController) {

        logger.debug("Using engine " + engine.toString());
        MetricRegistry metricRegistry = ActivityMetrics.getMetricRegistry();

        Context.Builder contextSettings = Context.newBuilder("js")
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
            .option("js.ecmascript-version", "2020")
            .option("js.nashorn-compat", "true");

        org.graalvm.polyglot.Engine.Builder engineBuilder = org.graalvm.polyglot.Engine.newBuilder();
        engineBuilder.option("engine.WarnInterpreterOnly", "false");
        org.graalvm.polyglot.Engine polyglotEngine = engineBuilder.build();

        // TODO: add in, out, err for this scenario
        this.scriptEngine = GraalJSScriptEngine.create(polyglotEngine, contextSettings);


        if (!progressInterval.equals("disabled")) {
            activityProgressIndicator = new ActivityProgressIndicator(scenarioController, progressInterval);
        }

        scriptEnv = new ScenarioContext(scenarioController);
        scriptEngine.setContext(scriptEnv);

        scriptEngine.put("params", scenarioScriptParams);

//            scriptEngine.put("scenario", scenarioController);
//            scriptEngine.put("metrics", new PolyglotMetricRegistryBindings(metricRegistry));
//            scriptEngine.put("activities", new NashornActivityBindings(scenarioController));

        scriptEngine.put("scenario", new PolyglotScenarioController(scenarioController));
        scriptEngine.put("metrics", new PolyglotMetricRegistryBindings(metricRegistry));
        scriptEngine.put("activities", new ActivityBindings(scenarioController));

        for (ScriptingPluginInfo<?> extensionDescriptor : SandboxExtensionFinder.findAll()) {
            if (!extensionDescriptor.isAutoLoading()) {
                logger.info(() -> "Not loading " + extensionDescriptor + ", autoloading is false");
                continue;
            }

            Logger extensionLogger =
                LogManager.getLogger("extensions." + extensionDescriptor.getBaseVariableName());
            Object extensionObject = extensionDescriptor.getExtensionObject(
                extensionLogger,
                metricRegistry,
                scriptEnv
            );
            ScenarioMetadataAware.apply(extensionObject, getScenarioMetadata());
            logger.trace(() -> "Adding extension object:  name=" + extensionDescriptor.getBaseVariableName() +
                " class=" + extensionObject.getClass().getSimpleName());
            scriptEngine.put(extensionDescriptor.getBaseVariableName(), extensionObject);
        }
    }

    private synchronized ScenarioMetadata getScenarioMetadata() {
        if (this.scenarioMetadata == null) {
            this.scenarioMetadata = new ScenarioMetadata(
                this.startedAtMillis,
                this.scenarioName,
                SystemId.getNodeId(),
                SystemId.getNodeFingerprint()
            );
        }
        return scenarioMetadata;
    }

    private synchronized void runScenario() {
        scenarioShutdownHook = new ScenarioShutdownHook(this);
        Runtime.getRuntime().addShutdownHook(scenarioShutdownHook);

        state = State.Running;
        startedAtMillis = System.currentTimeMillis();
        Annotators.recordAnnotation(
            Annotation.newBuilder()
                .session(this.scenarioName)
                .now()
                .layer(Layer.Scenario)
                .detail("engine", this.engine.toString())
                .build()
        );

        logger.debug("Running control script for " + getScenarioName() + ".");
        scenarioController = new ScenarioController(this);
        try {
            initializeScriptingEngine(scenarioController);
            executeScenarioScripts();
            long awaitCompletionTime = 86400 * 365 * 1000L;
            logger.debug("Awaiting completion of scenario and activities for " + awaitCompletionTime + " millis.");
            scenarioController.awaitCompletion(awaitCompletionTime);
        } catch (Exception e) {
            this.error=e;
        } finally {
            scenarioController.shutdown();
        }

        Runtime.getRuntime().removeShutdownHook(scenarioShutdownHook);
        scenarioShutdownHook.run();
        scenarioShutdownHook = null;
    }

    public void notifyException(Thread t, Throwable e) {
        this.error=new RuntimeException("in thread " + t.getName() + ", " +e, e);
    }
    private void executeScenarioScripts() {
        for (String script : scripts) {
            try {
                Object result = null;
                if (scriptEngine instanceof Compilable compilableEngine && wantsCompiledScript) {
                    logger.debug("Using direct script compilation");
                    CompiledScript compiled = compilableEngine.compile(script);
                    logger.debug("-> invoking main scenario script (compiled)");
                    result = compiled.eval();
                    logger.debug("<- scenario script completed (compiled)");
                } else {
                    if (scriptfile != null && !scriptfile.isEmpty()) {
                        String filename = scriptfile.replace("_SESSION_", scenarioName);
                        logger.debug("-> invoking main scenario script (" +
                            "interpreted from " + filename + ")");
                        Path written = Files.write(
                            Path.of(filename),
                            script.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.CREATE
                        );
                        BufferedReader reader = Files.newBufferedReader(written);
                        scriptEngine.eval(reader);
                        logger.debug("<- scenario control script completed (interpreted) " +
                            "from " + filename + ")");
                    } else {
                        logger.debug("-> invoking main scenario script (interpreted)");
                        result = scriptEngine.eval(script);
                        logger.debug("<- scenario control script completed (interpreted)");
                    }
                }

                if (result != null) {
                    logger.debug("scenario result: type(" + result.getClass().getCanonicalName() + "): value:" + result);
                }
                System.err.flush();
                System.out.flush();
            } catch (Exception e) {
                this.error=e;
                this.state = State.Errored;
                logger.error("Error in scenario, shutting down. (" + e + ")");
                try {
                    this.scenarioController.forceStopScenario(5000, false);
                } catch (Exception eInner) {
                    logger.debug("Found inner exception while forcing stop with rethrow=false: " + eInner);
                } finally {
                    throw new RuntimeException(e);
                }
            } finally {
                System.out.flush();
                System.err.flush();
                endedAtMillis = System.currentTimeMillis();
            }
        }
    }

    public synchronized void finish() {
        logger.debug("finishing scenario");
        endedAtMillis = System.currentTimeMillis(); //TODO: Make only one endedAtMillis assignment
        if (this.state == State.Running) {
            this.state = State.Finished;
        }

        if (scenarioShutdownHook != null) {
            // If this method was called while the shutdown hook is defined, then it means
            // that the scenario was ended before the hook was uninstalled normally.
            this.state = State.Interrupted;
            logger.warn("Scenario was interrupted by process exit, shutting down");
        } else {
            logger.info("Scenario completed successfully, with " + scenarioController.getActivityExecutorMap().size() + " logical activities.");
        }

        logger.info(() -> "scenario state: " + this.state);

        // We report the scenario state via annotation even for short runs
        Annotation annotation = Annotation.newBuilder()
            .session(this.scenarioName)
            .interval(this.startedAtMillis, endedAtMillis)
            .layer(Layer.Scenario)
            .label("state", this.state.toString())
            .detail("command_line", this.commandLine)
            .build();

        Annotators.recordAnnotation(annotation);

    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getEndedAtMillis() {
        return endedAtMillis;
    }

    /**
     * This should be the only way to get a ScenarioResult for a Scenario.
     *
     * The lifecycle of a scenario includes the lifecycles of all of the following:
     * <OL>
     * <LI>The scenario control script, executing within a graaljs context.</LI>
     * <LI>The lifecycle of every activity which is started within the scenario.</LI>
     * </OL>
     *
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
    public synchronized ExecutionMetricsResult call() {
        if (result == null) {
            try {
                runScenario();
            } catch (Exception e) {
                this.error=e;
            } finally {
                logger.debug((this.error==null ? "NORMAL" : "ERRORED") + " scenario run");
            }

            String iolog = scriptEnv.getTimedLog();
            this.result = new ExecutionMetricsResult(this.startedAtMillis, this.endedAtMillis, iolog, error);
            result.reportMetricsSummaryToLog();
            doReportSummaries(reportSummaryTo, result);
        }

        return result;
    }

    private void doReportSummaries(String reportSummaryTo, ExecutionMetricsResult result) {
        List<PrintStream> fullChannels = new ArrayList<>();
        List<PrintStream> briefChannels = new ArrayList<>();


        String[] destinationSpecs = reportSummaryTo.split(", *");

        for (String spec : destinationSpecs) {
            if (spec != null && !spec.isBlank()) {
                String[] split = spec.split(":", 2);
                String summaryTo = split[0];
                long summaryWhen = split.length == 2 ? Long.parseLong(split[1]) * 1000L : 0;

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
                        String outName = summaryTo
                            .replaceAll("_SESSION_", getScenarioName())
                            .replaceAll("_LOGS_", logsPath.toString());
                        try {
                            out = new PrintStream(new FileOutputStream(outName));
                            break;
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                }


                if (result.getElapsedMillis() > summaryWhen) {
                    fullChannels.add(out);
                } else {
                    logger.debug("Summarizing counting metrics only to " + spec + " with scenario duration of " + summaryWhen + "ms (<" + summaryWhen + ")");
                    briefChannels.add(out);
                }
            }
        }
        fullChannels.forEach(result::reportMetricsSummaryTo);
//        briefChannels.forEach(result::reportCountsTo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return getScenarioName() != null ? getScenarioName().equals(scenario.getScenarioName()) : scenario.getScenarioName() == null;
    }

    @Override
    public int hashCode() {
        return getScenarioName() != null ? getScenarioName().hashCode() : 0;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public ScenarioController getScenarioController() {
        return scenarioController;
    }

    public String getScriptText() {
        return scripts.stream().collect(Collectors.joining());
    }

    public Optional<List<String>> getIOLog() {
        return Optional.ofNullable(scriptEnv).map(ScriptEnvBuffer::getTimeLogLines);
    }

    public String toString() {
        return "name:'" + this.getScenarioName() + "'";
    }

    public void addScenarioScriptParams(ScriptParams scenarioScriptParams) {
        this.scenarioScriptParams = scenarioScriptParams;
    }

    public void addScenarioScriptParams(Map<String, String> scriptParams) {
        addScenarioScriptParams(new ScriptParams() {{
            putAll(scriptParams);
        }});
    }

    public State getScenarioState() {
        return state;
    }

    public void enableCharting() {
        MetricRegistry metricRegistry = ActivityMetrics.getMetricRegistry();
    }

    public String getReportSummaryTo() {
        return reportSummaryTo;
    }
}

