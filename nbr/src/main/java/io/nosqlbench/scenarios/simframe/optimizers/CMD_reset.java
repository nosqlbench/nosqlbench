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
import io.nosqlbench.nb.api.engine.activityimpl.ActivityDef;
import io.nosqlbench.nb.api.engine.activityimpl.ParameterMap;
import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.scenarios.simframe.SimFrameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.Reader;
import java.util.Map;
import java.util.Optional;

@Service(value = NBBaseCommand.class,selector = "reset")
public class CMD_reset extends NBBaseCommand {
    public final static Logger logger = LogManager.getLogger("reset");

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

        //if (flywheel.getActivityDef().getParams().getOptionalString("cycles").isEmpty()) {
        //params   .getOptionalLong("rate")
        flywheel.onEvent(new ParamChange<>(new CycleRateSpec(100.0d, 1.1d, SimRateSpec.Verb.restart)));
        flywheel.getActivityDef().setEndCycle(Long.parseLong((String) flywheel.getActivityDef().getParams().get("cycles")));

        SimFrameUtils.awaitActivity(flywheel);
        NBCommandParams newParams = NBCommandParams.of(flywheel.getActivityDef().getParams().getStringStringMap());
//        controller.run(newParams);
        return null;
    }
}
