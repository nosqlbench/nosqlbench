/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_double.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.DoubleFunction;

/**
 * Yield VALUE.unset if the input value is close to the
 * specified value by the sigma threshold. Otherwise,
 * pass the input value along.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfCloseTo implements DoubleFunction<Object> {

    private final double compareto;
    private final double sigma;

    public UnsetIfCloseTo(double compareto, double sigma) {
        this.compareto = compareto;
        this.sigma = sigma;
    }

    @Override
    public Object apply(double value) {
        if (Math.abs(value - compareto) <= sigma) return VALUE.unset;
        return value;
    }
}
