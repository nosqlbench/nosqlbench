/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.rng.core.source64;

/**
 * A fast all-purpose 64-bit generator.
 *
 * <p>This is a member of the Xor-Shift-Rotate family of generators. Memory footprint is 128 bits
 * and the period is 2<sup>128</sup>-1. Speed is expected to be similar to
 * {@link XoShiRo256StarStar}.</p>
 *
 * @see <a href="http://xoshiro.di.unimi.it/xoroshiro128statstar.c">Original source code</a>
 * @see <a href="http://xoshiro.di.unimi.it/">xorshiro / xoroshiro generators</a>
 *
 * @since 1.3
 */
public class XoRoShiRo128StarStar extends AbstractXoRoShiRo128 {
    /**
     * Creates a new instance.
     *
     * @param seed Initial seed.
     * If the length is larger than 2, only the first 2 elements will
     * be used; if smaller, the remaining elements will be automatically
     * set. A seed containing all zeros will create a non-functional generator.
     */
    public XoRoShiRo128StarStar(long[] seed) {
        super(seed);
    }

    /**
     * Creates a new instance using a 2 element seed.
     * A seed containing all zeros will create a non-functional generator.
     *
     * @param seed0 Initial seed element 0.
     * @param seed1 Initial seed element 1.
     */
    public XoRoShiRo128StarStar(long seed0, long seed1) {
        super(seed0, seed1);
    }

    /** {@inheritDoc} */
    @Override
    public long next() {
        final long s0 = state0;
        long s1 = state1;
        final long result = Long.rotateLeft(s0 * 5, 7) * 9;

        s1 ^= s0;
        state0 = Long.rotateLeft(s0, 24) ^ s1 ^ (s1 << 16); // a, b
        state1 = Long.rotateLeft(s1, 37); // c

        return result;
    }
}
