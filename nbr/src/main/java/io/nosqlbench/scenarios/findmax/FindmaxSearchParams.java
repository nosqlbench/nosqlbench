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

import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioParams;

public record FindmaxSearchParams(
    int sample_time_ms,
    int sample_max,
    double sample_incr,
    double rate_base,
    double rate_step,
    double rate_incr,
    double average_of,
    double latency_cutoff,
    double latency_pctile,
    double testrate_cutoff,
    double bestrate_cutoff
) {
    public FindmaxSearchParams(ScenarioParams params) {
        this(
            params.maybeGet("sample_time_ms").map(Integer::parseInt).orElse(3000),
            params.maybeGet("sample_max").map(Integer::parseInt).orElse(10000),
            params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.01d),
            params.maybeGet("rate_base").map(Double::parseDouble).orElse(0d),
            params.maybeGet("rate_step").map(Double::parseDouble).orElse(100d),
            params.maybeGet("rate_incr").map(Double::parseDouble).orElse(2.0d),
            params.maybeGet("average_of").map(Integer::parseInt).orElse(2),
            params.maybeGet("latency_cutoff").map(Double::parseDouble).orElse(50.0d),
            params.maybeGet("testrate_cutoff").map(Double::parseDouble).orElse(0.8),
            params.maybeGet("bestrate_cutoff").map(Double::parseDouble).orElse(0.90),
            params.maybeGet("latency_pctile").map(Double::parseDouble).orElse(0.99)
        );

    }
}
