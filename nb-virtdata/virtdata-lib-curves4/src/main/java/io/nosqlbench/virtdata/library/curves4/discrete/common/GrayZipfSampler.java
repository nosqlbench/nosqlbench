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
 * over the ranks; each subsequent sample takes constant time. The limiting form is used
 * when the exponent is one, and the same finite-domain approximation is retained for
 * larger exponents to preserve the existing Zipf mapper parameter range.</p>
 */
public final class GrayZipfSampler implements DoubleToIntFunction {

    private final int numberOfElements;
    private final double exponent;
    private final double zetan;
    private final double zeta2;
    private final double eta;

    public GrayZipfSampler(int numberOfElements, double exponent) {
        if (numberOfElements <= 0) {
            throw new IllegalArgumentException(
                    "numberOfElements must be greater than zero: " + numberOfElements);
        }
        if (!Double.isFinite(exponent) || exponent < 0.0d) {
            throw new IllegalArgumentException(
                    "exponent must be finite and non-negative: " + exponent);
        }

        this.numberOfElements = numberOfElements;
        this.exponent = exponent;
        this.zetan = exponent == 0.0d
                ? numberOfElements
                : generalizedHarmonic(numberOfElements, exponent);
        this.zeta2 = numberOfElements == 1 ? 1.0d : 1.0d + Math.pow(0.5d, exponent);

        if (numberOfElements <= 2 || exponent == 1.0d || zetan == zeta2) {
            this.eta = 0.0d;
        } else {
            double oneMinusExponent = 1.0d - exponent;
            double numerator = -Math.expm1(
                    oneMinusExponent * Math.log(2.0d / numberOfElements));
            this.eta = numerator / (1.0d - zeta2 / zetan);
        }
    }

    @Override
    public int applyAsInt(double uniform) {
        if (uniform <= 0.0d || numberOfElements == 1) {
            return 1;
        }
        if (uniform >= 1.0d) {
            return numberOfElements;
        }
        if (exponent == 0.0d) {
            return 1 + (int) (uniform * numberOfElements);
        }

        double weighted = uniform * zetan;
        if (weighted < 1.0d) {
            return 1;
        }
        if (weighted < zeta2 || numberOfElements == 2) {
            return 2;
        }

        double approximatedRank;
        if (exponent == 1.0d) {
            double harmonicTailScale = Math.log(numberOfElements / 2.0d)
                    * zetan / (zetan - zeta2);
            approximatedRank = numberOfElements
                    * Math.exp(harmonicTailScale * (uniform - 1.0d));
        } else {
            double oneMinusExponent = 1.0d - exponent;
            approximatedRank = numberOfElements * Math.exp(
                    Math.log1p(eta * (uniform - 1.0d)) / oneMinusExponent);
        }

        long sample = 1L + (long) approximatedRank;
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
