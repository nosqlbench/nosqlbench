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

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return a pseudo-random value which can only be as large as the input times
 * a scale factor, with a default scale factor of 1.0d
 */
@ThreadSafeMapper
public class HashRangeScaled implements LongUnaryOperator {

    private final double scalefactor;
    private final Hash hash = new Hash();

    public HashRangeScaled(double scalefactor) {
        this.scalefactor = scalefactor;
    }

    public HashRangeScaled() {
        this.scalefactor = 1.0D;
    }

    @Override
    public long applyAsLong(long operand) {
        if (operand == 0) {
            return 0;
        }
        long hashed = hash.applyAsLong(operand);
        return (long) ((hashed % operand) * scalefactor);
    }
}
