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

import com.codahale.metrics.MetricRegistry;
import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBSceneBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScriptParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine.Builder;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NBScriptedScenario extends NBScenario {
    private final Invocation invocation;

    private Exception error;

    private ExecutionMetricsResult result;
    private final NBLabeledElement parentComponent;

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
    private ScenarioScriptShell scriptEnv;
    private final String scenarioName;
    private ScriptParams scenarioScriptParams;
    private final Engine engine = Engine.Graalvm;
    private long startedAtMillis = -1L;
    private long endedAtMillis = -1L;

    public enum Engine {
        Graalvm
    }

    public NBScriptedScenario(
        final String scenarioName,
        final String progressInterval,
        Map<String, String> params,
        NBComponent parentComponent,
        Invocation invocation
    ) {
        super(parentComponent, scenarioName, params, progressInterval);
        this.scenarioName = scenarioName;
        this.progressInterval = progressInterval;
        this.parentComponent = parentComponent;
        this.invocation = invocation;
    }

    public static NBScriptedScenario ofScripted(String name, Map<String, String> params, NBComponent parent, Invocation invocation) {
        return new NBScriptedScenario(name, "console:10s",params,parent,invocation);
    };


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

    private void initializeScriptContext(NBSceneBuffer fixtures) {
        BufferedScriptContext ctx = new BufferedScriptContext(fixtures);
        this.scriptEngine.setContext(ctx);
    }

    private void initializeScriptingEngine() {

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

//        NBScenarioPojoContext sctx = new NBScenarioPojoContext(
//            this.scenarioScriptParams,
//            (NBSession) this.getParent(),
//            scenarioController,
//            new ActivityBindings(scenarioController)
//        );
//
//        this.scriptEngine.put("params", sctx.params());
//        this.scriptEngine.put("session", sctx.session());
//        this.scriptEngine.put("activities", sctx.activities());
//        this.scriptEngine.put("scenario", new PolyglotScenarioController(sctx.controller()));
//
    }

    protected synchronized void runScenario(NBSceneBuffer context) {
        if (null == result) {
            try {
                this.logger.debug("Initializing scripting engine for {}.", scenarioName);
                this.initializeScriptingEngine();
                this.initializeScriptContext(context);
                this.logger.debug("Running control script for {}.", scenarioName);
                this.executeScenarioScripts();
            } catch (final Exception e) {
                error = e;
            } finally {
                this.logger.debug("{} scenario run", null == this.error ? "NORMAL" : "ERRORED");
            }
//            String iolog = error != null ? error.toString() : this.scriptEnv.getTimedLog();
//            result = new ExecutionMetricsResult(startedAtMillis, endedAtMillis, iolog, this.error);
//            this.result.reportMetricsSummaryToLog();
        }
    }

    private void executeScenarioScripts() {
        for (final String script : this.scripts)
            try {
                Object result = null;
                if ((scriptEngine instanceof Compilable compilableEngine)) {
                    this.logger.debug("Using direct script compilation");
                    final CompiledScript compiled = compilableEngine.compile(script);
                    this.logger.debug("-> invoking main scenario script (compiled)");
                    result = compiled.eval();
                    this.logger.debug("<- scenario script completed (compiled)");
                }
//                 else if ((null != scriptfile) && !this.scriptfile.isEmpty()) {
//                    final String filename = this.scriptfile.replace("_SESSION_", this.scenarioName);
//                    this.logger.debug("-> invoking main scenario script (interpreted from {})", filename);
//                    final Path written = Files.writeString(
//                        Path.of(filename),
//                        script,
//                        StandardOpenOption.TRUNCATE_EXISTING,
//                        StandardOpenOption.CREATE
//                    );
//                    final BufferedReader reader = Files.newBufferedReader(written);
//                    this.scriptEngine.eval(reader);
//                    this.logger.debug("<- scenario control script completed (interpreted) from {})", filename);
//                }
                else {
                    this.logger.debug("-> invoking main scenario script (interpreted)");
                    result = this.scriptEngine.eval(script);
                    this.logger.debug("<- scenario control script completed (interpreted)");
                }
                if (null != result)
                    this.logger.debug("scenario result: type({}): value:{}", result.getClass().getCanonicalName(), result);
            } catch (final Exception e) {
                error = e;
                this.logger.error("Error in scenario, shutting down. ({})", e);
            } finally {
                this.endedAtMillis = System.currentTimeMillis();
                System.out.flush();
                System.err.flush();
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

    public void addScenarioScriptParams(final ScriptParams scenarioScriptParams) {
        this.scenarioScriptParams = scenarioScriptParams;
    }

    public void addScenarioScriptParams(final Map<String, String> scriptParams) {
        this.addScenarioScriptParams(new ScriptParams() {{
            this.putAll(scriptParams);
        }});
    }


}

