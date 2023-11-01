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

package io.nosqlbench.scenarios.simframe.optimo;

import io.nosqlbench.scenarios.simframe.capture.JournalView;
import io.nosqlbench.scenarios.simframe.findmax.FindMaxFrameParams;
import io.nosqlbench.scenarios.simframe.findmax.FindmaxSearchParams;
import io.nosqlbench.scenarios.simframe.planning.SimFramePlanner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OptimoPlanner extends SimFramePlanner<OptimoSearchParams, OptimoFrameParams> {
    private final Logger logger = LogManager.getLogger(OptimoPlanner.class);

    public OptimoPlanner(OptimoSearchParams plannerConfig) {
        super(plannerConfig);
    }

    @Override
    public OptimoFrameParams initialStep() {
        return new OptimoFrameParams(100.0,5000);

    }

    /**
     * Using a stateful history of all control parameters and all results, decide if there
     * is additional search space and return a set of parameters for the next workload
     * simulation frame. If the stopping condition has been met, return null
     *
     * @param journal
     *     All parameters and results, organized in enumerated simulation frames
     * @return Optionally, a set of params which indicates another simulation frame should be sampled, else null
     */
    public OptimoFrameParams nextStep(JournalView journal) {
        return null;
    }

}
