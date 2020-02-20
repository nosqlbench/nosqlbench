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
 * <a href="https://en.wikipedia.org/wiki/Ziggurat_algorithm">
 * Marsaglia and Tsang "Ziggurat" method</a> for sampling from a Gaussian
 * distribution with mean 0 and standard deviation 1.
 *
 * <p>The algorithm is explained in this
 * <a href="http://www.jstatsoft.org/article/view/v005i08/ziggurat.pdf">paper</a>
 * and this implementation has been adapted from the C code provided therein.</p>
 *
 * <p>Sampling uses:</p>
 *
 * <ul>
 *   <li>{@link UniformRandomProvider#nextLong()}
 *   <li>{@link UniformRandomProvider#nextDouble()}
 * </ul>
 *
 * @since 1.1
 */
public class ZigguratNormalizedGaussianSampler
    implements NormalizedGaussianSampler {
    /** Start of tail. */
    private static final double R = 3.442619855899;
    /** Inverse of R. */
    private static final double ONE_OVER_R = 1 / R;
    /** Rectangle area. */
    private static final double V = 9.91256303526217e-3;
    /** 2^63 */
    private static final double MAX = Math.pow(2, 63);
    /** 2^-63 */
    private static final double ONE_OVER_MAX = 1d / MAX;
    /** Number of entries. */
    private static final int LEN = 128;
    /** Index of last entry. */
    private static final int LAST = LEN - 1;
    /** Auxiliary table. */
    private static final long[] K = new long[LEN];
    /** Auxiliary table. */
    private static final double[] W = new double[LEN];
    /** Auxiliary table. */
    private static final double[] F = new double[LEN];
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    static {
        // Filling the tables.

        double d = R;
        double t = d;
        double fd = gauss(d);
        final double q = V / fd;

        K[0] = (long) ((d / q) * MAX);
        K[1] = 0;

        W[0] = q * ONE_OVER_MAX;
        W[LAST] = d * ONE_OVER_MAX;

        F[0] = 1;
        F[LAST] = fd;

        for (int i = LAST - 1; i >= 1; i--) {
            d = Math.sqrt(-2 * Math.log(V / d + fd));
            fd = gauss(d);

            K[i + 1] = (long) ((d / t) * MAX);
            t = d;

            F[i] = fd;

            W[i] = d * ONE_OVER_MAX;
        }
    }

    /**
     * @param rng Generator of uniformly distributed random numbers.
     */
    public ZigguratNormalizedGaussianSampler(UniformRandomProvider rng) {
        this.rng = rng;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        final long j = rng.nextLong();
        final int i = (int) (j & LAST);
        if (Math.abs(j) < K[i]) {
            return j * W[i];
        } else {
            return fix(j, i);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Ziggurat normalized Gaussian deviate [" + rng.toString() + "]";
    }

    /**
     * Gets the value from the tail of the distribution.
     *
     * @param hz Start random integer.
     * @param iz Index of cell corresponding to {@code hz}.
     * @return the requested random value.
     */
    private double fix(long hz,
                       int iz) {
        double x;
        double y;

        x = hz * W[iz];
        if (iz == 0) {
            // Base strip.
            // This branch is called about 5.7624515E-4 times per sample.
            do {
                y = -Math.log(rng.nextDouble());
                x = -Math.log(rng.nextDouble()) * ONE_OVER_R;
            } while (y + y < x * x);

            final double out = R + x;
            return hz > 0 ? out : -out;
        } else {
            // Wedge of other strips.
            // This branch is called about 0.027323 times per sample.
            if (F[iz] + rng.nextDouble() * (F[iz - 1] - F[iz]) < gauss(x)) {
                return x;
            } else {
                // Try again.
                // This branch is called about 0.012362 times per sample.
                return sample();
            }
        }
    }

    /**
     * @param x Argument.
     * @return \( e^{-\frac{x^2}{2}} \)
     */
    private static double gauss(double x) {
        return Math.exp(-0.5 * x * x);
    }
}
