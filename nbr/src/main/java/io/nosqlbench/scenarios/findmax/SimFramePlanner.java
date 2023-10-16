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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SimFramePlanner {
    private final Logger logger = LogManager.getLogger(SimFramePlanner.class);


    /**
     * Search params which control findmax
     */
    private final FindmaxSearchParams findmax;

    public SimFramePlanner(FindmaxSearchParams findMaxSettings) {
        this.findmax = findMaxSettings;

    }

    public SimFrameParams initialStep() {
        return new SimFrameParams(
            this.findmax.rate_base(), this.findmax.rate_step(), this.findmax.sample_time_ms(), "INITIAL"
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
        SimFrame last = journal.last();
        SimFrame best = journal.bestRun();
        if (best.index() == last.index()) { // got better consecutively
            return new SimFrameParams(
                last.params().rate_shelf(),
                last.params().rate_delta() * findmax.rate_incr(),
                last.params().sample_time_ms(),
                "CONTINUE after improvement from frame " + last.index()
            );
        } else if (best.index() == last.index() - 1) {
            // got worse consecutively, this may be collapsed out since the general case below covers it (test first)
            if ((last.params().computed_rate() - best.params().computed_rate()) <= findmax.rate_step()) {
                logger.info("could not divide search space further, stop condition met");
                return null;
            } else {
                return new SimFrameParams(
                    best.params().computed_rate(),
                    findmax.rate_step(),
                    (long) (last.params().sample_time_ms() * findmax.sample_incr()),
                    "REBASE search range to new base after frame " + best.index()
                );
            }
        } else { // any other case
            // find next frame with higher rate but lower value, the closest one by rate
            SimFrame nextWorseFrameWithHigherRate = journal.frames().stream()
                    .filter(f -> f.value() < best.value())
                    .filter(f -> f.params().computed_rate() > best.params().computed_rate())
                .min(Comparator.comparingDouble(f -> f.params().computed_rate()))
                .orElseThrow(() -> new RuntimeException("inconsistent samples"));
            if ((nextWorseFrameWithHigherRate.params().computed_rate() - best.params().computed_rate()) > findmax.rate_step()) {
                return new SimFrameParams(
                    best.params().computed_rate(),
                    findmax.rate_step(),
                    (long) (last.params().sample_time_ms() * findmax.sample_incr()),
                    "REBASE search range from frames " + best.index() + " ➞ " +nextWorseFrameWithHigherRate.index()
                );
            } else {
                logger.info("could not divide search space further, stop condition met");
                return null;
            }
        }
    }

}
