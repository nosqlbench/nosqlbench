package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Convert the input value into a float.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToFloat implements DoubleFunction<Float> {
    private final double scale;

    public ToFloat(double scale) {
        this.scale = scale;
    }
    public ToFloat() {
        this.scale = Float.MAX_VALUE;
    }

    @Override
    public Float apply(double input) {
        return (float) (input % scale);
    }
}
