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

import java.util.function.DoubleSupplier;
import java.util.function.ToDoubleFunction;

public record Criterion(
    String name,
    EvalType evaltype,
    ToDoubleFunction<DoubleMap> remix,
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
