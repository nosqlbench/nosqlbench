/*
 * Copyright (c) 2020-2024 nosqlbench
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

public record FindmaxConfig (
    double sample_time_ms,
    double max_value,
    double base_value,
    double min_value,
    double step_value,
    double value_incr,
    double sample_incr,
    long min_settling_ms,
    String optimization_type,
    double[] initial_point
) {
    public double[] initialPoint() {
        return new double[]{base_value};
    }

    public FindmaxConfig(NBCommandParams params) {
        this(
            params.maybeGet("sample_time_ms").map(Double::parseDouble).orElse(4000d),
            params.maybeGet("max_value").map(Double::parseDouble).orElse(10000d),
            params.maybeGet("base_value").map(Double::parseDouble).orElse(10d),
            params.maybeGet("min_value").map(Double::parseDouble).orElse(0d),
            params.maybeGet("step_value").map(Double::parseDouble).orElse(100d),
            params.maybeGet("value_incr").map(Double::parseDouble).orElse(2d),
            params.maybeGet("sample_incr").map(Double::parseDouble).orElse(1.2d),
            params.maybeGet("min_settling_ms").map(Long::parseLong).orElse(4000L),
            params.maybeGet("optimization_type").orElse("rate"),
            new double[]{params.maybeGet("base_value").map(Double::parseDouble).orElse(10d)}
        );
    }
}
