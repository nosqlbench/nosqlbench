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

package io.nosqlbench.scenarios.simframe.findmax.survey;

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

public class FindmaxSurvey extends SimFramePlanner<SurveyConfig, SurveyFrameParams> {
    private final Logger logger = LogManager.getLogger(FindmaxSurvey.class);

    public FindmaxSurvey(NBCommandParams params) {
        super(params);
    }


    @Override
    public SurveyConfig getConfig(NBCommandParams params) {
        return new SurveyConfig(params);
    }

    public SurveyFrameParams initialStep() {
        return new SurveyFrameParams(config.max_rate(), config.steps(), "INITIAL");
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
    public SurveyFrameParams nextStep(JournalView<SurveyFrameParams> journal) {
        return null;
    }


    @Override
    public void applyParams(SurveyFrameParams params, Activity flywheel) {
        flywheel.onEvent(ParamChange.of(new CycleRateSpec(params.rate(), 1.1d, SimRateSpec.Verb.restart)));

    }
}
