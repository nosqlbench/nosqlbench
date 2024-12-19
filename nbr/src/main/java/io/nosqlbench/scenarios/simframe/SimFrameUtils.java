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

package io.nosqlbench.scenarios.simframe;

import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.api.activityimpl.uniform.Activity;
import io.nosqlbench.engine.core.lifecycle.scenario.container.ContainerActivitiesController;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.engine.metrics.instruments.NBMetricTimer;

import java.util.Optional;
import java.util.concurrent.locks.LockSupport;

public class SimFrameUtils {
    public static final String SIM_CYCLES = "sim_cycles";

    public static void awaitActivity(Activity flywheel) {
        // await flywheel actually spinning, or timeout with error
        NBMetricTimer result_success_timer = flywheel.find().timer("name:result_success");
        for (int i = 0; i < 1000; i++) {
            if (result_success_timer.getCount() > 0) {
                System.out.printf("saw traffic after %.2f seconds\n", ((double) i / 10));
                break;
            }
            LockSupport.parkNanos(100_000_000);
        }
        if (result_success_timer.getCount() == 0) {
            throw new RuntimeException("Unable to see traffic on activity " + flywheel.getAlias() + " after 10 seconds.");
        }
    }

    public static Activity findFlywheelActivity(ContainerActivitiesController controller, String providedActivityName) {
        Optional<Activity> optionalActivity = Optional.ofNullable(providedActivityName).flatMap(controller::getActivity);
        if (providedActivityName!=null && optionalActivity.isEmpty()) {
            throw new RuntimeException("you specified activity '" + providedActivityName + "' but it was not found.");
        }
        Activity flywheel = optionalActivity.or(controller::getSoloActivity)
            .orElseThrow(() -> new RuntimeException("You didn't provide the name of an activity to attach to, nor was there a solo activity available in this context"));

        // Start the flywheel at an "idle" speed, even if the user hasn't set it
        flywheel.onEvent(new ParamChange<>(new CycleRateSpec(100.0d, 1.1d, SimRateSpec.Verb.restart)));
        flywheel.getActivityDef().setEndCycle(Long.MAX_VALUE);
        flywheel.getActivityDef().getParams().set(SIM_CYCLES, Long.MAX_VALUE);

        return flywheel;
    }
}
