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

import io.nosqlbench.engine.cmdstream.Cmd;
import io.nosqlbench.engine.core.lifecycle.ExecutionResult;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBCommandResult;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBInvokableCommand;
import io.nosqlbench.nb.api.components.decorators.NBTokenWords;
import io.nosqlbench.nb.api.components.status.NBHeartbeatComponent;
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabeledElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A session represents a single execution of NoSQLBench, whether episodic or persistent under some service layer.
 * An NBSession takes care of system level concerns like logging, annotations, error reporting, metrics flows, and so
 * on.
 * All NBScenarios are run within an NBSession.
 */
public class NBSession extends NBHeartbeatComponent implements Function<List<Cmd>, ExecutionResult>, NBTokenWords {
    private final static Logger logger = LogManager.getLogger(NBSession.class);
//    private final ClientSystemMetricChecker clientMetricChecker;

    private final Map<String, NBBufferedContainer> containers = new ConcurrentHashMap<>();

    public enum STATUS {
        OK,
        WARNING,
        ERROR
    }

    public NBSession(
        NBLabeledElement labelContext,
        String sessionName,
        Map<String, String> props
    ) {
        super(
            null,
            labelContext.getLabels()
                .and("session", sessionName),
            props,
            "session"
        );

        new NBSessionSafetyMetrics(this);

        create().gauge(
            "session_time",
            () -> (double) System.nanoTime(),
            MetricCategory.Core,
            "session time in nanoseconds"
        );

        bufferOrphanedMetrics = true;
    }


    public ExecutionResult apply(List<Cmd> cmds) {

        // TODO: add container closing command
        // TODO: inject container closing commands after the last command referencing each container
        List<Cmd> assembledCommands = NBCommandAssembly.assemble(cmds, this::getContext);
        ResultCollector collector = new ResultCollector();

        try (ResultContext results = new ResultContext(collector).ok()) {
            for (Cmd cmd : assembledCommands) {
                String explanation = " in context " + cmd.getTargetContext() + ", command '" + cmd.toString() + "'";
                try (NBInvokableCommand command = NBCommandAssembly.resolve(cmd,this::getContext)) {
                    NBCommandParams params = NBCommandAssembly.paramsFor(cmd);
                    NBBufferedContainer container = getContext(cmd.getTargetContext());
                    NBCommandResult cmdResult = container.apply(command, params);
                    results.apply(cmdResult);
                    if (cmdResult.hasException()) {
                        throw cmdResult.getException();
                    }
                } catch (Exception e) {
                    String msg = "While running " + explanation + ", an error occurred: " + e.toString();
                    results.error(e);
                    onError(e);
                    logger.error(msg);
                    break;
                }
            }
        }

        for (String containerName : containers.keySet()) {
            NBBufferedContainer ctx = containers.get(containerName);
            logger.debug("awaiting end of activities in container '" + containerName + "':" +
                ctx.controller().getActivityDefs().stream().map(ActivityDef::getAlias).toList());
            ctx.controller().shutdown();
            ctx.controller().awaitCompletion(Long.MAX_VALUE);
            logger.debug("completed");
        }
        metricsBuffer.printMetricSummary(this);
        return collector.toExecutionResult();
    }


    private NBBufferedContainer getContext(String name) {
        return containers.computeIfAbsent(
            name,
            n -> NBContainer.builder().name(n).build(this)
        );
    }

}

