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

package io.nosqlbench.scenarios.simframe.optimizers.planners.rcurve;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;

/**
 * These search parameters are based on the original findmax algorithm, and
 * should be reduced down to the minimum set needed.
 */
public record RCurveConfig(
    double max_rate,
    int max_step,
    double min_sample_seconds
) {
    public RCurveConfig(NBCommandParams params) {
        this(
            params.maybeGet("max_rate").map(Double::parseDouble).orElse(10.0),
            params.maybeGet("max_step").map(Integer::parseInt).orElse(10),
            params.maybeGet("min_sample_seconds").map(Double::parseDouble).orElse(10.0)
        );
    }

    double rateForStep(int step) {
        return ((double)step/(double) max_step)* max_rate;
    }

}
