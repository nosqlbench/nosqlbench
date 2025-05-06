/*
 * Copyright (c) 2023-2024 nosqlbench
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
 *
 */

package io.nosqlbench.scenarios.simframe.optimizers;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBBufferedContainer;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.engine.core.lifecycle.scenario.execution.NBBaseCommand;
import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.components.events.SetThreads;
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service(value = NBBaseCommand.class,selector = "reset")
public class CMD_reset extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("reset");
    private static final HashSet<String> IGNORABLE = new HashSet<>(List.of("activity"));

    public CMD_reset(NBBufferedContainer parentComponent, String scenarioName, String context) {
        super(parentComponent, scenarioName, context);
    }

    /**
     * This command is used to restart the initial step activity in a scenario after optimization results have
     * been determined by the previous steps. Any parameters that should be modified from the initially specified
     * parameters for the activity should be specified on the command line, typically using bindings to reference
     * the outputs of the previous steps, although this is not mandatory.
     * @param params
     * @param stdout
     * @param stderr
     * @param stdin
     * @param controller
     * @return null
     */
    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Optional<Activity> optionalActivity = Optional.ofNullable(params.get("activity")).flatMap(controller::getActivity);
        if (params.get("activity")!=null && optionalActivity.isEmpty()) {
            throw new RuntimeException("you specified activity '" + params.get("activity") + "' but it was not found.");
        }
        try (Activity flywheel = optionalActivity.or(controller::getSoloActivity)
                .orElseThrow(() -> new RuntimeException("You didn't provide the name of an activity to attach to, nor was there a solo activity available in this context"))) {

            params.forEach((key, value) -> {
                switch (key) {
                    case "rate" -> {
                        logger.debug("Resetting rate to " + value + " cycles per second");
                        flywheel.onEvent(new ParamChange<>(new CycleRateSpec(Double.parseDouble(value), 1.1d, SimRateSpec.Verb.restart)));
                    }
                    case "threads" -> {
                        logger.debug("Resetting threads to " + value + " threads");
                        flywheel.onEvent(ParamChange.of(new SetThreads((int) Math.round(Double.parseDouble(value)))));
                    }
                    default -> {
                        if (!IGNORABLE.contains(key)) {
                            logger.debug("Resetting parameter: " + key + " to " + value);
                            flywheel.getActivityDef().getParams().put(key, value);
                        }
                    }
                }
            });

            // Get the original cycle count and re-apply it
            long cycles = Long.parseLong((String) flywheel.getActivityDef().getParams().get("cycles"));
            logger.debug("Resetting cycle count to " + cycles + " cycles");
            flywheel.getActivityDef().setEndCycle(cycles);

            //TODO: This needs to be reworked, but simply calling controller.start on the flywheel results in 2
            //      copies of the activity running simultaneously. This is a temporary workaround.
            SimFrameUtils.awaitActivity(flywheel);
            flywheel.getMotorDispenserDelegate().getMotor(flywheel.getActivityDef(), 0).run();
        }

        return null;
    }
}
