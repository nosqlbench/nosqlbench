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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VirtDataConversions;

import java.util.function.LongUnaryOperator;

/**
 * Computes the distance between the current input value and the
 * beginning of the phase, according to a phase length.
 * This means that for a phase length of 100, the values will
 * range from 0 (for cycle values 0 and 100 or any multiple thereof)
 * and 50, when the cycle value falls immediately at the middle
 * of the phase.
 */
@ThreadSafeMapper
@Categories(Category.periodic)
public class CyclicDistance implements LongUnaryOperator {
    private final long phaseLength;
    private final LongUnaryOperator scaleFunc;

    public CyclicDistance(long phaseLength, Object scaleFunc) {
        this.phaseLength=phaseLength;
        this.scaleFunc = VirtDataConversions.adaptFunction(scaleFunc, LongUnaryOperator.class);
    }
    public CyclicDistance(long phaseLength) {
        this(phaseLength, LongUnaryOperator.identity());
    }

    @Override
    public long applyAsLong(long operand) {
        long position = operand % phaseLength;
        long minDistanceFromEnds = Math.min(Math.abs(phaseLength - position), position);
        long result = scaleFunc.applyAsLong(minDistanceFromEnds);
        return result;
    }
}
