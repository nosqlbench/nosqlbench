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
package org.apache.commons.rng.sampling.distribution;

import org.apache.commons.rng.UniformRandomProvider;

/**
 * Sampling from an <a href="http://mathworld.wolfram.com/ExponentialDistribution.html">exponential distribution</a>.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.0
 */
public class AhrensDieterExponentialSampler
    extends SamplerBase
    implements ContinuousSampler {
    /**
     * Table containing the constants
     * \( q_i = sum_{j=1}^i (\ln 2)^j / j! = \ln 2 + (\ln 2)^2 / 2 + ... + (\ln 2)^i / i! \)
     * until the largest representable fraction below 1 is exceeded.
     *
     * Note that
     * \( 1 = 2 - 1 = \exp(\ln 2) - 1 = sum_{n=1}^\infinity (\ln 2)^n / n! \)
     * thus \( q_i \rightarrow 1 as i \rightarrow +\infinity \),
     * so the higher \( i \), the closer we get to 1 (the series is not alternating).
     *
     * By trying, n = 16 in Java is enough to reach 1.
     */
    private static final double[] EXPONENTIAL_SA_QI = new double[16];
    /** The mean of this distribution. */
    private final double mean;
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    /**
     * Initialize tables.
     */
    static {
        /**
         * Filling EXPONENTIAL_SA_QI table.
         * Note that we don't want qi = 0 in the table.
         */
        final double ln2 = Math.log(2);
        double qi = 0;

        for (int i = 0; i < EXPONENTIAL_SA_QI.length; i++) {
            qi += Math.pow(ln2, i + 1) / InternalUtils.factorial(i + 1);
            EXPONENTIAL_SA_QI[i] = qi;
        }
    }

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param mean Mean of this distribution.
     * @throws IllegalArgumentException if {@code mean <= 0}
     */
    public AhrensDieterExponentialSampler(UniformRandomProvider rng,
                                          double mean) {
        super(null);
        if (mean <= 0) {
            throw new IllegalArgumentException("mean is not strictly positive: " + mean);
        }
        this.rng = rng;
        this.mean = mean;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        // Step 1:
        double a = 0;
        double u = rng.nextDouble();

        // Step 2 and 3:
        while (u < 0.5) {
            a += EXPONENTIAL_SA_QI[0];
            u *= 2;
        }

        // Step 4 (now u >= 0.5):
        u += u - 1;

        // Step 5:
        if (u <= EXPONENTIAL_SA_QI[0]) {
            return mean * (a + u);
        }

        // Step 6:
        int i = 0; // Should be 1, be we iterate before it in while using 0.
        double u2 = rng.nextDouble();
        double umin = u2;

        // Step 7 and 8:
        do {
            ++i;
            u2 = rng.nextDouble();

            if (u2 < umin) {
                umin = u2;
            }

            // Step 8:
        } while (u > EXPONENTIAL_SA_QI[i]); // Ensured to exit since EXPONENTIAL_SA_QI[MAX] = 1.

        return mean * (a + umin * EXPONENTIAL_SA_QI[0]);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Ahrens-Dieter Exponential deviate [" + rng.toString() + "]";
    }
}
