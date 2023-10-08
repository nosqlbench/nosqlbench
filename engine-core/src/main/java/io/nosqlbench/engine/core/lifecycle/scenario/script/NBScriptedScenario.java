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
package io.nosqlbench.engine.core.lifecycle.scenario.script;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneFixtures;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotScenarioController;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine.Builder;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

import javax.script.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NBScriptedScenario extends NBScenario {
    private final Invocation invocation = Invocation.EXECUTE_SCRIPT;

    private Exception error;

    private ExecutionMetricsResult result;
    private BufferedScriptContext context;

    public Optional<ExecutionMetricsResult> getResultIfComplete() {
        return Optional.ofNullable(result);
    }


    public enum Invocation {
        RENDER_SCRIPT,
        EXECUTE_SCRIPT
    }

    private final List<String> scripts = new ArrayList<>();
    private ScriptEngine scriptEngine;

    private ActivitiesProgressIndicator activitiesProgressIndicator;
    private String progressInterval = "console:1m";
    //    private ScenarioScriptShell scriptEnv;
    private final String scenarioName;
    private ScenarioParams scenarioScenarioParams;
    private final Engine engine = Engine.Graalvm;
    private long startedAtMillis = -1L;
    private long endedAtMillis = -1L;

    public enum Engine {
        Graalvm
    }

    public NBScriptedScenario(
        final String scenarioName,
        NBComponent parentComponent
    ) {
        super(parentComponent, scenarioName);
        this.scenarioName = scenarioName;
        this.progressInterval = progressInterval;
    }

    public static NBScriptedScenario ofScripted(String name, Map<String, String> params, NBComponent parent, Invocation invocation) {
        return new NBScriptedScenario(name, parent);
    }

    ;


    public NBScriptedScenario addScriptText(final String scriptText) {
        this.scripts.add(scriptText);
        return this;
    }


    public NBScriptedScenario addScriptFiles(final String... args) {
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

    private BufferedScriptContext initializeScriptContext(NBSceneFixtures fixtures) {
        BufferedScriptContext ctx = new BufferedScriptContext(fixtures);
//        this.scriptEngine.setContext(ctx);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("scenario", new PolyglotScenarioController(fixtures.controller()));
        return ctx;
    }

    private void initializeScriptingEngine() {
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
        scriptEngine = GraalJSScriptEngine.create(polyglotEngine, contextSettings);
    }

    protected final void runScenario(NBSceneFixtures shell) {
        try {
            this.logger.debug("Initializing scripting engine for {}.", scenarioName);
            this.initializeScriptingEngine();
            this.context = this.initializeScriptContext(shell);
            this.logger.debug("Running control script for {}.", scenarioName);
            this.executeScenarioScripts();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } finally {
            this.endedAtMillis = System.currentTimeMillis();
//            this.logger.debug("{} scenario run", null == this.error ? "NORMAL" : "ERRORED");
        }
//            String iolog = error != null ? error.toString() : this.scriptEnv.getTimedLog();
//            result = new ExecutionMetricsResult(startedAtMillis, endedAtMillis, iolog, this.error);
//            this.result.reportMetricsSummaryToLog();
    }

    private void executeScenarioScripts() throws ScriptException {
        for (final String script : this.scripts) {
            if ((scriptEngine instanceof Compilable compilableEngine)) {
                this.logger.debug("Using direct script compilation");
                final CompiledScript compiled = compilableEngine.compile(script);
                this.logger.debug("-> invoking main scenario script (compiled)");
                compiled.eval(this.context);
                this.logger.debug("<- scenario script completed (compiled)");
            } else {
                this.logger.debug("-> invoking main scenario script (interpreted)");
                this.scriptEngine.eval(script);
                this.logger.debug("<- scenario control script completed (interpreted)");
            }
        }
    }


    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((null == o) || (this.getClass() != o.getClass())) {
            return false;
        }
        final NBScriptedScenario scenario = (NBScriptedScenario) o;
        return Objects.equals(this.scenarioName, scenario.scenarioName);
    }

    @Override
    public int hashCode() {
        return (null != this.scenarioName) ? scenarioName.hashCode() : 0;
    }

    public String toString() {
        return "name:'" + scenarioName + '\'';
    }

//    public void addScenarioScriptParams(final ScriptParams scenarioScriptParams) {
//        this.scenarioScriptParams = scenarioScriptParams;
//    }

//    public void addScenarioScriptParams(final Map<String, String> scriptParams) {
//        this.addScenarioScriptParams(new ScriptParams() {{
//            this.putAll(scriptParams);
//        }});
//    }


}

