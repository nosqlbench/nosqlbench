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
 * <p>This is a member of the Xor-Shift-Rotate family of generators. Memory footprint is 256 bits
 * and the period is 2<sup>256</sup>-1.</p>
 *
 * @see <a href="http://xoshiro.di.unimi.it/xoshiro256starstar.c">Original source code</a>
 * @see <a href="http://xoshiro.di.unimi.it/">xorshiro / xoroshiro generators</a>
 *
 * @since 1.3
 */
public class XoShiRo256StarStar extends AbstractXoShiRo256 {
    /**
     * Creates a new instance.
     *
     * @param seed Initial seed.
     * If the length is larger than 4, only the first 4 elements will
     * be used; if smaller, the remaining elements will be automatically
     * set. A seed containing all zeros will create a non-functional generator.
     */
    public XoShiRo256StarStar(long[] seed) {
        super(seed);
    }

    /**
     * Creates a new instance using a 4 element seed.
     * A seed containing all zeros will create a non-functional generator.
     *
     * @param seed0 Initial seed element 0.
     * @param seed1 Initial seed element 1.
     * @param seed2 Initial seed element 2.
     * @param seed3 Initial seed element 3.
     */
    public XoShiRo256StarStar(long seed0, long seed1, long seed2, long seed3) {
        super(seed0, seed1, seed2, seed3);
    }

    /** {@inheritDoc} */
    @Override
    public long next() {
        final long result = Long.rotateLeft(state1 * 5, 7) * 9;

        final long t = state1 << 17;

        state2 ^= state0;
        state3 ^= state1;
        state1 ^= state2;
        state0 ^= state3;

        state2 ^= t;

        state3 = Long.rotateLeft(state3, 45);

        return result;
    }
}
