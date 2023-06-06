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

package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.LongToDoubleFunction;


/**
 * Create a double by converting values. This function works in the following modes:
 * <UL>
 * <LI>If you pass a function that produces a numeric type, then the result of this function
 * is evaluated for the input cycle and converted to a double.</LI>
 * <LI>If you pass any numeric value which is not a function, then this value is converted and
 * returned for every cycle as a fixed value.</LI>
 * </UL>
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToDouble implements LongToDoubleFunction {

    private final LongToDoubleFunction func;

    ToDouble(Object func) {
        if (func instanceof Number number) {
            final double aDouble = number.doubleValue();
            this.func = l -> aDouble;
        } else {
            this.func = VirtDataConversions.adaptFunction(func, LongToDoubleFunction.class);
        }
    }

    @Override
    public double applyAsDouble(long value) {
        return func.applyAsDouble(value);
    }
}
