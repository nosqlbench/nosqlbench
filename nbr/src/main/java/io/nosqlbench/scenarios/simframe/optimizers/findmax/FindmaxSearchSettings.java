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

package io.nosqlbench.scenarios.simframe.optimizers.findmax;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;

public record FindmaxSearchSettings(
    double sample_time_ms,
    double sample_max,
    double sample_incr,
    double rate_base,
    double rate_step,
    double rate_incr,
    double average_of,
    double min_settling_ms,
    FindmaxParamModel model
) {
    public FindmaxSearchSettings(NBCommandParams params, FindmaxParamModel model) {
        this(
            params.maybeGet("sample_time_ms").map(Double::parseDouble).orElse(4000d),
            params.maybeGet("sample_max").map(Double::parseDouble).orElse(10000d),
            params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.2d),
            params.maybeGet("rate_base").map(Double::parseDouble).orElse(0d),
            params.maybeGet("rate_step").map(Double::parseDouble).orElse(100d),
            params.maybeGet("rate_incr").map(Double::parseDouble).orElse(2d),
            params.maybeGet("average_of").map(Double::parseDouble).orElse(2d),
            params.maybeGet("min_settling_ms").map(Double::parseDouble).orElse(4000d),
            model
        );
    }
}
