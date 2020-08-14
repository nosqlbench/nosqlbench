/*
 *   Copyright 2016 jshook
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package io.nosqlbench.engine.core.script;

import ch.qos.logback.classic.Logger;
import com.codahale.metrics.MetricRegistry;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import io.nosqlbench.engine.core.*;
import io.nosqlbench.engine.core.metrics.PolyglotMetricRegistryBindings;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.metrics.NashornMetricRegistryBindings;
import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.graalvm.polyglot.*;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
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

public class Scenario implements Callable<ScenarioResult> {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(Scenario.class);

    private static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private final List<String> scripts = new ArrayList<>();
    private ScriptEngine scriptEngine;
    private ScenarioController scenarioController;
    private ProgressIndicator progressIndicator;
    private String progressInterval = "console:1m";
    private boolean wantsGraaljsCompatMode;
    private ScenarioContext scriptEnv;
    private String scenarioName;
    private ScenarioLogger scenarioLogger;
    private ScriptParams scenarioScriptParams;
    private String scriptfile;
    private Engine engine = Engine.Graalvm;
    private boolean wantsStackTraces=false;
    private boolean wantsCompiledScript;
    private long startedAtMillis = -1L;
    private long endedAtMillis = -1L;

    public enum Engine {
        Nashorn,
        Graalvm
    }

    public Scenario(
            String scenarioName,
            String scriptfile,
            Engine engine,
            String progressInterval,
            boolean wantsGraaljsCompatMode,
            boolean wantsStackTraces,
            boolean wantsCompiledScript) {
        this.scenarioName = scenarioName;
        this.scriptfile = scriptfile;
        this.engine = engine;
        this.progressInterval = progressInterval;
        this.wantsGraaljsCompatMode = wantsGraaljsCompatMode;
        this.wantsStackTraces = wantsStackTraces;
        this.wantsCompiledScript = wantsCompiledScript;
    }

    public Scenario(String name, Engine engine) {
        this.scenarioName = name;
        this.engine = engine;
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
            Charset utf8 = Charset.forName("UTF8");
            String scriptData = utf8.decode(bb).toString();
            addScriptText(scriptData);
        }
        return this;
    }

    private void init() {

        logger.info("Using engine " + engine.toString());

        MetricRegistry metricRegistry = ActivityMetrics.getMetricRegistry();

        switch (engine) {
            case Nashorn:
                NashornScriptEngineFactory f = new NashornScriptEngineFactory();
                this.scriptEngine = f.getScriptEngine("--language=es6");

                // engineManager.getEngineByName("nashorn");
                // TODO: Figure out how to do this in engine bindings: --language=es-6
                break;
            case Graalvm:
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

                this.scriptEngine = GraalJSScriptEngine.create(null, contextSettings);

//                try {
//                    this.scriptEngine.put("javaObj", new Object());
//                    this.scriptEngine.eval("(javaObj instanceof Java.type('java.lang.Object'));");
//                } catch (ScriptException e) {
//                    throw new RuntimeException(e);
//                }

                break;
        }

        scenarioController = new ScenarioController();
        if (!progressInterval.equals("disabled")) {
            progressIndicator = new ProgressIndicator(scenarioController, progressInterval);
        }

        scriptEnv = new ScenarioContext(scenarioController);
        scriptEngine.setContext(scriptEnv);

        scriptEngine.put("params", scenarioScriptParams);

        if (engine == Engine.Graalvm) {
            // https://github.com/graalvm/graaljs/blob/master/docs/user/JavaInterop.md
            if (wantsGraaljsCompatMode) {
                scriptEngine.put("scenario", scenarioController);
                scriptEngine.put("metrics", new NashornMetricRegistryBindings(metricRegistry));
                scriptEngine.put("activities", new NashornActivityBindings(scenarioController));
            } else {
                scriptEngine.put("scenario", new PolyglotScenarioController(scenarioController));
                scriptEngine.put("metrics", new PolyglotMetricRegistryBindings(metricRegistry));
                scriptEngine.put("activities", new NashornActivityBindings(scenarioController));
            }
        } else if (engine == Engine.Nashorn) {
            scriptEngine.put("scenario", scenarioController);
            scriptEngine.put("metrics", new NashornMetricRegistryBindings(metricRegistry));
            scriptEngine.put("activities", new NashornActivityBindings(scenarioController));
        } else {
            throw new RuntimeException("Unsupported engine: " + engine);
        }

        for (ScriptingPluginInfo<?> extensionDescriptor : SandboxExtensionFinder.findAll()) {
            if (!extensionDescriptor.isAutoLoading()) {
                logger.info("Not loading " + extensionDescriptor + ", autoloading is false");
                continue;
            }

            org.slf4j.Logger extensionLogger =
                LoggerFactory.getLogger("extensions." + extensionDescriptor.getBaseVariableName());
            Object extensionObject = extensionDescriptor.getExtensionObject(
                extensionLogger,
                metricRegistry,
                scriptEnv
            );
            logger.debug("Adding extension object:  name=" + extensionDescriptor.getBaseVariableName() +
                " class=" + extensionObject.getClass().getSimpleName());
            scriptEngine.put(extensionDescriptor.getBaseVariableName(), extensionObject);
        }


    }

    public void run() {
        startedAtMillis=System.currentTimeMillis();

        init();

        logger.debug("Running control script for " + getScenarioName() + ".");
        for (String script : scripts) {
            try {
                Object result = null;
                if (scriptEngine instanceof Compilable && wantsCompiledScript) {
                    logger.debug("Using direct script compilation");
                    Compilable compilableEngine = (Compilable) scriptEngine;
                    CompiledScript compiled = compilableEngine.compile(script);
                    logger.debug("-> invoking main scenario script (compiled)");
                    result = compiled.eval();
                    logger.debug("<- scenario completed (compiled)");
                } else {
                    if (scriptfile != null && !scriptfile.isEmpty()) {

                        String filename = scriptfile.replace("_SESSIONNAME_", scenarioName);
                        logger.debug("-> invoking main scenario script (" +
                                "interpreted from " + filename +")");
                        Path written = Files.write(
                                Path.of(filename),
                                script.getBytes(StandardCharsets.UTF_8),
                                StandardOpenOption.TRUNCATE_EXISTING,
                                StandardOpenOption.CREATE
                        );
                        BufferedReader reader = Files.newBufferedReader(written);
                        scriptEngine.eval(reader);
                        logger.debug("<- scenario completed (interpreted " +
                                "from " + filename + ")");
                    } else {
                        logger.debug("-> invoking main scenario script (interpreted)");
                        result = scriptEngine.eval(script);
                        logger.debug("<- scenario completed (interpreted)");
                    }
                }

                if (result != null) {
                    logger.debug("scenario result: type(" + result.getClass().getCanonicalName() + "): value:" + result.toString());
                }
                System.err.flush();
                System.out.flush();
            } catch (Exception e) {
                this.scenarioController.forceStopScenario(5000);
                throw new RuntimeException(e);
            } finally {
                System.out.flush();
                System.err.flush();
                endedAtMillis=System.currentTimeMillis();
            }
        }
        int awaitCompletionTime = 86400 * 365 * 1000;
        logger.debug("Awaiting completion of scenario for " + awaitCompletionTime + " millis.");
        scenarioController.awaitCompletion(awaitCompletionTime);
        //TODO: Ensure control flow covers controller shutdown in event of internal error.

        logger.debug("scenario completed without errors");
        endedAtMillis=System.currentTimeMillis(); //TODO: Make only one endedAtMillis assignment
    }

    public long getStartedAtMillis() {
        return startedAtMillis;
    }

    public long getEndedAtMillis() {
        return endedAtMillis;
    }

    public ScenarioResult call() {
        run();
        String iolog = scriptEnv.getTimedLog();
        return new ScenarioResult(iolog);
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

    public void setScenarioLogger(ScenarioLogger scenarioLogger) {
        this.scenarioLogger = scenarioLogger;
    }

    public void addScenarioScriptParams(ScriptParams scenarioScriptParams) {
        this.scenarioScriptParams = scenarioScriptParams;
    }

    public void addScenarioScriptParams(Map<String, String> scriptParams) {
        addScenarioScriptParams(new ScriptParams() {{
            putAll(scriptParams);
        }});
    }

    public void enableCharting() {
        MetricRegistry metricRegistry = ActivityMetrics.getMetricRegistry();
    }
}

