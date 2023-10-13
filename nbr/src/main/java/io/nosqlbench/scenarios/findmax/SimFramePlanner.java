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

package io.nosqlbench.scenarios.findmax;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SimFramePlanner {
    private final Logger logger = LogManager.getLogger(SimFramePlanner.class);


    /**
     * Search params which control findmax
     */
    private final FindmaxSearchParams findmax;
    /**
     * A history of execution parameters by step
     */

    /**
     * A history of execution results by step
     */

    public SimFramePlanner(FindmaxSearchParams findMaxSettings) {
        this.findmax = findMaxSettings;

    }

    public boolean conditionsMet() {

        return false;
    }

    public SimFrameParams initialStep() {
        return new SimFrameParams(
            this.findmax.rate_base(), this.findmax.rate_step(),
            this.findmax.sample_time_ms()
        );
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
    public SimFrameParams nextStep(JournalView journal) {
        List<SimFrame> frames = journal.frames();
        if (frames.size() < 2) {
            System.out.println("FIRSTTWO");
            return new SimFrameParams(
                journal.last().params().rate_shelf(),
                journal.last().params().rate_shelf() + (journal.last().params().rate_delta() * findmax.rate_incr()),
                journal.last().params().sample_time_ms()
            );
        }
        // if last result was better than the one before it
        // increment from settings
        // else if the new base would be higher than the initial rate_step
        // rebase
        // else return null;
        SimFrame last = journal.last();
        SimFrame before = journal.beforeLast();
        if (before.value() < last.value()) { // got a better result, keep on keepin' on
            System.out.println("CONTINUE");
            return new SimFrameParams(
                last.params().rate_shelf(),
                last.params().rate_delta() * findmax.rate_incr(),
                last.params().sample_time_ms()
            );
        } else { // reset to last better result as base and start again
            if (last.params().rate_delta() > findmax.rate_step()) { // but only if there is still searchable space
                System.out.println("REBASE");
                return new SimFrameParams(
                    before.params().computed_rate(),
                    findmax.rate_step(),
                    (long) (before.params().sample_time_ms() * findmax.sample_incr()));
            } else {
                // but only if there is still unsearched resolution within rate_step
                logger.info("could not divide search space further, stop condition met");
                System.out.println("STOP CONDITION");
                return null;

            }
        }
    }

    private SimFrameParams nextStepParams(SimFrame previous) {
        return null;
    }

}
