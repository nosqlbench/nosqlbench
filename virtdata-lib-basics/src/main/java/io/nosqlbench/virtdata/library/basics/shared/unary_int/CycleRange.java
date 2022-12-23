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

package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Yields a value within a specified range, which rolls over continuously.
 */
@ThreadSafeMapper
@Categories({Category.general})
public class CycleRange implements IntUnaryOperator {

    private final int minValue;
    private final int width;

    /**
     * Sets the maximum value of the cycle range. The minimum is default to 0.
     * @param maxValue The maximum value in the cycle to be added.
     */
    @Example({"CycleRange(34)","add a rotating value between 0 and 34 to the input"})
    public CycleRange(int maxValue) {
        this(0,maxValue);
    }

    /**
     * Sets the minimum and maximum value of the cycle range.
     * @param minValue minimum value of the cycle to be added.
     * @param maxValue maximum value of the cycle to be added.
     */
    public CycleRange(int minValue, int maxValue) {
        this.minValue = minValue;

        if (maxValue<minValue) {
            throw new RuntimeException("CycleRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public int applyAsInt(int operand) {
        return minValue + (operand % width);
    }
}
