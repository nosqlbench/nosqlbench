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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongToDoubleFunction;

/*
 * <p>This simulates a uniform sample from a range of double values
 * via long hashing. This function attempts to take a double
 * unit interval value from a long/long division over the whole
 * range of long values but via double value types, thus providing
 * a very linear sample. This means that the range of double
 * values to be accessed will not fall along all possible doubles,
 * but will still provide suitable values for ranges close to
 * high-precision points in the IEEE floating point number line.
 * This suffices for most reasonable ranges in practice outside
 * of scientific computing, where large exponents put adjacent
 * IEEE floating point values much further apart.</p>
 *
 * <p>This should be consider the default double range sampling
 * function for most uses, when the exponent is not needed for
 * readability.</p>
 */

/**
 * Return a double value within the specified range. This function
 * uses an intermediate long to arrive at the sampled value before
 * conversion to double, thus providing a more linear sample at the
 * expense of some precision at extremely large values.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashRange implements LongToDoubleFunction {

    private final double min;
    private final double max;
    private final double interval;
    private final static double MAX_DOUBLE_VIA_LONG = (double) Long.MAX_VALUE;
    private final Hash hash = new Hash();

    public HashRange(double min, double max) {
        if (min>max) {
            throw new RuntimeException("min must be less than or equal to max");
        }
        this.min = min;
        this.max = max;
        this.interval = max - min;
    }

    @Override
    public double applyAsDouble(long value) {
        long hashed = hash.applyAsLong(value);
        double unitScale = ((double) hashed) / MAX_DOUBLE_VIA_LONG;
        double valueScaled =interval*unitScale + min;
        return valueScaled;
    }
}
