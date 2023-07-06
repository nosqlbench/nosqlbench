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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_float;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories({Category.general})
public class FixedValues implements LongFunction<Float> {

    private final float[] fixedValues;

    @Example({"FixedValues(3.0,53.0,73d)", "Yield 3D, 53D, 73D, 3D, 53D, 73D, 3D, ..."})
    public FixedValues(Object... values) {
        this.fixedValues = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value instanceof Number number) {
                fixedValues[i]=number.floatValue();
            } else {
                throw new RuntimeException("Not a number: " + value);
            }
        }
    }

    @Override
    public Float apply(long value) {
        int index = (int) (value % Integer.MAX_VALUE) % fixedValues.length;
        return fixedValues[index];
    }


}
