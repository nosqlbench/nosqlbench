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
import io.nosqlbench.components.NBBaseComponent;
import io.nosqlbench.api.engine.metrics.ActivityMetrics;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponentSubScope;
import io.nosqlbench.engine.cli.BasicScriptBuffer;
import io.nosqlbench.engine.cli.Cmd;
import io.nosqlbench.engine.cli.ScriptBuffer;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.process.NBCLIErrorHandler;
import io.nosqlbench.engine.core.lifecycle.process.ShutdownManager;
import io.nosqlbench.engine.core.lifecycle.scenario.NBScenario;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosExecutor;
import io.nosqlbench.engine.core.lifecycle.scenario.ScenariosResults;
import io.nosqlbench.engine.core.lifecycle.scenario.script.ScriptParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
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
    private final String reportSummaryTo;
    private final Path logspath;
    private final boolean wantsShowScript;
    private final String scriptfile;


    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    public NBSession(
        NBComponent parent,
        String sessionName,
        String progressSpec,
        String reportSummaryTo,
        Path logspath,
        String scriptfile,
        boolean wantsShowScript
    ) {
        super(parent, NBLabels.forKV("session", sessionName));
        this.sessionName = sessionName;
        this.progressSpec = progressSpec;
        this.reportSummaryTo = reportSummaryTo;
        this.logspath = logspath;
        this.scriptfile = scriptfile;
        this.wantsShowScript = wantsShowScript;
    }

    public ExecutionResult apply(List<Cmd> cmds) {
        ResultCollector collector = new ResultCollector();

        try (ResultContext results = new ResultContext(collector)) {
            final ScenariosExecutor scenariosExecutor = new ScenariosExecutor("executor-" + sessionName, 1);
            NBScenario.Invocation invocation = wantsShowScript ? NBScenario.Invocation.RENDER_SCRIPT : NBScenario.Invocation.EXECUTE_SCRIPT;

            final NBScenario scenario = new NBScenario(
                sessionName,
                progressSpec,
                reportSummaryTo,
                logspath,
                scriptfile,
                this,
                invocation
            );
            try (NBComponentSubScope s = new NBComponentSubScope(scenario)) {

                final ScriptBuffer buffer = new BasicScriptBuffer().add(cmds.toArray(new Cmd[0]));
                final String scriptData = buffer.getParsedScript();

                // Execute Scenario!
                if (cmds.isEmpty()) {
                    logger.info("No commands provided.");
                }

                scenario.addScriptText(scriptData);
                final ScriptParams scriptParams = new ScriptParams();
                scriptParams.putAll(buffer.getCombinedParams());
                scenario.addScenarioScriptParams(scriptParams);
                scenariosExecutor.execute(scenario);
                final ScenariosResults scenariosResults = scenariosExecutor.awaitAllResults();
                logger.debug(() -> "Total of " + scenariosResults.getSize() + " result object returned from ScenariosExecutor");

                ActivityMetrics.closeMetrics();
                scenariosResults.reportToLog();
                ShutdownManager.shutdown();

                logger.info(scenariosResults.getExecutionSummary());


                if (scenariosResults.hasError()) {
                    results.error(scenariosResults.getAnyError().orElseThrow());
                    final Exception exception = scenariosResults.getOne().getException();
                    logger.warn(scenariosResults.getExecutionSummary());
                    NBCLIErrorHandler.handle(exception, true);
                    System.err.println(exception.getMessage()); // TODO: make this consistent with ConsoleLogging sequencing
                }

                results.output(scenariosResults.getExecutionSummary());

            }

        }
        return collector.toExecutionResult();
    }

}
