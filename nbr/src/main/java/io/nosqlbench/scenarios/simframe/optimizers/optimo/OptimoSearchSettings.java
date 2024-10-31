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

package io.nosqlbench.scenarios.simframe.optimizers.optimo;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;
import io.nosqlbench.nb.api.engine.util.Unit;

public record OptimoSearchSettings(
    double startRate,
    long sample_time_ms,
    double cutoff_quantile,
    double cutoff_ms,
    double start_threads,
    OptimoParamModel model
) {
    public OptimoSearchSettings(NBCommandParams params, OptimoParamModel model) {
        this(
            params.maybeGet("startrate").flatMap(Unit::doubleCountFor).orElse(1000.0d),
            params.maybeGet("sample_time_ms").map(Long::parseLong).orElse(5000L),
            params.maybeGet("cutoff_quantile").map(Double::parseDouble).orElse(0.99),
            params.maybeGet("cutoff_ms").map(Double::parseDouble).orElse(50.0d),
            params.maybeGet("start_threads").map(Double::parseDouble).orElse(50.0d),
            model
        );
    }
}
