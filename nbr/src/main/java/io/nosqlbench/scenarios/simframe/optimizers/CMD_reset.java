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
import java.util.Optional;

@Service(value = NBBaseCommand.class,selector = "reset")
public class CMD_reset extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("reset");
    public static final String DEFAULT_RATE = "100";
    public static final String DEFAULT_THREADS = "10";

    public CMD_reset(NBBufferedContainer parentComponent, String scenarioName, String context) {
        super(parentComponent, scenarioName, context);
    }

    @Override
    public Object invoke(NBCommandParams params, PrintWriter stdout, PrintWriter stderr, Reader stdin, ContainerActivitiesController controller) {
        Optional<Activity> optionalActivity = Optional.ofNullable(params.get("activity")).flatMap(controller::getActivity);
        if (params.get("activity")!=null && optionalActivity.isEmpty()) {
            throw new RuntimeException("you specified activity '" + params.get("activity") + "' but it was not found.");
        }
        Activity flywheel = optionalActivity.or(controller::getSoloActivity)
            .orElseThrow(() -> new RuntimeException("You didn't provide the name of an activity to attach to, nor was there a solo activity available in this context"));

        /*
         Set the CycleRateSpec. This should be found in params.get("rate") if the value from a previous step is specified.
         If no value from a previous step is used, the original can be found in the flywheel activity definition.
         If a value is passed in on the command line as opposed to specified in the yaml file it will override other values, so don't do this.

         cli rate|   yaml rate|  reset rate| params rate| flywheel params rate
         null	 |   1	      |  50	       | 50	        | 1
         null	 |   null	  |  50	       | 50	        | null
         null	 |   1	      |  null	   | null	    | 1
         1	     |   5	      |  50	       | 1	        | 1
         1	     |   null	  |  50	       | 1	        | 1
         */
        String rateStr = params.hasMember("rate") ? params.get("rate") :
            flywheel.getActivityDef().getParams().getOptionalString("rate").orElse(DEFAULT_RATE);
        logger.debug("Resetting rate to " + rateStr + " cycles per second");
        flywheel.onEvent(new ParamChange<>(new CycleRateSpec(Double.parseDouble(rateStr), 1.1d, SimRateSpec.Verb.restart)));

        // Get the original cycle count and re-apply it
        long cycles = Long.parseLong((String) flywheel.getActivityDef().getParams().get("cycles"));
        logger.debug("Resetting cycle count to " + cycles + " cycles");
        flywheel.getActivityDef().setEndCycle(cycles);

        /*
         Set the thread count. This should be found in params and the flywheel if the value from a previous step is specified.
         If no value from a previous step is used, the original can be found in the flywheel activity definition.
         If a value is passed in on the command line as opposed to specified in the yaml file it will be found in params
         and the correct optimo thread count can be found in the flywheel, so in this case we go to the flywheel first

         cli threads|   yaml threads|  reset threads| params threads| flywheel params threads
         null	    |   1	        |  50	        | 50	        | 50
         null	    |   null	    |  50	        | 50	        | 50
         null	    |   1	        |  null	        | null	        | 1
         1	        |   5	        |  50	        | 1	            | 50
         1	        |   null	    |  50	        | 1	            | 50
         */

        String threadStr = flywheel.getActivityDef().getParams().getOptionalString("threads")
                .orElse(params.hasMember("threads") ? params.get("threads") : DEFAULT_THREADS);
        logger.debug("Resetting threads to " + threadStr);
        flywheel.onEvent(ParamChange.of(new SetThreads((int)Math.round(Double.parseDouble(threadStr)))));

        SimFrameUtils.awaitActivity(flywheel);
        flywheel.getMotorDispenserDelegate().getMotor(flywheel.getActivityDef(), 0).run();

        return null;
    }
}
