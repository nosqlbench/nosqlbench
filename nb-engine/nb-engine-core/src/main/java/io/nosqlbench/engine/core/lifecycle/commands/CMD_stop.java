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

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Optional;

@Service(value = NBBaseCommand.class,selector = "stop")
public class CMD_stop extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("stop");

    public CMD_stop(NBBufferedContainer parentComponent, String stepName, String targetScenario) {
        super(parentComponent, stepName, targetScenario);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        String activityName
            = params.maybeGet("activity").orElseThrow(
            () -> new RuntimeException("The stop command requires an 'activity' parameter")
        );
        Optional<Activity> activity = controller.getActivity(activityName);
        if (activity.isEmpty()) {
            BasicError error = new BasicError("Activity '" + activityName + "' was not found for stop command.");
            logger.warn(error);
            throw error;
        }

        controller.stop(activityName);
        return null;
    }
}
