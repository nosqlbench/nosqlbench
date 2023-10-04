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
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.components.NBComponentSubScope;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.process.NBCLIErrorHandler;
import io.nosqlbench.engine.core.lifecycle.scenario.context.ScriptParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.ScenariosResults;
import io.nosqlbench.engine.core.lifecycle.scenario.script.NBScriptedScenario;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
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
    private final String progressSpec;
    private final boolean wantsDryRun;

    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    public NBSession(
        NBLabeledElement labelContext,
        String sessionName,
        String progressSpec,
        boolean wantsDryRun
    ) {
        super(null, labelContext.getLabels().and("session", sessionName));
        this.sessionName = sessionName;
        this.progressSpec = progressSpec;
        this.wantsDryRun = wantsDryRun;
    }

    public ExecutionResult apply(List<Cmd> cmds) {

        if (cmds.isEmpty()) {
            logger.info("No commands provided.");
        }

        ResultCollector collector = new ResultCollector();

        try (ResultContext results = new ResultContext(collector)) {
            final ScenariosExecutor scenariosExecutor = new ScenariosExecutor(this, "executor-" + sessionName, 1);

            NBScenario scenario;
            if (cmds.get(0).getCmdType().equals(Cmd.CmdType.java)) {
                scenario = buildJavaScenario(cmds, wantsDryRun);
            } else {
                scenario = buildJavacriptScenario(cmds, wantsDryRun);
            }
            try (NBComponentSubScope scope = new NBComponentSubScope(scenario)) {
                assert scenario != null;
                scenariosExecutor.execute(scenario);

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

        }
        return collector.toExecutionResult();
    }


    private NBScenario buildJavacriptScenario(List<Cmd> cmds, boolean dryrun) {
        NBScriptedScenario.Invocation invocation = dryrun ?
            NBScriptedScenario.Invocation.RENDER_SCRIPT :
            NBScriptedScenario.Invocation.EXECUTE_SCRIPT;

        final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
        final String scriptData = buffer.getParsedScript();

        final ScriptParams scriptParams = new ScriptParams();
        scriptParams.putAll(buffer.getCombinedParams());

        final NBScriptedScenario scenario = new NBScriptedScenario(
            sessionName,
            progressSpec,
            scriptParams,
            this,
            invocation
        );

        scenario.addScriptText(scriptData);
        scenario.addScenarioScriptParams(scriptParams);
        return scenario;
    }

    private NBScenario buildJavaScenario(List<Cmd> cmds, boolean dryrun) {
        return null;
    }

}

