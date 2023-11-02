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

package io.nosqlbench.scenarios.simframe.planning;

import io.nosqlbench.scenarios.simframe.capture.JournalView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A frame planner is what decides what next set of parameters to try based on a history
 * of simulation frames, and whether to proceed with another sim frame.
 * @param <C> The configuration type for the planner
 * @param <P> The parameter set type for the planner, emitted for each time another sim frame should be run
 */
public abstract class SimFramePlanner<C,P> {
    private final Logger logger = LogManager.getLogger(SimFramePlanner.class);
    protected final C config;

    public SimFramePlanner(C plannerConfig) {
        this.config = plannerConfig;
    }

    public abstract P initialStep();

    /**
     * Using a stateful history of all control parameters and all results, decide if there
     * is additional search space and return a set of parameters for the next workload
     * simulation frame. If the stopping condition has been met, return null
     *
     * @param journal
     *     All parameters and results, organized in enumerated simulation frames
     * @return Optionally, a set of paramValues which indicates another simulation frame should be sampled, else null
     */
    public abstract P nextStep(JournalView<P> journal);

}
