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

package io.nosqlbench.virtdata.library.basics.shared.vectors.primitive;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_double.HashRange;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.Hash;

import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;

/**
 * Construct an arbitrarily large vector with hashes. The initial value is assumed to be non-hashed, and is thus hashed
 * on input to ensure that inputs are non-contiguous. Once the starting value is hashed, the sequence of long values is
 * walked and each value added to the vector is hashed from the values in that sequence.
 */
@Categories({Category.vectors, Category.experimental})
@ThreadSafeMapper
public class HashedDoubleVectors implements LongFunction<double[]> {

    private final LongToIntFunction sizeFunc;
    private final Hash rehasher;
    private final LongToDoubleFunction valueFunc;

    /**
     * Build a double[] generator with a given size value or size function, and the given long->double function.
     * @param sizer Either a numeric type which sets a fixed dimension, or a long->int function to derive it uniquely for each input
     * @param valueFunc A long->double function
     */
    public HashedDoubleVectors(Object sizer, Object valueFunc) {
        if (sizer instanceof Number number) {
            int size = number.intValue();
            this.sizeFunc = (long l) -> size;
        } else {
            this.sizeFunc = VirtDataConversions.adaptFunction(sizer, LongToIntFunction.class);
        }
        this.valueFunc = VirtDataConversions.adaptFunction(valueFunc, LongToDoubleFunction.class);
        this.rehasher = new Hash();
    }

    public HashedDoubleVectors(Object sizer, double min, double max) {
        this(sizer, new HashRange(min, max));
    }

    public HashedDoubleVectors(Object sizer) {
        this(sizer, new HashRange(0.0d, 1.0d));
    }

    @Override
    public double[] apply(long value) {
        int size = sizeFunc.applyAsInt(value);
        double[] doubles = new double[size];
        long image = rehasher.applyAsLong(value);
        for (int i = 0; i < doubles.length; i++) { // don't consider overflow, hashing doesn't care
            doubles[i] = valueFunc.applyAsDouble(image + i);
        }
        return doubles;
    }
}
