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
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ContextActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.script.bindings.PolyglotScenarioController;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine.Builder;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.io.IOAccess;

import javax.script.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class NBScriptedCommand extends NBBaseCommand {
    private final Invocation invocation = Invocation.EXECUTE_SCRIPT;

    private Exception error;

    private ExecutionMetricsResult result;
    private BufferedScriptContext context;
    private GraalJSScriptEngine _engine;

    public Optional<ExecutionMetricsResult> getResultIfComplete() {
        return Optional.ofNullable(result);
    }



    public enum Invocation {
        RENDER_SCRIPT,
        EXECUTE_SCRIPT
    }

    private final List<String> scripts = new ArrayList<>();

    private ActivitiesProgressIndicator activitiesProgressIndicator;
    private String progressInterval = "console:1m";
    //    private ScenarioScriptShell scriptEnv;
    private final String phaseName;
    private NBCommandParams scenarioScenarioParams;
    private final Engine engine = Engine.Graalvm;
    private long startedAtMillis = -1L;
    private long endedAtMillis = -1L;

    public enum Engine {
        Graalvm
    }

    public NBScriptedCommand(
        NBBufferedCommandContext parentComponent,
        String phaseName,
        String targetScenario
    ) {
        super(parentComponent, phaseName, targetScenario);
        this.phaseName = phaseName;
        this.progressInterval = progressInterval;
    }

    public static NBScriptedCommand ofScripted(String name, Map<String, String> params, NBBufferedCommandContext parent, Invocation invocation) {
        return new NBScriptedCommand(parent, name, "default");
    }

    ;


    public NBScriptedCommand addScriptText(final String scriptText) {
        this.scripts.add(scriptText);
        return this;
    }


    public NBScriptedCommand addScriptFiles(final String... args) {
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

    private BufferedScriptContext initializeScriptContext(PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
        BufferedScriptContext ctx = new BufferedScriptContext(stdout, stderr, stdin);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("scenario", new PolyglotScenarioController(controller));
        return ctx;
    }

    private GraalJSScriptEngine initializeScriptingEngine() {
        if (_engine == null) {
            final Context.Builder contextSettings = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .allowNativeAccess(true)
                .allowCreateThread(true)
                .allowIO(IOAccess.ALL)
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
            this._engine= GraalJSScriptEngine.create(polyglotEngine, contextSettings);
        }
        return this._engine;
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContextActivitiesController controller) {
        try {
            this.logger.debug("Initializing scripting engine for {}.", phaseName);
            GraalJSScriptEngine engine = this.initializeScriptingEngine();
            this.context = this.initializeScriptContext(stdout, stderr, stdin, controller);
            this.logger.debug("Running control script for {}.", phaseName);

            Object resultObject = null;
            for (final String script : this.scripts) {
                if ((engine instanceof Compilable compilableEngine)) {
                    this.logger.debug("Using direct script compilation");
                    final CompiledScript compiled = compilableEngine.compile(script);
                    this.logger.debug("-> invoking main scenario script (compiled)");
                    engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("params",params);
                    resultObject = compiled.eval(this.context);
//                    engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).remove("params");
                    this.logger.debug("<- scenario script completed (compiled)");
                } else {
                    this.logger.debug("-> invoking main scenario script (interpreted)");
                    engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("params",params);
                    resultObject = engine.eval(script);
//                    engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).remove("params");
                    this.logger.debug("<- scenario control script completed (interpreted)");
                }
                return resultObject;
            }
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } finally {
            this.endedAtMillis = System.currentTimeMillis();
//            this.logger.debug("{} scenario run", null == this.error ? "NORMAL" : "ERRORED");
        }
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if ((null == o) || (this.getClass() != o.getClass())) {
            return false;
        }
        final NBScriptedCommand scenario = (NBScriptedCommand) o;
        return Objects.equals(this.phaseName, scenario.phaseName);
    }

    @Override
    public int hashCode() {
        return (null != this.phaseName) ? phaseName.hashCode() : 0;
    }

    public String toString() {
        return "name:'" + phaseName + '\'';
    }


}

