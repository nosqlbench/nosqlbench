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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a value within a range, pseudo-randomly, using interval semantics,
 * where the range of values return does not include the last value.
 * This function behaves exactly like HashRange except for the exclusion
 * of the last value. This allows you to stack intervals using known
 * reference points without duplicating or skipping any given value.
 *
 * You can specify hash intervals as small as a single-element range, like
 * (5,6), or as wide as the relevant data type allows.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class HashInterval implements LongUnaryOperator {

    private final long minValue;
    private final long width;
    private final Hash hash = new Hash();

    /**
     * Create a hash interval based on a minimum value of 0 and a specified width.
     * @param width The maximum value, which is excluded.
     */
    @Example({"HashInterval(4L)","return values which could include 0L, 1L, 2L, 3L, but not 4L"})
    public HashInterval(long width) {
        this.minValue=0L;
        this.width=width;
    }

    /**
     * Create a hash interval
     * @param minIncl The minimum value, which is included
     * @param maxExcl The maximum value, which is excluded
     */
    @Example({"HashInterval(2L,5L)","return values which could include 2L, 3L, 4L, but not 5L"})
    public HashInterval(long minIncl, long maxExcl) {
        if (maxExcl<=minIncl) {
            throw new BasicError("HashInterval must have min and max value in that order, where the min is less than the max.");
        }
        this.minValue = minIncl;
        this.width = (maxExcl - minIncl);
    }

    @Override
    public long applyAsLong(long operand) {
        return minValue + (hash.applyAsLong(operand) % width);
    }
}
