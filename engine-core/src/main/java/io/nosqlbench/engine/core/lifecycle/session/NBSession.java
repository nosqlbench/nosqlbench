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

import io.nosqlbench.api.labels.NBLabeledElement;
import io.nosqlbench.api.spi.SimpleServiceLoader;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponentSubScope;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.process.NBCLIErrorHandler;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosResults;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenario;
import io.nosqlbench.nb.annotations.Maturity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A session represents a single execution of NoSQLBench, whether episodic or persistent under some service layer.
 * An NBSession takes care of system level concerns like logging, annotations, error reporting, metrics flows, and so
 * on.
 * All NBScenarios are run within an NBSession.
 */
public class NBSession extends NBBaseComponent implements Function<List<Cmd>, ExecutionResult> {
    private final static Logger logger = LogManager.getLogger(NBSession.class);
    private final String sessionName;

    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    public NBSession(
        NBLabeledElement labelContext,
        String sessionName
    ) {
        super(null, labelContext.getLabels().and("session", sessionName));
        this.sessionName = sessionName;
    }

    public ExecutionResult apply(List<Cmd> cmds) {

        if (cmds.isEmpty()) {
            logger.info("No commands provided.");
        }

        Map<String, String> params = new CmdParamsBuffer(cmds).getGlobalParams();

        ResultCollector collector = new ResultCollector();

        try (ResultContext results = new ResultContext(collector)) {
            final ScenariosExecutor scenariosExecutor = new ScenariosExecutor(this, "executor-" + sessionName, 1);

            NBScenario scenario;
            if (cmds.get(0).getCmdType().equals(Cmd.CmdType.java)) {
                scenario = buildJavaScenario(cmds);
            } else {
                scenario = buildJavacriptScenario(cmds);
            }
            try (NBComponentSubScope scope = new NBComponentSubScope(scenario)) {
                assert scenario != null;
                scenariosExecutor.execute(scenario,params);

                //             this.doReportSummaries(this.reportSummaryTo, this.result);
            }
            final ScenariosResults scenariosResults = scenariosExecutor.awaitAllResults();
            logger.debug(() -> "Total of " + scenariosResults.getSize() + " result object returned from ScenariosExecutor");

//            ActivityMetrics.closeMetrics();
//            scenariosResults.reportToLog();
//            ShutdownManager.shutdown();
//
//            logger.info(scenariosResults.getExecutionSummary());

            if (scenariosResults.hasError()) {
                results.error(scenariosResults.getAnyError().orElseThrow());
                final Exception exception = scenariosResults.getOne().getException();
                logger.warn(scenariosResults.getExecutionSummary());
                NBCLIErrorHandler.handle(exception, true);
                System.err.println(exception.getMessage()); // TODO: make this consistent with ConsoleLogging sequencing
            }

            results.output(scenariosResults.getExecutionSummary());
            results.ok();

        }
        return collector.toExecutionResult();
    }


    private NBScenario buildJavacriptScenario(List<Cmd> cmds) {
//        boolean dryrun;
//        NBScriptedScenario.Invocation invocation = dryrun ?
//            NBScriptedScenario.Invocation.RENDER_SCRIPT :
//            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;

        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        final ScenarioParams scenarioParams = new ScenarioParams();
        scenarioParams.putAll(buffer.getCombinedParams());

        final NBScriptedScenario scenario = new NBScriptedScenario(sessionName, this);

        scenario.addScriptText(scriptData);
        return scenario;
    }

    private NBScenario buildJavaScenario(List<Cmd> cmds) {
        if (cmds.size()!=1) {
            throw new RuntimeException("java scenarios require exactly 1 java command");
        }
        Cmd javacmd = cmds.get(0);
        String mainClass = javacmd.getArg("main_class");

//        This doesn't work as expected; The newest service loader docs are vague about Provider and no-args ctor requirements
//        and the code suggests that you still have to have one unless you are in a named module
//        SimpleServiceLoader<NBScenario> loader = new SimpleServiceLoader<>(NBScenario.class, Maturity.Any);
//        List<SimpleServiceLoader.Component<? extends NBScenario>> namedProviders = loader.getNamedProviders(mainClass);
//        SimpleServiceLoader.Component<? extends NBScenario> provider = namedProviders.get(0);
//        Class<? extends NBScenario> type = provider.provider.type();

        Class<NBScenario> type;
        try {
            type = (Class<NBScenario>) Class.forName(mainClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            Constructor<? extends NBScenario> constructor = type.getConstructor(NBComponent.class, String.class);
            NBScenario scenario = constructor.newInstance(this, sessionName);
            return scenario;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}

