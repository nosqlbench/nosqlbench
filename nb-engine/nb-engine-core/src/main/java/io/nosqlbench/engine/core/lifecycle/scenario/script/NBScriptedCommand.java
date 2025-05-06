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
import io.nosqlbench.engine.cmdstream.BasicScriptBuffer;
import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.engine.core.lifecycle.ExecutionMetricsResult;
import io.nosqlbench.engine.core.lifecycle.activity.ActivitiesProgressIndicator;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine.Builder;
import org.graalvm.polyglot.EnvironmentAccess;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.io.IOAccess;

import javax.script.*;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.*;

public class NBScriptedCommand extends NBBaseCommand {
    private final Invocation invocation = Invocation.EXECUTE_SCRIPT;
    private final BasicScriptBuffer buffer;
    private Exception error;
    private ExecutionMetricsResult result;
    private BufferedScriptCtx ctx;
    private GraalJSScriptEngine _engine;

    public Optional<ExecutionMetricsResult> getResultIfComplete() {
        return Optional.ofNullable(result);
    }

    public NBScriptedCommand add(BasicScriptBuffer otherBuf) {
        this.buffer.add(otherBuf);
        return this;
    }


    public enum Invocation {
        RENDER_SCRIPT,
        EXECUTE_SCRIPT
    }

//    private final List<String> scripts = new ArrayList<>();

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
        NBBufferedContainer parentComponent,
        String phaseName
    ) {
        super(parentComponent, phaseName);
        this.phaseName = phaseName;
        this.progressInterval = progressInterval;
        this.buffer = new BasicScriptBuffer();
    }

    public static NBScriptedCommand ofScripted(String name, Map<String, String> params, NBBufferedContainer parent, Invocation invocation) {
        return new NBScriptedCommand(parent, name);
    }
    public NBScriptedCommand add(Cmd... cmds) {
        this.buffer.add(cmds);
        return this;
    }

//    public NBScriptedCommand addScriptText(final String scriptText) {
//        this.scripts.add(scriptText);
//        return this;
//    }
//

//    public NBScriptedCommand addScriptFiles(final String... args) {
//        for (final String scriptFile : args) {
//            final Path scriptPath = Paths.get(scriptFile);
//            byte[] bytes = new byte[0];
//            try {
//                bytes = Files.readAllBytes(scriptPath);
//            } catch (final IOException e) {
//                e.printStackTrace();
//            }
//            final ByteBuffer bb = ByteBuffer.wrap(bytes);
//            final Charset utf8 = StandardCharsets.UTF_8;
//            final String scriptData = utf8.decode(bb).toString();
//            this.addScriptText(scriptData);
//        }
//        return this;
//    }

    private BufferedScriptCtx initializeScriptContext(PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        BufferedScriptCtx ctx = new BufferedScriptCtx(stdout, stderr, stdin);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("this", this);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("container", this.ctx);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("controller", controller);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("stdout", stdout);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("stderr", stderr);
        ctx.getBindings(ScriptContext.ENGINE_SCOPE).put("stdin", stdin);

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
            this._engine = GraalJSScriptEngine.create(polyglotEngine, contextSettings);
        }
        return this._engine;
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        try {
            this.logger.debug("Initializing scripting engine for {}.", phaseName);
            GraalJSScriptEngine engine = this.initializeScriptingEngine();
            this.ctx = this.initializeScriptContext(stdout, stderr, stdin, controller);
            this.logger.debug("Running control script for {}.", phaseName);
            engine.setContext(ctx);
            engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("params", params);

            Object resultObject = null;

//            for (final String script : this.scripts) {
//                if ((engine instanceof Compilable compilableEngine)) {
//                    this.logger.debug("Using direct script compilation");
//                    final CompiledScript compiled = compilableEngine.compile(script);
//                    this.logger.debug("-> invoking main scenario script (compiled)");
//                    resultObject = compiled.eval(this.context);
//                    this.logger.debug("<- scenario script completed (compiled)");
//                } else {
                this.logger.debug("-> invoking main scenario script (interpreted)");
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("this", this);
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("params", params);
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("context", context);
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("controller", controller);
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("stdout", stdout);
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("stderr", stderr);
//                engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE).put("stdin", stdin);
                resultObject = engine.eval(buffer.getParsedScript());
                this.logger.debug("<- scenario control script completed (interpreted)");
//                }
                return resultObject;
//            }
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        } finally {
            this.endedAtMillis = System.currentTimeMillis();
//            this.logger.debug("{} scenario run", null == this.error ? "NORMAL" : "ERRORED");
        }
//        return null;
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
        return description();
    }


}

