package io.nosqlbench.virtdata.library.random;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */



import io.nosqlbench.virtdata.murmur.Murmur3F;

import java.util.function.LongUnaryOperator;

/**
 * A generator that is mostly useless, except for testing useless generators.
 * This is used as a control for the concurrent generation tester.
 */
public class Murmur3Time implements LongUnaryOperator {
    private Murmur3F murmur3F = new Murmur3F(Thread.currentThread().getName().hashCode());
    @Override
    public long applyAsLong(long operand) {
        murmur3F.updateLongLE(System.nanoTime());
        return murmur3F.getValue();
    }
}
