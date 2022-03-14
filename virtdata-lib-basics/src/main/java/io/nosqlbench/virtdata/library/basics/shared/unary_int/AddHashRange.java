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

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Adds a pseudo-random value within the specified range to the input.
 */
@ThreadSafeMapper
public class AddHashRange implements IntUnaryOperator {

    private final io.nosqlbench.virtdata.library.basics.shared.unary_int.HashRange hashRange;

    public AddHashRange(int maxValue) {
        this(0, maxValue);
    }

    public AddHashRange(int minValue, int maxValue) {
        this.hashRange = new io.nosqlbench.virtdata.library.basics.shared.unary_int.HashRange(minValue, maxValue);
    }

    @Override
    public int applyAsInt(int operand) {
        return operand + hashRange.applyAsInt(operand);
    }
}
