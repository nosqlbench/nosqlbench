package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

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

import java.util.function.LongFunction;

/**
 * Convert the input value to a short.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToShort implements LongFunction<Short> {

    private final int scale;

    public ToShort() {
        this.scale = Short.MAX_VALUE;
    }

    /**
     * This form allows for limiting the short values at a lower limit than Short.MAX_VALUE.
     * @param wrapat The maximum value to return.
     */
    public ToShort(int wrapat) {
        this.scale = wrapat;
    }

    @Override
    public Short apply(long input) {
        return (short) (input % scale);
    }
}
