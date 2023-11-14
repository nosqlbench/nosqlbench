/*
 * Copyright (c) 2023 nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.session;

import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.api.components.NBComponent;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.NBCommandLoader;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

public class NBCommandAssembly {

    private final static Logger logger = LogManager.getLogger(NBCommandAssembly.class);

    public static record CommandInvocation(NBBaseCommand command, NBCommandParams params, String contextName){};

    public static List<CommandInvocation> assemble(List<Cmd> cmds, Function<String, NBBufferedCommandContext> ctxprovider) {
        List<Cmd> mappedCmds = tagCommandsWithContext(cmds);
        List<CommandInvocation> invocations = prepareMappedPhases(mappedCmds, ctxprovider);
        return invocations;
    }

    private static List<Cmd> tagCommandsWithContext(List<Cmd> cmds) {
        LinkedList<Cmd> tagged = new LinkedList<>();
        String contextName=Cmd.DEFAULT_TARGET_CONTEXT;
        for (Cmd cmd : cmds) {
            if (cmd.getParams().containsKey("context")) {
                String ctx = cmd.getParams().remove("context");
                tagged.add(cmd.forTargetContext(ctx));
            } else if (cmd.getCmdType() == Cmd.CmdType.context) {
                contextName = cmd.getArg("context_name");
                if (contextName.equals(Cmd.DEFAULT_TARGET_CONTEXT)) {
                    logger.warn("You are explicitly setting the scenario name to "+Cmd.DEFAULT_TARGET_CONTEXT+"'. This is likely an error. " +
                        "This is the default scenario name, and if you are using different scenario names you should pick something that is different and specific.");
                }
            } else {
                tagged.add(cmd.forTargetContext(contextName));
            }
        }
        return new ArrayList<>(tagged);
    }

    private static List<CommandInvocation> prepareMappedPhases(List<Cmd> mappedCmds, Function<String, NBBufferedCommandContext> ctxProvider) {
        List<CommandInvocation> parameterizedInvocations = new ArrayList<>();
        String basename="phase_";
        int count=0;
        for (Cmd cmd : mappedCmds) {
            count++;
            String phaseName=basename+String.valueOf(count);
            String targetScenario = cmd.getParams().remove("scenario");

            NBCommandParams params = switch (cmd.getCmdType()) {
                case java, context -> NBCommandParams.of(cmd.getParams());
                default -> NBCommandParams.of(Map.of());
            };

            var command = switch (cmd.getCmdType()) {
                case java -> buildJavaCommand(List.of(cmd), ctxProvider.apply(cmd.getTargetContext()), phaseName);
                case context -> throw new RuntimeException("scenario commands should have already been parsed out.");
                default -> buildJavascriptCommand(List.of(cmd), targetScenario, ctxProvider.apply(cmd.getTargetContext()), phaseName);
            };

            String contextName = cmd.getTargetContext();
            parameterizedInvocations.add(new CommandInvocation(command,params,contextName));
        }
        return parameterizedInvocations;
    }

    private static NBBaseCommand buildJavascriptCommand(List<Cmd> cmds, String targetScenario, NBBufferedCommandContext parent, String phaseName) {
//        boolean dryrun;
//        NBScriptedScenario.Invocation invocation = dryrun ?
//            NBScriptedScenario.Invocation.RENDER_SCRIPT :
//            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;

        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        final NBCommandParams cmdParams = new NBCommandParams();
        cmdParams.putAll(buffer.getCombinedParams());

        final NBScriptedCommand cmd = new NBScriptedCommand(parent, phaseName, targetScenario);

        cmd.addScriptText(scriptData);
        return cmd;
    }

    private static NBBaseCommand buildJavaCommand(List<Cmd> cmds, NBComponent parent, String phaseName) {
        if (cmds.size() != 1) {
            throw new RuntimeException("java phases require exactly 1 java command");
        }
        Cmd javacmd = cmds.get(0);
        NBBaseCommand cmd = (NBBaseCommand) NBCommandLoader.init(javacmd.getArg("main_class"), parent, phaseName, javacmd.getTargetContext());
        return cmd;
    }
}
