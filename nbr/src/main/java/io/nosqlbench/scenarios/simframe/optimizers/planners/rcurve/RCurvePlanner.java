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

package io.nosqlbench.scenarios.simframe.optimizers.planners.rcurve;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.ratelimits.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.scenarios.simframe.capture.JournalView;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RCurvePlanner extends SimFramePlanner<RCurveConfig, RCurveFrameParams> {
    private final Logger logger = LogManager.getLogger(RCurvePlanner.class);

    public RCurvePlanner(NBCommandParams params) {
        super(params);
    }


    @Override
    public RCurveConfig getConfig(NBCommandParams params) {
        return new RCurveConfig(params);
    }

    public RCurveFrameParams initialStep() {
        return new RCurveFrameParams(config.rateForStep(1), 1, "INITIAL");
    }

    /**
     * Using a stateful history of all control parameters and all results, decide if there
     * is additional search space and return a set of parameters for the next workload
     * simulation frame. If the stopping condition has been met, return null
     *
     * @param journal
     *     All parameters and results, organized in enumerated simulation frames
     * @return Optionally, a set of paramValues which indicates another simulation frame should be sampled, else null
     */

    @Override
    public RCurveFrameParams nextStep(JournalView<RCurveFrameParams> journal) {
        SimFrame<RCurveFrameParams> last = journal.last();
        int nextStep = last.params().step() +1;
        if (nextStep<=config.steps()) {
            return new RCurveFrameParams(config.rateForStep(nextStep),nextStep,"Advancing to step " + nextStep);
        } else {
            return null;
        }
    }

    @Override
    public void applyParams(RCurveFrameParams params, Activity flywheel) {
        flywheel.onEvent(ParamChange.of(new CycleRateSpec(params.rate(), 1.1d, SimRateSpec.Verb.restart)));
    }
}
