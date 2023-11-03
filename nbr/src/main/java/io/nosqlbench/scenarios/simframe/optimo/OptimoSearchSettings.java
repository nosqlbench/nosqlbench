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

import io.nosqlbench.engine.core.lifecycle.scenario.context.ScenarioParams;

public record OptimoSearchSettings(
    long sample_time_ms,
    double cutoff_quantile,
    double cutoff_ms,
    OptimoParamModel model
) {
    public OptimoSearchSettings(ScenarioParams params, OptimoParamModel model) {
        this(
            params.maybeGet("sample_time_ms").map(Long::parseLong).orElse(5000L),
            params.maybeGet("cutoff_quantile").map(Double::parseDouble).orElse(0.99),
            params.maybeGet("cutoff_ms").map(Double::parseDouble).orElse(50.0d),
            model
        );
    }
}
