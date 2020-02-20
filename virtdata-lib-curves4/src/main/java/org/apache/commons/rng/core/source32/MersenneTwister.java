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
 * This class implements a powerful pseudo-random number generator
 * developed by Makoto Matsumoto and Takuji Nishimura during
 * 1996-1997.
 *
 * <p>
 * This generator features an extremely long period
 * (2<sup>19937</sup>-1) and 623-dimensional equidistribution up to
 * 32 bits accuracy.  The home page for this generator is located at
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html">
 * http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html</a>.
 * </p>
 *
 * <p>
 * This generator is described in a paper by Makoto Matsumoto and
 * Takuji Nishimura in 1998:
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/ARTICLES/mt.pdf">
 * Mersenne Twister: A 623-Dimensionally Equidistributed Uniform Pseudo-Random
 * Number Generator</a>,
 * ACM Transactions on Modeling and Computer Simulation, Vol. 8, No. 1,
 * January 1998, pp 3--30
 * </p>
 *
 * <p>
 * This class is mainly a Java port of the
 * <a href="http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/MT2002/emt19937ar.html">
 * 2002-01-26 version of the generator</a> written in C by Makoto Matsumoto
 * and Takuji Nishimura. Here is their original copyright:
 * </p>
 *
 * <table style="background-color: #E0E0E0; width: 80%">
 * <caption>Mersenne Twister licence</caption>
 * <tr><td style="padding: 10px">Copyright (C) 1997 - 2002, Makoto Matsumoto and Takuji Nishimura,
 *     All rights reserved.</td></tr>
 *
 * <tr><td style="padding: 10px">Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <ol>
 *   <li>Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.</li>
 *   <li>Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.</li>
 *   <li>The names of its contributors may not be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.</li>
 * </ol></td></tr>
 *
 * <tr><td style="padding: 10px"><strong>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.</strong></td></tr>
 * </table>
 *
 * @see <a href="https://en.wikipedia.org/wiki/Mersenne_Twister">Mersenne Twister (Wikipedia)</a>
 * @since 1.0
 */
public class MersenneTwister extends IntProvider {
    /** Mask 32 most significant bits. */
    private static final long INT_MASK_LONG = 0xffffffffL;
    /** Most significant w-r bits. */
    private static final long UPPER_MASK_LONG = 0x80000000L;
    /** Least significant r bits. */
    private static final long LOWER_MASK_LONG = 0x7fffffffL;
    /** Most significant w-r bits. */
    private static final int UPPER_MASK = 0x80000000;
    /** Least significant r bits. */
    private static final int LOWER_MASK = 0x7fffffff;
    /** Size of the bytes pool. */
    private static final int N = 624;
    /** Period second parameter. */
    private static final int M = 397;
    /** X * MATRIX_A for X = {0, 1}. */
    private static final int[] MAG01 = { 0x0, 0x9908b0df };
    /** Bytes pool. */
    private int[] mt = new int[N];
    /** Current index in the bytes pool. */
    private int mti;

    /**
     * Creates a new random number generator.
     *
     * @param seed Initial seed.
     */
    public MersenneTwister(int[] seed) {
        setSeedInternal(seed);
    }

    /** {@inheritDoc} */
    @Override
    protected byte[] getStateInternal() {
        final int[] s = Arrays.copyOf(mt, N + 1);
        s[N] = mti;

        return composeStateInternal(NumberFactory.makeByteArray(s),
                                    super.getStateInternal());
    }

    /** {@inheritDoc} */
    @Override
    protected void setStateInternal(byte[] s) {
        final byte[][] c = splitStateInternal(s, (N + 1) * 4);

        final int[] tmp = NumberFactory.makeIntArray(c[0]);
        System.arraycopy(tmp, 0, mt, 0, N);
        mti = tmp[N];

        super.setStateInternal(c[1]);
    }

    /**
     * Initializes the generator with the given seed.
     *
     * @param seed Initial seed.
     */
    private void setSeedInternal(int[] seed) {
        fillStateMersenneTwister(mt, seed);

        // Initial index.
        mti = N;
    }

    /**
     * Utility for wholly filling a {@code state} array with non-zero
     * bytes, even if the {@code seed} has a smaller size.
     * The procedure is the one defined by the standard implementation
     * of the algorithm.
     *
     * @param state State to be filled (must be allocated).
     * @param seed Seed (cannot be {@code null}).
     */
    private static void fillStateMersenneTwister(int[] state,
                                                 int[] seed) {
        if (seed.length == 0) {
            // Accept empty seed.
            seed = new int[1];
        }

        final int stateSize = state.length;

        long mt = 19650218 & INT_MASK_LONG;
        state[0] = (int) mt;
        for (int i = 1; i < stateSize; i++) {
            mt = (1812433253L * (mt ^ (mt >> 30)) + i) & INT_MASK_LONG;
            state[i] = (int) mt;
        }

        int i = 1;
        int j = 0;

        for (int k = Math.max(stateSize, seed.length); k > 0; k--) {
            final long a = (state[i] & LOWER_MASK_LONG) | ((state[i] < 0) ? UPPER_MASK_LONG : 0);
            final long b = (state[i - 1] & LOWER_MASK_LONG) | ((state[i - 1] < 0) ? UPPER_MASK_LONG : 0);
            final long c = (a ^ ((b ^ (b >> 30)) * 1664525L)) + seed[j] + j; // Non linear.
            state[i] = (int) (c & INT_MASK_LONG);
            i++;
            j++;
            if (i >= stateSize) {
                state[0] = state[stateSize - 1];
                i = 1;
            }
            if (j >= seed.length) {
                j = 0;
            }
        }

        for (int k = stateSize - 1; k > 0; k--) {
            final long a = (state[i] & LOWER_MASK_LONG) | ((state[i] < 0) ? UPPER_MASK_LONG : 0);
            final long b = (state[i - 1] & LOWER_MASK_LONG) | ((state[i - 1] < 0) ? UPPER_MASK_LONG : 0);
            final long c = (a ^ ((b ^ (b >> 30)) * 1566083941L)) - i; // Non linear.
            state[i] = (int) (c & INT_MASK_LONG);
            i++;
            if (i >= stateSize) {
                state[0] = state[stateSize - 1];
                i = 1;
            }
        }

        state[0] = (int) UPPER_MASK_LONG; // MSB is 1, ensuring non-zero initial array.
    }

    /** {@inheritDoc} */
    @Override
    public int next() {
        int y;

        if (mti >= N) { // Generate N words at one time.
            int mtNext = mt[0];
            for (int k = 0; k < N - M; ++k) {
                int mtCurr = mtNext;
                mtNext = mt[k + 1];
                y = (mtCurr & UPPER_MASK) | (mtNext & LOWER_MASK);
                mt[k] = mt[k + M] ^ (y >>> 1) ^ MAG01[y & 1];
            }
            for (int k = N - M; k < N - 1; ++k) {
                int mtCurr = mtNext;
                mtNext = mt[k + 1];
                y = (mtCurr & UPPER_MASK) | (mtNext & LOWER_MASK);
                mt[k] = mt[k + (M - N)] ^ (y >>> 1) ^ MAG01[y & 1];
            }
            y = (mtNext & UPPER_MASK) | (mt[0] & LOWER_MASK);
            mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ MAG01[y & 1];

            mti = 0;
        }

        y = mt[mti++];

        // Tempering.
        y ^=  y >>> 11;
        y ^= (y << 7) & 0x9d2c5680;
        y ^= (y << 15) & 0xefc60000;
        y ^=  y >>> 18;

        return y;
    }
}
