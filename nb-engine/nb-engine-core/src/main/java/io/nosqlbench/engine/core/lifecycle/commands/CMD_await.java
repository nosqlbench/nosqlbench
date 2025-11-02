/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.engine.core.lifecycle.commands;

import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.engine.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.concurrent.locks.LockSupport;

@Service(value = NBBaseCommand.class, selector = "await")
public class CMD_await extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("await");

    public CMD_await(NBBufferedContainer parentComponent, String stepName, String targetScenario) {
        super(parentComponent, stepName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        // Extract the activity name from the 'activity' parameter
        String activityName = params.maybeGet("activity").orElseThrow(
            () -> new RuntimeException("The await command requires an 'activity' parameter")
        );

        // Extract optional timeout parameter (default to Long.MAX_VALUE)
        long timeoutMs = params.maybeGet("s")
            .or(() -> params.maybeGet("seconds"))
            .map(s -> Unit.msFor(s).orElseThrow(() ->
                new RuntimeException("Invalid timeout value: " + s)))
            .orElse(Long.MAX_VALUE);

        controller.awaitActivity(activityName, timeoutMs);
        return null;
    }
}
