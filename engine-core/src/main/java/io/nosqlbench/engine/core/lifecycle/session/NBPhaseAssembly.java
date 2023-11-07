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
import io.nosqlbench.components.NBComponentExecutionScope;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioPhaseParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenarioPhase;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenarioPhase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class NBPhaseAssembly {

    private final static Logger logger = LogManager.getLogger(NBPhaseAssembly.class);

    public final static String DEFAULT_SCENARIO_NAME = "default";

    public static record PhaseAndParams(NBScenarioPhase phase, ScenarioPhaseParams params){};

    public static List<PhaseAndParams> preparePhases(List<Cmd> cmds, NBSession parent) {
        List<Cmd> mappedCmds = tagScenarios(cmds);
        List<PhaseAndParams> phases = preparePhases(mappedCmds, parent);
        return phases;
    }

    private static List<Cmd> tagScenarios(List<Cmd> cmds) {
        LinkedList<Cmd> tagged = new LinkedList<>(cmds);
        String scenarioName = DEFAULT_SCENARIO_NAME;
        ListIterator<Cmd> cmdIter = tagged.listIterator();
        while (cmdIter.hasNext()) {
            Cmd cmd = cmdIter.next();
            if (cmd.getCmdType() == Cmd.CmdType.scenario) {
                scenarioName = cmd.getArg("scenario_name");
                if (scenarioName.equals(DEFAULT_SCENARIO_NAME)) {
                    logger.warn("You are explicitly setting the scenario name to 'main'. This is likely an error. This is the default scenario name, and if you are using different scenario names you should pick something that is different and specific.");
                }
                cmdIter.previous();
                cmdIter.remove();
            } else {
                cmd.getParams().put("scenario", scenarioName);
            }
        }
        return new ArrayList<>(tagged);
    }

    private static List<NBScenarioPhase> preparePhases(List<Cmd> mappedCmds, NBComponent parent) {
        List<NBScenarioPhase> phases = new ArrayList<>();
        String basename="phase_";
        int count=0;
        for (Cmd cmd : mappedCmds) {
            count++;
            String phaseName=basename+String.valueOf(count);
            String targetScenario = cmd.getParams().remove("scenario");
            var phase = switch (cmd.getCmdType()) {
                case java -> buildJavaPhase(List.of(cmd), targetScenario, parent, phaseName);
                case scenario -> throw new RuntimeException("scenario commands should have already been parsed out.");
                default -> buildJavascriptPhase(List.of(cmd), targetScenario, parent, phaseName);
            };
            phases.add(phase);
        }
        return phases;
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

    private static NBScenarioPhase buildJavascriptPhase(List<Cmd> cmds, String targetScenario, NBComponent parent, String phaseName) {
//        boolean dryrun;
//        NBScriptedScenario.Invocation invocation = dryrun ?
//            NBScriptedScenario.Invocation.RENDER_SCRIPT :
//            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;

        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        final ScenarioPhaseParams scenarioPhaseParams = new ScenarioPhaseParams();
        scenarioPhaseParams.putAll(buffer.getCombinedParams());

        final NBScriptedScenarioPhase scenario = new NBScriptedScenarioPhase(parent, phaseName, targetScenario);

        scenario.addScriptText(scriptData);
        return scenario;
    }

    private static NBScenarioPhase buildJavaPhase(List<Cmd> cmds, String targetScenario, NBComponent parent, String phaseName) {
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

        Class<NBScenarioPhase> type;
        try {
            type = (Class<NBScenarioPhase>) Class.forName(mainClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            Constructor<? extends NBScenarioPhase> constructor = type.getConstructor(NBComponent.class, String.class, String.class);
            NBScenarioPhase scenario = constructor.newInstance(parent, phaseName, targetScenario);
            return scenario;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }




}
