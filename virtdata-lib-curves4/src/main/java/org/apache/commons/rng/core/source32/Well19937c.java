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

/**
 * This class implements the WELL19937c pseudo-random number generator
 * from Fran&ccedil;ois Panneton, Pierre L'Ecuyer and Makoto Matsumoto.
 * <p>
 * This generator is described in a paper by Fran&ccedil;ois Panneton,
 * Pierre L'Ecuyer and Makoto Matsumoto
 * <a href="http://www.iro.umontreal.ca/~lecuyer/myftp/papers/wellrng.pdf">
 * Improved Long-Period Generators Based on Linear Recurrences Modulo 2</a>
 * ACM Transactions on Mathematical Software, 32, 1 (2006).
 * The errata for the paper are in
 * <a href="http://www.iro.umontreal.ca/~lecuyer/myftp/papers/wellrng-errata.txt">wellrng-errata.txt</a>.
 * </p>
 *
 * @see <a href="http://www.iro.umontreal.ca/~panneton/WELLRNG.html">WELL Random number generator</a>
 * @since 1.0
 */
public class Well19937c extends Well19937a {
    /**
     * Creates a new random number generator.
     *
     * @param seed Initial seed.
     */
    public Well19937c(int[] seed) {
        super(seed);
    }

    /** {@inheritDoc} */
    @Override
    public int next() {
        int z4 = super.next();

        // Matsumoto-Kurita tempering to get a maximally equidistributed generator.
        z4 ^= (z4 << 7) & 0xe46e1700;
        z4 ^= (z4 << 15) & 0x9b868000;

        return z4;
    }
}
