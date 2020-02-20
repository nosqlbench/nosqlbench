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

/**
 * Port from Marsaglia's <a href="http://www.cse.yorku.ca/~oz/marsaglia-rng.html">
 * "KISS" algorithm</a>.
 * This version contains the correction referred to
 * <a href="https://programmingpraxis.com/2010/10/05/george-marsaglias-random-number-generators/">here</a>
 * in a reply to the original post.
 *
 * @see <a href="https://en.wikipedia.org/wiki/KISS_(algorithm)">KISS (Wikipedia)</a>
 * @since 1.0
 */
public class KISSRandom extends IntProvider {
    /** Size of the seed. */
    private static final int SEED_SIZE = 4;
    /** State variable. */
    private int z;
    /** State variable. */
    private int w;
    /** State variable. */
    private int jsr;
    /** State variable. */
    private int jcong;

    /**
     * Creates a new instance.
     *
     * @param seed Seed.
     * If the length is larger than 4, only the first 4 elements will
     * be used; if smaller, the remaining elements will be automatically
     * set.
     */
    public KISSRandom(int[] seed) {
        setSeedInternal(seed);
    }

    /** {@inheritDoc} */
    @Override
    protected byte[] getStateInternal() {
        return composeStateInternal(NumberFactory.makeByteArray(new int[] { z, w, jsr, jcong }),
                                    super.getStateInternal());
    }

    /** {@inheritDoc} */
    @Override
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, SEED_SIZE * 4);

        final int[] tmp = NumberFactory.makeIntArray(c[0]);
        z = tmp[0];
        w = tmp[1];
        jsr = tmp[2];
        jcong = tmp[3];

        super.setStateInternal(c[1]);
    }

    /**
     * Seeds the RNG.
     *
     * @param seed Seed.
     */
    private void setSeedInternal(int[] seed) {
        // Reset the whole state of this RNG (i.e. the 4 state variables).
        // Filling procedure is not part of the reference code.
        final int[] tmp = new int[SEED_SIZE];
        fillState(tmp, seed);

        z = tmp[0];
        w = tmp[1];
        jsr = tmp[2];
        jcong = tmp[3];
    }

    /** {@inheritDoc} */
    @Override
    public int next() {
        z = computeNew(36969, z);
        w = computeNew(18000, w);
        final int mwc = (z << 16) + w;

        // Cf. correction mentioned in the reply to the original post:
        //   https://programmingpraxis.com/2010/10/05/george-marsaglias-random-number-generators/
        jsr ^= jsr << 13;
        jsr ^= jsr >>> 17;
        jsr ^= jsr << 5;

        jcong = 69069 * jcong + 1234567;

        return (mwc ^ jcong) + jsr;
    }

    /**
     * Compute new value.
     *
     * @param mult Multiplier.
     * @param previous Previous value.
     * @return new value.
     */
    private int computeNew(int mult,
                           int previous) {
        return mult * (previous & 65535) + (previous >>> 16);
    }
}
