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

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a value within a range, pseudo-randomly. This is equivalent to
 * returning a value with in range between 0 and some maximum value, but
 * with a minimum value added.
 *
 * You can specify hash ranges as small as a single-element range, like
 * (5,5), or as wide as the relevant data type allows.
 */
@ThreadSafeMapper
public class HashRange implements LongUnaryOperator {

    private final long minValue;
    private final long width;
    private final Hash hash = new Hash();

    public HashRange(long width) {
        this.minValue=0L;
        this.width=width;
    }

    public HashRange(long minValue, long maxValue) {
        if (maxValue<minValue) {
            throw new BasicError("HashRange must have min and max value in that order.");
        }
        this.minValue = minValue;
        this.width = (maxValue - minValue)+1;
    }

    @Override
    public long applyAsLong(long operand) {
        return minValue + (hash.applyAsLong(operand) % width);
    }
}
