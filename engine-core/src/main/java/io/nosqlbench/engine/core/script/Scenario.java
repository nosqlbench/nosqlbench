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
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.engine.api.extensions.ScriptingPluginInfo;
import io.nosqlbench.engine.api.metrics.ActivityMetrics;
import io.nosqlbench.engine.core.metrics.NashornMetricRegistryBindings;
import io.nosqlbench.engine.api.scripting.ScriptEnvBuffer;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private ScenarioContext scriptEnv;
    private String name;
    private ScenarioLogger scenarioLogger;
    private ScriptParams scenarioScriptParams;
    private Engine engine = Engine.Graalvm;

    public enum Engine {
        Nashorn,
        Graalvm
    }

    public Scenario(String name, Engine engine, String progressInterval) {
        this.name = name;
        this.engine = engine;
        this.progressInterval = progressInterval;
    }

    public Scenario(String name, Engine engine) {
        this.name = name;
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
                scriptEngine = engineManager.getEngineByName("nashorn");
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

                scriptEngine = GraalJSScriptEngine.create(null, contextSettings);

                try {
                    scriptEngine.put("javaObj", new Object());
                    scriptEngine.eval("(javaObj instanceof Java.type('java.lang.Object'));");
                } catch (ScriptException e) {
                    throw new RuntimeException(e);
                }

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
            scriptEngine.put("scenario", new PolyglotScenarioController(scenarioController));
            scriptEngine.put("metrics", new PolyglotMetricRegistryBindings(metricRegistry));
            scriptEngine.put("activities", new NashornActivityBindings(scenarioController));
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
        init();

        logger.debug("Running control script for " + getName() + ".");
        for (String script : scripts) {
            try {
                Object result = null;
                if (scriptEngine instanceof Compilable) {
                    logger.debug("Using direct script compilation");
                    Compilable compilableEngine = (Compilable) scriptEngine;
                    CompiledScript compiled = compilableEngine.compile(script);
                    logger.debug("-> invoking main scenario script (compiled)");
                    result = compiled.eval();
                    logger.debug("<- scenario completed (compiled)");
                } else {
                    logger.debug("-> invoking main scenario script (interpreted)");
                    result = scriptEngine.eval(script);
                    logger.debug("<- scenario completed (interpreted)");
                }

                if (result != null) {
                    logger.debug("scenario result: type(" + result.getClass().getCanonicalName() + "): value:" + result.toString());
                }
                System.err.flush();
                System.out.flush();
            } catch (ScriptException e) {
                String diagname = "diag_" + System.currentTimeMillis() + ".js";
                try {
                    Path diagFilePath = Paths.get(scenarioLogger.getLogDir(), diagname);
                    Files.writeString(diagFilePath, script);
                } catch (Exception ignored) {
                }
                String errorDesc = "Script error while running scenario:" + e.toString() + ", script content is at " + diagname;
                e.printStackTrace();
                logger.error(errorDesc, e);
                scenarioController.forceStopScenario(5000);
                throw new RuntimeException("Script error while running scenario:" + e.getMessage(), e);
            } catch (BasicError ue) {
                logger.error(ue.getMessage());
                scenarioController.forceStopScenario(5000);
                throw ue;
            } catch (Exception o) {
                String errorDesc = "Non-Script error while running scenario:" + o.getMessage();
                logger.error(errorDesc, o);
                scenarioController.forceStopScenario(5000);
                throw new RuntimeException("Non-Script error while running scenario:" + o.getMessage(), o);
            } finally {
                System.out.flush();
                System.err.flush();
            }
        }
        int awaitCompletionTime = 86400 * 365 * 1000;
        logger.debug("Awaiting completion of scenario for " + awaitCompletionTime + " millis.");
        scenarioController.awaitCompletion(awaitCompletionTime);
        logger.debug("scenario completed without errors");
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
        return getName() != null ? getName().equals(scenario.getName()) : scenario.getName() == null;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public String getName() {
        return name;
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
        return "name:'" + this.getName() + "'";
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

