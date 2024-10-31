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

package io.nosqlbench.scenarios.simframe.optimizers.planners.findmax;

import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.simrate.CycleRateSpec;
import io.nosqlbench.engine.api.activityapi.simrate.SimRateSpec;
import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.scenarios.simframe.capture.JournalView;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;

import java.util.Comparator;

public class FindmaxPlanner extends SimFramePlanner<FindmaxConfig, FindmaxFrameParams> {
    public FindmaxPlanner(NBBaseComponent parent, NBCommandParams analyzerParams) {
        super(parent, analyzerParams);
    }

    @Override
    public FindmaxConfig getConfig(NBCommandParams params) {
        return new FindmaxConfig(params);
    }

    public FindmaxFrameParams initialStep() {
        return new FindmaxFrameParams(
            config.rate_base(), config.rate_step(), config.sample_time_ms(), config.min_settling_ms(), "INITIAL"
        );
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
    public FindmaxFrameParams nextStep(JournalView<FindmaxFrameParams> journal) {
        SimFrame<FindmaxFrameParams> last = journal.last();
        SimFrame<FindmaxFrameParams> best = journal.bestRun();
        if (best.index() == last.index()) { // got better consecutively
            return new FindmaxFrameParams(
                last.params().rate_shelf(),
                last.params().rate_delta() * config.rate_incr(),
                last.params().sample_time_ms(),
                config.min_settling_ms(),
                "CONTINUE after improvement from frame " + last.index()
            );
        } else if (best.index() == last.index() - 1) {
            // got worse consecutively, this may be collapsed out since the general case below covers it (test first)
            if ((last.params().computed_rate() - best.params().computed_rate()) <= config.rate_step()) {
                logger.info("could not divide search space further, stop condition met");
                return null;
            } else {
                return new FindmaxFrameParams(
                    best.params().computed_rate(),
                    config.rate_step(),
                    (long) (last.params().sample_time_ms() * config.sample_incr()),
                    config.min_settling_ms()*4,
                    "REBASE search range to new base after frame " + best.index()
                );
            }
        } else { // any other case
            // find next frame with higher rate but lower value, the closest one by rate
            SimFrame<FindmaxFrameParams> nextWorseFrameWithHigherRate = journal.frames().stream()
                    .filter(f -> f.value() < best.value())
                    .filter(f -> f.params().computed_rate() > best.params().computed_rate())
                .min(Comparator.comparingDouble(f -> f.params().computed_rate()))
                .orElseThrow(() -> new RuntimeException("inconsistent samples"));
            if ((nextWorseFrameWithHigherRate.params().computed_rate() - best.params().computed_rate()) > config.rate_step()) {
                return new FindmaxFrameParams(
                    best.params().computed_rate(),
                    config.rate_step(),
                    (long) (last.params().sample_time_ms() * config.sample_incr()),
                    config.min_settling_ms()* 2,
                    "REBASE search range from frames " + best.index() + " ➞ " +nextWorseFrameWithHigherRate.index()
                );
            } else {
                logger.info("could not divide search space further, stop condition met");
                return null;
            }
        }
    }

    @Override
    public void applyParams(FindmaxFrameParams params, Activity flywheel) {
        flywheel.onEvent(ParamChange.of(new CycleRateSpec(params.rate_shelf()+params.rate_delta(), 1.1d, SimRateSpec.Verb.restart)));
    }


}
