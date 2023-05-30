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

package io.nosqlbench.virtdata.library.basics.shared.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.LongFunction;


/**
 * Wrap any function producing a valid numeric value as a float.
 */
@Categories(Category.conversion)
@ThreadSafeMapper
public class ToFloat implements LongFunction<Float> {

    private final LongFunction<Float> func;

    ToFloat(Object funcOrValue) {
        if (funcOrValue instanceof Number number) {
            final float afloat = number.floatValue();
            this.func = l -> afloat;
        } else {
            this.func = VirtDataConversions.adaptFunction(funcOrValue,LongFunction.class,Float.class);
        }

    }

    @Override
    public Float apply(long value) {
        return func.apply(value);
    }
}
