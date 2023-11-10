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

import io.nosqlbench.components.NBComponent;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBBufferedCommandContext;
import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommand;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedCommand;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;

public class NBCommandAssembly {

    private final static Logger logger = LogManager.getLogger(NBCommandAssembly.class);

    public static record CommandInvocation(NBCommand command, NBCommandParams params, String contextName){};

    public static List<CommandInvocation> preparePhases(List<Cmd> cmds, NBSession parent, Function<String, NBBufferedCommandContext> ctxprovider) {
        List<Cmd> mappedCmds = tagCommandsWithContext(cmds);
        List<CommandInvocation> invocations = prepareMappedPhases(mappedCmds, parent, ctxprovider);
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

    private static List<CommandInvocation> prepareMappedPhases(List<Cmd> mappedCmds, NBComponent parent, Function<String, NBBufferedCommandContext> ctxProvider) {
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

            var phase = switch (cmd.getCmdType()) {
                case java -> buildJavaPhase(List.of(cmd), targetScenario, parent, phaseName);
                case context -> throw new RuntimeException("scenario commands should have already been parsed out.");
                default -> buildJavascriptPhase(List.of(cmd), targetScenario, parent, phaseName);
            };

            String contetName = cmd.getTargetContext();
            parameterizedInvocations.add(new CommandInvocation(phase,params,contetName));
        }
        return parameterizedInvocations;
    }


    private static List<List<Cmd>> splitByCmdTypes(List<Cmd> cmds) {
        LinkedList<Cmd> traverse = new LinkedList<>(cmds);
        List<List<Cmd>> sections = new ArrayList<>();
        while (!traverse.isEmpty()) {
            while ((traverse.peek() != null ? traverse.peek().getCmdType() : null) == Cmd.CmdType.java) {
                ArrayList<Cmd> javacmd = new ArrayList<>(List.of(traverse.removeFirst()));
                sections.add(javacmd);
            }
            if (traverse.isEmpty()) {
                break;
            }
            ArrayList<Cmd> scriptCmd = new ArrayList<>(List.of(traverse.removeFirst()));
            while ((traverse.peek() != null ? traverse.peek().getCmdType() : null) != Cmd.CmdType.java) {
                scriptCmd.add(traverse.removeFirst());
            }
            sections.add(scriptCmd);
        }
        return sections;
    }

    private static NBCommand buildJavascriptPhase(List<Cmd> cmds, String targetScenario, NBComponent parent, String phaseName) {
//        boolean dryrun;
//        NBScriptedScenario.Invocation invocation = dryrun ?
//            NBScriptedScenario.Invocation.RENDER_SCRIPT :
//            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;

        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        final NBCommandParams NBCommandParams = new NBCommandParams();
        NBCommandParams.putAll(buffer.getCombinedParams());

        final NBScriptedCommand scenario = new NBScriptedCommand(parent, phaseName, targetScenario);

        scenario.addScriptText(scriptData);
        return scenario;
    }

    private static NBCommand buildJavaPhase(List<Cmd> cmds, String targetScenario, NBComponent parent, String phaseName) {
        if (cmds.size() != 1) {
            throw new RuntimeException("java phases require exactly 1 java command");
        }
        Cmd javacmd = cmds.get(0);
        String mainClass = javacmd.getArg("main_class");

//        This doesn't work as expected; The newest service loader docs are vague about Provider and no-args ctor requirements
//        and the code suggests that you still have to have one unless you are in a named module
//        SimpleServiceLoader<NBScenario> loader = new SimpleServiceLoader<>(NBScenario.class, Maturity.Any);
//        List<SimpleServiceLoader.Component<? extends NBScenario>> namedProviders = loader.getNamedProviders(mainClass);
//        SimpleServiceLoader.Component<? extends NBScenario> provider = namedProviders.get(0);
//        Class<? extends NBScenario> type = provider.provider.type();

        Class<NBCommand> type;
        try {
            type = (Class<NBCommand>) Class.forName(mainClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            Constructor<? extends NBCommand> constructor = type.getConstructor(NBComponent.class, String.class, String.class);
            NBCommand scenario = constructor.newInstance(parent, phaseName, targetScenario);
            return scenario;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
