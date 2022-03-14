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

import io.nosqlbench.nb.api.errors.BasicError;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class HashRange implements IntUnaryOperator {

    private final int minValue;
    private final int  width;
    private final Hash hash = new Hash();

    public HashRange(int width) {
        this.minValue=0;
        this.width=width;
    }

    public HashRange(int minValue, int maxValue) {
        if (maxValue<minValue) {
            throw new BasicError("HashRange must have min and max value in that order.");
        }
        this.minValue = minValue;
        this.width = (maxValue - minValue) +1;
    }

    @Override
    public int applyAsInt(int operand) {
        return minValue + (hash.applyAsInt(operand) & width);
    }
}
