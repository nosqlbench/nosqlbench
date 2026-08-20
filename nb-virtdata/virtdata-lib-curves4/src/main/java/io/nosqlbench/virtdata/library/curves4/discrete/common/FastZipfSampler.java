/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.library.curves4.discrete.common;

import java.util.function.DoubleToIntFunction;

/**
 * A constant-time approximate inverse Zipf sampler for a finite domain.
 *
 * <p>The first eight ranks use their exact cumulative weights. The remaining ranks use a
 * continuous power-law approximation conditioned on the remaining probability mass.
 * Parameter calculation takes one pass over the ranks ({@code O(numberOfElements)}),
 * except for the uniform case; each subsequent sample takes constant time
 * ({@code O(1)}).</p>
 *
 * <p>{@code numberOfElements} must be at least one and {@code exponent} must be finite
 * and non-negative. The sampler accepts a uniform variate in {@code [0.0, 1.0]}; the
 * upper endpoint is converted to {@code Math.nextDown(1.0)} before sampling over
 * {@code [0.0, 1.0)}.</p>
 */
public final class FastZipfSampler implements DoubleToIntFunction {

    private static final int CDF_TABLE_SIZE = 8;

    private final int numberOfElements;
    private final double alpha;
    private final double[] cdf;

    /**
     * Creates a sampler for a finite Zipf domain.
     *
     * @param numberOfElements the number of ranks, starting at rank one
     * @param exponent the Zipf exponent
     * @throws IllegalArgumentException if {@code numberOfElements < 1}, or if
     *         {@code exponent} is negative or non-finite
     */
    public FastZipfSampler(int numberOfElements, double exponent) {
        if (numberOfElements <= 0) {
            throw new IllegalArgumentException(
                    "numberOfElements must be greater than zero: " + numberOfElements);
        }
        if (!Double.isFinite(exponent) || exponent < 0.0d) {
            throw new IllegalArgumentException(
                    "exponent must be finite and non-negative: " + exponent);
        }

        this.numberOfElements = numberOfElements;

        double zetan = exponent == 0.0d
                ? numberOfElements
                : generalizedHarmonic(numberOfElements, exponent);

        this.cdf = new double[CDF_TABLE_SIZE];
        double cumulative = 0.0d;
        int exactRanks = Math.min(numberOfElements, cdf.length);
        for (int i = 0; i < exactRanks; i++) {
            cumulative += exponent == 0.0d
                    ? 1.0d
                    : Math.pow((double) i + 1.0d, -exponent);
            cdf[i] = cumulative / zetan;
        }

        this.alpha = 1.0d - exponent;
    }

    /**
     * Selects a rank using a uniform variate.
     *
     * @param uniform a finite value in {@code [0.0, 1.0]}
     * @return a rank in {@code [1, numberOfElements]}
     * @throws IllegalArgumentException if {@code uniform} is not in {@code [0.0, 1.0]}
     */
    @Override
    public int applyAsInt(double uniform) {
        if (!Double.isFinite(uniform) || uniform < 0.0d || uniform > 1.0d) {
            throw new IllegalArgumentException(
                    "uniform must be in the range [0.0, 1.0]: " + uniform);
        }

        if (uniform == 1.0d) {
            uniform = Math.nextDown(uniform);
        }

        for (int i = 0; i < cdf.length; i++) {
            if (uniform < cdf[i]) {
                return i + 1;
            }
        }

        double tailCdf = cdf[cdf.length - 1];
        double tailUniform = (uniform - tailCdf) / (1.0d - tailCdf);
        double lowerRank = cdf.length;
        double upperRank = Math.nextDown((double) numberOfElements);
        double logRange = Math.log(upperRank / lowerRank);

        double scale = alpha == 0.0d
                ? tailUniform * logRange
                : Math.log1p(tailUniform * Math.expm1(alpha * logRange)) / alpha;
        long zeroBasedRank = (long) (lowerRank * Math.exp(scale));

        return (int) (Math.min(zeroBasedRank, (long) numberOfElements - 1L) + 1L);
    }

    private static double generalizedHarmonic(int numberOfElements, double exponent) {
        double sum = 0.0d;
        for (int rank = numberOfElements; rank >= 1; rank--) {
            sum += Math.pow(rank, -exponent);
        }
        return sum;
    }
}
