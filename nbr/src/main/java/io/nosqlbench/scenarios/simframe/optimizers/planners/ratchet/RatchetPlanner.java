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

package io.nosqlbench.scenarios.simframe.optimizers.planners.ratchet;

import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivity;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.scenarios.simframe.capture.JournalView;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;

public class RatchetPlanner extends SimFramePlanner<RatchetConfig, RatchetFrameParams> {
    public RatchetPlanner(NBBaseComponent parent, NBCommandParams params) {
        super(parent, params);
    }


    @Override
    public RatchetConfig getConfig(NBCommandParams params) {
        return new RatchetConfig(params);
    }

    public RatchetFrameParams initialStep() {
        return new RatchetFrameParams(config.rate_base(), 1, config.rate_step(), "INITIAL");
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
    public RatchetFrameParams nextStep(JournalView<RatchetFrameParams> journal) {
        SimFrame<RatchetFrameParams> last = journal.last();
        SimFrame<RatchetFrameParams> best = journal.bestRun();
        if (best.index() == last.index()) { // got better consecutively
            return new RatchetFrameParams(
                last.params().rate() + last.params().step_size(),
                last.params().attempt(),
                last.params().step_size(),
                "CONTINUE after improvement from frame " + last.index()
            );
        } else if (last.params().step_size() > config.rate_minstep()) {
            double newStepSize = best.params().step_size() * config.rate_scaledown();
            return new RatchetFrameParams(
                best.params().rate() + newStepSize, best.params().attempt(), newStepSize,
                "SMALLER-STEP: " + newStepSize + " from frame " + best.index()
            );
        } else if (last.params().attempt() < config.max_attempts()) {
            return new RatchetFrameParams(
                config.rate_base(), last.params().attempt() + 1, config.rate_step(), "NEXT ATTEMPT after FRAME " + last.index()
            );
        } else {
            return null;
        }
    }


    @Override
    public void applyParams(RatchetFrameParams params, StandardActivity flywheel) {
        flywheel.onEvent(ParamChange.of(new CycleRateSpec(params.rate(), 1.1d, SimRateSpec.Verb.restart)));

    }
}
