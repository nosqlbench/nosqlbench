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

package io.nosqlbench.scenarios.simframe.findmax.planners.ratchet;

import io.nosqlbench.engine.core.lifecycle.scenario.context.NBCommandParams;

/**
 * These search parameters are based on the original findmax algorithm, and
 * should be reduced down to the minimum set needed.
 */
public record RatchetConfig(
    int sample_time_ms,
    int sample_max,
    double sample_incr,
    double rate_base,
    double rate_step,
    double rate_minstep,
    double rate_scaledown,
    int max_attempts
) {
    public RatchetConfig(NBCommandParams params) {
        this(
            params.maybeGet("sample_time_ms").map(Integer::parseInt).orElse(1000),
            params.maybeGet("sample_max").map(Integer::parseInt).orElse(10000),
            params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.2d),
            params.maybeGet("rate_base").map(Double::parseDouble).orElse(5d),
            params.maybeGet("rate_step").map(Double::parseDouble).orElse(1000d),
            params.maybeGet("rate_minstep").map(Double::parseDouble).orElse(1000d),
            params.maybeGet("rate_scaledown").map(Double::parseDouble).orElse(0.25),
            params.maybeGet("max_attempts").map(Integer::parseInt).orElse(3)
        );

    }
}
