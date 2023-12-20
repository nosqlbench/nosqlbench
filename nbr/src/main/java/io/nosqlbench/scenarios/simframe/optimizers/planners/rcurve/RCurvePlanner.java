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
import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.components.events.ParamChange;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.scenarios.simframe.capture.JournalView;
import io.nosqlbench.scenarios.simframe.capture.SimFrameCapture;
import io.nosqlbench.scenarios.simframe.planning.HoldAndSample;
import io.nosqlbench.scenarios.simframe.planning.SimFrame;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;

import java.util.concurrent.locks.LockSupport;

public class RCurvePlanner extends SimFramePlanner<RCurveConfig, RCurveFrameParams> implements HoldAndSample {
    private RCurveFrameParams lastFrame;

    public RCurvePlanner(NBBaseComponent parent, NBCommandParams params) {
        super(parent,params);
        create().gauge(
            "rcurve_step",
            () -> lastFrame==null ? 0 : (double)lastFrame.step(),
            MetricCategory.Analysis,
            "The current step which the response curve analyzer is on"
        );
        create().gauge(
            "rcurve_maxstep",
            () -> lastFrame==null ? 0 : (double)lastFrame.maxsteps(),
            MetricCategory.Analysis,
            "The maximum step that the response curve analyzer will run"
        );
        create().gauge(
            "rcurve_ratio",
            () -> lastFrame==null ? 0.0 : lastFrame.ratio(),
            MetricCategory.Analysis,
            "The fractional throughput capacity of the current response curve step"
        );
        create().gauge(
            "rcurve_rate",() -> lastFrame==null ? 0.0 : lastFrame.rate(),
            MetricCategory.Analysis,
            "The actual throughput target of the current response curve step"
        );
    }

    @Override
    public RCurveConfig getConfig(NBCommandParams params) {
        return new RCurveConfig(params);
    }

    public RCurveFrameParams initialStep() {
        return new RCurveFrameParams(config.rateForStep(1), 1,config.max_step(),"INITIAL");
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
        if (nextStep<=config.max_step()) {
            return new RCurveFrameParams(config.rateForStep(nextStep),nextStep,config.max_step(),"Advancing to step " + nextStep);
        } else {
            return null;
        }
    }

    @Override
    public void applyParams(RCurveFrameParams params, Activity flywheel) {
        flywheel.onEvent(ParamChange.of(new CycleRateSpec(params.rate(), 1.1d, SimRateSpec.Verb.restart)));
    }

    @Override
    public void holdAndSample(SimFrameCapture capture) {
        logger.info("holding and sampling for " + config.min_sample_seconds() + " seconds");
        LockSupport.parkNanos((long)(config.min_sample_seconds()*1_000_000_000));
        capture.awaitSteadyState();
    }
}
