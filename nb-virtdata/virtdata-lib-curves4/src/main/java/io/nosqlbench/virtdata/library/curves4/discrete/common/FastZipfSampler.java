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
 * The constant-time approximate inverse Zipf sampler described by Jim Gray et al. in
 * <a href="https://doi.org/10.1145/191843.191886">Quickly Generating Billion-Record
 * Synthetic Databases</a>.
 *
 * <p>The first two ranks use their exact cumulative weights. The remaining ranks use
 * the paper's corrected continuous approximation. Parameter calculation takes one pass
 * over the ranks ({@code O(numberOfElements)}); each subsequent sample takes constant
 * time ({@code O(1)}).</p>
 *
 * <p>This implementation uses the parameter range defined by Gray's algorithm:
 * {@code numberOfElements >= 1} and {@code 0.0 <= exponent < 1.0}. The sampler accepts
 * a uniform variate in {@code [0.0, 1.0]}; the upper endpoint is converted to
 * {@code Math.nextDown(1.0)} before evaluating Gray's formula over {@code [0.0, 1.0)}.</p>
 */
public final class FastZipfSampler implements DoubleToIntFunction {

    private final int numberOfElements;
    private final double alpha;
    private final double[] cdf;
    private final double eta;

    /**
     * Creates a sampler for a finite Zipf domain.
     *
     * @param numberOfElements the number of ranks, starting at rank one
     * @param exponent Gray's theta parameter
     * @throws IllegalArgumentException if {@code numberOfElements < 1}, or if
     *         {@code exponent} is not in {@code [0.0, 1.0)}
     */
    public FastZipfSampler(int numberOfElements, double exponent) {
        if (numberOfElements <= 0) {
            throw new IllegalArgumentException(
                    "numberOfElements must be greater than zero: " + numberOfElements);
        }
        if (!Double.isFinite(exponent) || exponent < 0.0d || exponent >= 1.0d) {
            throw new IllegalArgumentException(
                    "exponent must be in the range [0.0, 1.0): " + exponent);
        }

        this.numberOfElements = numberOfElements;

        double zetan = exponent == 0.0d
                ? numberOfElements
                : generalizedHarmonic(numberOfElements, exponent);
        double zeta2 = numberOfElements == 1
                ? 1.0d
                : 1.0d + Math.pow(0.5d, exponent);

        this.cdf = new double[]{1.0d / zetan, zeta2 / zetan};
        this.alpha = 1.0d / (1.0d - exponent);

        if (numberOfElements <= 2) {
            this.eta = 0.0d;
        } else {
            double oneMinusExponent = 1.0d - exponent;
            double numerator = -Math.expm1(
                    oneMinusExponent * Math.log(2.0d / numberOfElements));
            this.eta = numerator / (1.0d - cdf[1]);
        }
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

        double rank = (double) numberOfElements * Math.exp(
                alpha * Math.log1p(eta * (uniform - 1.0d)));
        long sample = 1L + (long) rank;

        return (int) Math.max(3L, Math.min(numberOfElements, sample));
    }

    private static double generalizedHarmonic(int numberOfElements, double exponent) {
        double sum = 0.0d;
        for (int rank = numberOfElements; rank >= 1; rank--) {
            sum += Math.pow(rank, -exponent);
        }
        return sum;
    }
}
