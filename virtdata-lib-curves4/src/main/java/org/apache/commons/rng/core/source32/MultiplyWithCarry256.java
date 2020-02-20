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
package org.apache.commons.rng.core.source32;

import org.apache.commons.rng.core.util.NumberFactory;

import java.util.Arrays;

/**
 * Port from Marsaglia's <a href="https://en.wikipedia.org/wiki/Multiply-with-carry">
 * "Multiply-With-Carry" algorithm</a>.
 *
 * <p>
 * Implementation is based on the (non-portable!) C code reproduced on
 * <a href="http://school.anhb.uwa.edu.au/personalpages/kwessen/shared/Marsaglia03.html">
 * that page</a>.
 * </p>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Multiply-with-carry">Multiply with carry (Wikipedia)</a>
 * @since 1.0
 */
public class MultiplyWithCarry256 extends IntProvider {
    /** Length of the state array. */
    private static final int Q_SIZE = 256;
    /** Size of the seed. */
    private static final int SEED_SIZE = Q_SIZE + 1;
    /** Multiply. */
    private static final long A = 809430660;
    /** State. */
    private final int[] state = new int[Q_SIZE];
    /** Current index in "state" array. */
    private int index;
    /** Carry. */
    private int carry;

    /**
     * Creates a new instance.
     *
     * @param seed Seed.
     * If the length is larger than 257, only the first 257 elements will
     * be used; if smaller, the remaining elements will be automatically
     * set.
     */
    public MultiplyWithCarry256(int[] seed) {
        setSeedInternal(seed);
    }

    /** {@inheritDoc} */
    @Override
    protected byte[] getStateInternal() {
        final int[] s = Arrays.copyOf(state, SEED_SIZE + 1);
        s[SEED_SIZE - 1] = carry;
        s[SEED_SIZE] = index;

        return composeStateInternal(NumberFactory.makeByteArray(s),
                                    super.getStateInternal());
    }

    /** {@inheritDoc} */
    @Override
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, (SEED_SIZE + 1) * 4);

        final int[] tmp = NumberFactory.makeIntArray(c[0]);

        System.arraycopy(tmp, 0, state, 0, Q_SIZE);
        carry = tmp[SEED_SIZE - 1];
        index = tmp[SEED_SIZE];

        super.setStateInternal(c[1]);
    }

    /**
     * Seeds the RNG.
     *
     * @param seed Seed.
     */
    private void setSeedInternal(int[] seed) {
        // Reset the whole state of this RNG (i.e. "state" and "index").
        // Filling procedure is not part of the reference code.
        final int[] tmp = new int[SEED_SIZE];
        fillState(tmp, seed);

        // First element of the "seed" is the initial "carry".
        final int c = tmp[0];
        // Marsaglia's recommendation: 0 <= carry < A.
        carry = (int) (Math.abs(c) % A);

        // Initial state.
        System.arraycopy(tmp, 1, state, 0, Q_SIZE);

        // Initial index.
        index = Q_SIZE;
    }

    /** {@inheritDoc} */
    @Override
    public int next() {
        // Produce an index in the range 0-255
        index &= 0xff;
        final long t = A * (state[index] & 0xffffffffL) + carry;
        carry = (int) (t >> 32);
        return state[index++] = (int) t;
    }
}
