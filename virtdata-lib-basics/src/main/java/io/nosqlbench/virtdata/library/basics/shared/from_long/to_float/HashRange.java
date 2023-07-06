/*
 * Copyright (c) 2022-2023 nosqlbench
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
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories({Category.general})
public class HashRange implements LongFunction<Float> {

    private final float min;
    private final float max;
    private final float interval;
    private final static float MAX_FLOAT_VIA_LONG = (float) Long.MAX_VALUE;
    private final Hash hash = new Hash();

    public HashRange(float min, float max) {
        if (min>max) {
            throw new RuntimeException("max must be greater than or equal to min");
        }
        this.min = min;
        this.max = max;
        this.interval = max - min;
    }

    @Override
    public Float apply(long value) {
        long hashed = hash.applyAsLong(value);
        float unitScale = ((float) hashed) / MAX_FLOAT_VIA_LONG;
        float valueScaled =interval*unitScale + min;
        return valueScaled;
    }
}
