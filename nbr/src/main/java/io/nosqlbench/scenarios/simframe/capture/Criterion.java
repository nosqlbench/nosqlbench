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

package io.nosqlbench.scenarios.simframe.capture;

import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

/**
 * A criterion is a single component factor of a value for a black-box testing samples.
 *
 * @param name name of the factor
 * @param evaltype Whether to compute the basis value as a deltaV/deltaT,
 *                 a raw value, or a functional remix of the other values.
 * @param remix A remix function, required for {@link EvalType#remix}
 * @param supplier A value supplier, required for {@link EvalType#direct} and {@link EvalType#deltaT}
 * @param weight A weight which affects how the basis value is applied as a factor to composite value. Set this to NaN to nullify effects.
 * @param frameStartCallback A runnable which can be used to do preparatory work at the start of sampling a simulation frame
 */
public record Criterion(
    String name,
    EvalType evaltype,
    ToDoubleFunction<BasisValues> remix,
    DoubleSupplier supplier,
    double weight,
    /**
     * This frameStartCallback is run at the start of a window
     */
    Runnable frameStartCallback
) {
    public Criterion {
        frameStartCallback = frameStartCallback!=null ? frameStartCallback : () -> {};
    }
}
