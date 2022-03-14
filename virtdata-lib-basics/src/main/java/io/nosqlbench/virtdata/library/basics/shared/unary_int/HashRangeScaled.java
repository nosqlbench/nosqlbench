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

@ThreadSafeMapper
public class HashRangeScaled implements IntUnaryOperator {

    private final Hash hash = new Hash();
    private final double scalefactor;

    public HashRangeScaled(double scalefactor) {
        this.scalefactor = scalefactor;
    }

    public HashRangeScaled() {
        this.scalefactor = 1.0D;
    }

    @Override
    public int applyAsInt(int operand) {
        if (operand == 0) {
            return 0;
        }
        return (int) ((hash.applyAsInt(operand) % operand) * scalefactor) % Integer.MAX_VALUE;
    }
}
