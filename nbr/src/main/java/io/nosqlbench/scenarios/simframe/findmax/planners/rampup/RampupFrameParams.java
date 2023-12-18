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

package io.nosqlbench.scenarios.simframe.findmax.planners.rampup;

/**
 * These parameters are calculated by the planner based on previous simulation frame history.
 */
public record RampupFrameParams(
    /**
     * The base rate upon which we add higher deltas
     */
    double rate_shelf,

        /**
         * The incremental rate which we stack on top of the base rate to find a new limit
         */
        double rate_delta,
        /**
         * How many millis we sample the current frame for
         */
        long sample_time_ms,
        /**
         * How many millis we let the workload settle for to achieve stability, such as when it has recently
         * be in in over-saturation mode with a too-high delta
         */
        long settling_time_ms,
        /**
         * Narrate the reason for the current parameters being set the way the are
         */
        String description

) {
    public double computed_rate() {
        return rate_delta + rate_shelf;
    }


//    public FindMaxFrameParams(double rate_shelf, double rate_delta, long sample_time_ms, long settling_time_ms, String description) {
//        this.rate_shelf = rate_shelf;
//        this.rate_delta = rate_delta;
//        this.sample_time_ms = sample_time_ms;
//        this.settling_time_ms = settling_time_ms;
//        this.description = description;
//    }
//
//    public double computed_rate() {
//        return rate_shelf+rate_delta;
//    }
//
//    public double rate_shelf() {
//        return rate_shelf;
//    }
//
//    public double rate_delta() {
//        return rate_delta;
//    }
//
//    public long sample_time_ms() {
//        return sample_time_ms;
//    }

}
