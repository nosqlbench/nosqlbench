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

    private static NBCoreInvokableResolver core_resolver = new NBCoreInvokableResolver();

    private final static Logger logger = LogManager.getLogger(NBCommandAssembly.class);

    public static NBCommandParams paramsFor(Cmd cmd) {
        return switch (cmd.getCmdType()) {
            case indirect, java, container -> {
                Map<String, String> params = cmd.getArgMap();
                params.remove("_impl");
                yield NBCommandParams.of(params);
            }
            default -> NBCommandParams.of(Map.of());
        };
    }

    public static record CommandInvocation(NBInvokableCommand command, NBCommandParams params, String containerName) {
    }

    public static List<Cmd> assemble(List<Cmd> cmds, Function<String, NBBufferedContainer> ctxprovider) {
        List<Cmd> mappedCmds = tagCommandsWithContext(cmds);
        NBCoreInvokableResolver core_resolver = new NBCoreInvokableResolver();
        for (Cmd mappedCmd : mappedCmds) {
            core_resolver.verify(mappedCmd);
        }

        return mappedCmds;

//        List<CommandInvocation> invocations = prepareMappedPhases(mappedCmds, ctxprovider);
//        return invocations;
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
                tagged.add(cmd.forContainer(containerName, containerName));
            }
        }
        return new ArrayList<>(tagged);
    }


    public static NBInvokableCommand resolve(Cmd cmd, Function<String, NBBufferedContainer> ctxProvider) {
        try {
            NBCommandParams params = switch (cmd.getCmdType()) {
                case indirect, java, container -> NBCommandParams.of(cmd.getArgMap());
                default -> NBCommandParams.of(Map.of());
            };

            String targetContext = cmd.getTargetContext();
            NBInvokableCommand command = core_resolver.resolve(cmd, ctxProvider.apply(targetContext), cmd.getStepName());
            return command;
        } catch (Exception e) {
            throw new UnresolvedCommand(cmd, e);
        }
    }

    public static CommandInvocation assembleCommand(Cmd cmd, Function<String, NBBufferedContainer> ctxProvider) {
        NBCommandParams params = switch (cmd.getCmdType()) {
            case indirect, java, container -> NBCommandParams.of(cmd.getArgMap());
            default -> NBCommandParams.of(Map.of());
        };

        String targetContext = cmd.getTargetContext();
        NBInvokableCommand command = core_resolver.resolve(cmd, ctxProvider.apply(targetContext), cmd.getStepName());
        if (command == null) {
            throw new BasicError("Found zero commands for spec;" + cmd);
        }
        String containerName = cmd.getTargetContext();

        // TODO, make this unnecessary by moving the impl out of the map to a dedicated cmd structure
        params.remove("_impl");
        return new CommandInvocation(command, params, containerName);
    }

    private static List<CommandInvocation> prepareMappedPhases(List<Cmd> mappedCmds, Function<String, NBBufferedContainer> ctxProvider) {
        List<CommandInvocation> parameterizedInvocations = new ArrayList<>();
        NBCoreInvokableResolver core_resolver = new NBCoreInvokableResolver();
        for (Cmd cmd : mappedCmds) {
            NBCommandParams params = switch (cmd.getCmdType()) {
                case indirect, java, container -> NBCommandParams.of(cmd.getArgMap());
                default -> NBCommandParams.of(Map.of());
            };

            String targetContext = cmd.getTargetContext();
            NBInvokableCommand command = core_resolver.resolve(cmd, ctxProvider.apply(targetContext), cmd.getStepName());
            if (command == null) {
                throw new BasicError("Found zero commands for spec;" + cmd);
            }
            String containerName = cmd.getTargetContext();

            // TODO, make this unnecessary by moving the impl out of the map to a dedicated cmd structure
            params.remove("_impl");
            parameterizedInvocations.add(new CommandInvocation(command, params, containerName));
        }
        return parameterizedInvocations;
    }

}
