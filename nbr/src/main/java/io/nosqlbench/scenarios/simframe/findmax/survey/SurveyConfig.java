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

package io.nosqlbench.scenarios.simframe.findmax.survey;

import io.nosqlbench.engine.core.lifecycle.scenario.container.NBCommandParams;

/**
 * These search parameters are based on the original findmax algorithm, and
 * should be reduced down to the minimum set needed.
 */
public record SurveyConfig(
    double max_rate,
    int steps
) {
    public SurveyConfig(NBCommandParams params) {
        this(
            params.maybeGet("max_rate").map(Double::parseDouble).orElse(1.2d),
            params.maybeGet("steps").map(Integer::parseInt).orElse(3)
        );

    }
}
