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

import java.util.function.LongFunction;


/**
 * Create a float by converting values. This function works in the following modes:
 * <UL>
 *     <LI>If a float is provided, then the long input is scaled to be between 0.0f and the value provided,
 *     as a fraction of the highest possible long.</LI>
 *     <LI>If any other type of number is provided, then this function returns the equivalent float value.</LI>
 *     <LI>Otherwise, the input is assumed to be a function which takes a long input, and, if necessary,
 *     adapts to return a float with an appropriate type conversion.</LI>
 * </UL>
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToFloat implements LongFunction<Float> {

    private final LongFunction<Float> func;

    ToFloat(Object func) {
        if (func instanceof Float afloat) {
            float scale = afloat.floatValue();
            this.func = l -> (float) (l % scale);
        } else if (func instanceof Number number) {
            final float afloat = number.floatValue();
            this.func = l -> afloat;
        } else {
            this.func = VirtDataConversions.adaptFunction(func, LongFunction.class, Float.class);
        }
    }

    @Override
    public Float apply(long value) {
        return func.apply(value);
    }
}
