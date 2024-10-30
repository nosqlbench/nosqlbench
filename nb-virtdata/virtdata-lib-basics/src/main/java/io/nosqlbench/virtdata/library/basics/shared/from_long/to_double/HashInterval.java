/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongToDoubleFunction;

/**
 * Create a double value from a hashed long, over the valid range of long inputs.
 * This version provides a strict unit interval value, not a unit range value.
 * That is, it can yield any value between 0.0 and 1.0, EXCEPT 1.0.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashInterval implements LongToDoubleFunction {

    private final double min;
    private final double max;
    private final double interval;
    private final static double MAX_DOUBLE_VIA_LONG_PHI = ((double) Long.MAX_VALUE)+1026d;
    private final Hash hash = new Hash();

    public HashInterval(double min, double max) {
        this.min = min;
        this.max = max;
        this.interval = max - min;
        if (min>max) {
            throw new RuntimeException("min must be less than or equal to max");
        }
    }

    @Override
    public double applyAsDouble(long value) {
        long hashed = hash.applyAsLong(value);
        double unitScale = ((double) hashed) / MAX_DOUBLE_VIA_LONG_PHI;
        double valueScaled =interval*unitScale + min;
        return valueScaled;
    }

}
