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

import io.nosqlbench.engine.cmdstream.*;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

public class NBCommandAssembly {

    private final static Logger logger = LogManager.getLogger(NBCommandAssembly.class);

    public static record CommandInvocation(NBInvokableCommand command, NBCommandParams params, String containerName) {
    }

    public static List<CommandInvocation> assemble(List<Cmd> cmds, Function<String, NBBufferedContainer> ctxprovider) {
        List<Cmd> mappedCmds = tagCommandsWithContext(cmds);
        List<CommandInvocation> invocations = prepareMappedPhases(mappedCmds, ctxprovider);
        return invocations;
    }

    private static List<Cmd> tagCommandsWithContext(List<Cmd> cmds) {
        LinkedList<Cmd> tagged = new LinkedList<>();
        String containerName = Cmd.DEFAULT_TARGET_CONTEXT;
        for (Cmd cmd : cmds) {

            if (cmd.getArgs().containsKey("container")) {
                String specificContainer = cmd.getArgs().remove("container").getValue();
                String step = cmd.getArgs().containsKey("step") ? cmd.getArgs().remove("step").getValue() : "no-step";
                tagged.add(cmd.forContainer(specificContainer, step));
            } else if (cmd.getCmdType() == CmdType.container) {
                containerName = cmd.getArgValue("name");
                if (containerName.equals(Cmd.DEFAULT_TARGET_CONTEXT)) {
                    logger.warn("You are explicitly setting the scenario name to " + Cmd.DEFAULT_TARGET_CONTEXT + "'. This is likely an error. " +
                        "This is the default scenario name, and if you are using different scenario names you should pick something that is different and specific.");
                }
            } else {
                tagged.add(cmd.forContainer(containerName, null));
            }
        }
        return new ArrayList<>(tagged);
    }

    private static List<CommandInvocation> prepareMappedPhases(List<Cmd> mappedCmds, Function<String, NBBufferedContainer> ctxProvider) {
        List<CommandInvocation> parameterizedInvocations = new ArrayList<>();
        NBCoreInvokableResolver core_resolver = new NBCoreInvokableResolver();
        String basename = "phase_";
        int count = 0;
        for (Cmd cmd : mappedCmds) {
            count++;
            String phaseName = basename + count;

            NBCommandParams params = switch (cmd.getCmdType()) {
                case indirect, java, container -> NBCommandParams.of(cmd.getArgMap());
                default -> NBCommandParams.of(Map.of());
            };

            String targetContext = cmd.getTargetContext();
            NBInvokableCommand command = core_resolver.resolve(cmd, ctxProvider.apply(targetContext), phaseName);
            if (command==null) {
                throw new BasicError("Found zero commands for spec;" + cmd);
            }
            String containerName = cmd.getTargetContext();
            parameterizedInvocations.add(new CommandInvocation(command, params, containerName));
        }
        return parameterizedInvocations;
    }

//    private static NBBaseCommand buildJavascriptCommand(List<Cmd> cmds, String targetScenario, NBBufferedCommandContext parent, String phaseName) {
////        boolean dryrun;
////        NBScriptedScenario.Invocation invocation = dryrun ?
////            NBScriptedScenario.Invocation.RENDER_SCRIPT :
////            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;
//
//        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
//        final String scriptData = buffer.getParsedScript();
//
//        final NBCommandParams cmdParams = new NBCommandParams();
//        cmdParams.putAll(buffer.getCombinedParams());
//
//        final NBScriptedCommand cmd = new NBScriptedCommand(parent, phaseName, targetScenario);
//
//        cmd.addScriptText(scriptData);
//        return cmd;
//    }

//    private static NBBaseCommand buildJavaCommand(List<Cmd> cmds, NBComponent parent, String phaseName) {
//        if (cmds.size() != 1) {
//            throw new RuntimeException("java phases require exactly 1 java command");
//        }
//        Cmd javacmd = cmds.get(0);
//        NBBaseCommand cmd = (NBBaseCommand) NBJavaCommandLoader.init(javacmd.getArgValue("_impl"), parent, phaseName, javacmd.getTargetContext());
//        return cmd;
//    }
}
