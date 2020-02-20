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
 * Implementation of the <a href="https://en.wikipedia.org/wiki/Zipf's_law">Zipf distribution</a>.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.0
 */
public class RejectionInversionZipfSampler
    extends SamplerBase
    implements DiscreteSampler {
    /** Threshold below which Taylor series will be used. */
    private static final double TAYLOR_THRESHOLD = 1e-8;
    /** 1/2 */
    private static final double F_1_2 = 0.5;
    /** 1/3 */
    private static final double F_1_3 = 1d / 3;
    /** 1/4 */
    private static final double F_1_4 = 0.25;
    /** Number of elements. */
    private final int numberOfElements;
    /** Exponent parameter of the distribution. */
    private final double exponent;
    /** {@code hIntegral(1.5) - 1}. */
    private final double hIntegralX1;
    /** {@code hIntegral(numberOfElements + 0.5)}. */
    private final double hIntegralNumberOfElements;
    /** {@code 2 - hIntegralInverse(hIntegral(2.5) - h(2)}. */
    private final double s;
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param numberOfElements Number of elements.
     * @param exponent Exponent.
     * @throws IllegalArgumentException if {@code numberOfElements <= 0}
     * or {@code exponent <= 0}.
     */
    public RejectionInversionZipfSampler(UniformRandomProvider rng,
                                         int numberOfElements,
                                         double exponent) {
        super(null);
        this.rng = rng;
        if (numberOfElements <= 0) {
            throw new IllegalArgumentException("number of elements is not strictly positive: " + numberOfElements);
        }
        if (exponent <= 0) {
            throw new IllegalArgumentException("exponent is not strictly positive: " + exponent);
        }

        this.numberOfElements = numberOfElements;
        this.exponent = exponent;
        this.hIntegralX1 = hIntegral(1.5) - 1;
        this.hIntegralNumberOfElements = hIntegral(numberOfElements + F_1_2);
        this.s = 2 - hIntegralInverse(hIntegral(2.5) - h(2));
    }

    /**
     * Rejection inversion sampling method for a discrete, bounded Zipf
     * distribution that is based on the method described in
     * <blockquote>
     *   Wolfgang Hörmann and Gerhard Derflinger.
     *   <i>"Rejection-inversion to generate variates from monotone discrete
     *    distributions",</i><br>
     *   <strong>ACM Transactions on Modeling and Computer Simulation</strong> (TOMACS) 6.3 (1996): 169-184.
     * </blockquote>
     */
    @Override
    public int sample() {
        // The paper describes an algorithm for exponents larger than 1
        // (Algorithm ZRI).
        // The original method uses
        //   H(x) = (v + x)^(1 - q) / (1 - q)
        // as the integral of the hat function.
        // This function is undefined for q = 1, which is the reason for
        // the limitation of the exponent.
        // If instead the integral function
        //   H(x) = ((v + x)^(1 - q) - 1) / (1 - q)
        // is used,
        // for which a meaningful limit exists for q = 1, the method works
        // for all positive exponents.
        // The following implementation uses v = 0 and generates integral
        // number in the range [1, numberOfElements].
        // This is different to the original method where v is defined to
        // be positive and numbers are taken from [0, i_max].
        // This explains why the implementation looks slightly different.

        while(true) {
            final double u = hIntegralNumberOfElements + rng.nextDouble() * (hIntegralX1 - hIntegralNumberOfElements);
            // u is uniformly distributed in (hIntegralX1, hIntegralNumberOfElements]

            double x = hIntegralInverse(u);
            int k = (int) (x + F_1_2);

            // Limit k to the range [1, numberOfElements] if it would be outside
            // due to numerical inaccuracies.
            if (k < 1) {
                k = 1;
            } else if (k > numberOfElements) {
                k = numberOfElements;
            }

            // Here, the distribution of k is given by:
            //
            //   P(k = 1) = C * (hIntegral(1.5) - hIntegralX1) = C
            //   P(k = m) = C * (hIntegral(m + 1/2) - hIntegral(m - 1/2)) for m >= 2
            //
            //   where C = 1 / (hIntegralNumberOfElements - hIntegralX1)

            if (k - x <= s || u >= hIntegral(k + F_1_2) - h(k)) {

                // Case k = 1:
                //
                //   The right inequality is always true, because replacing k by 1 gives
                //   u >= hIntegral(1.5) - h(1) = hIntegralX1 and u is taken from
                //   (hIntegralX1, hIntegralNumberOfElements].
                //
                //   Therefore, the acceptance rate for k = 1 is P(accepted | k = 1) = 1
                //   and the probability that 1 is returned as random value is
                //   P(k = 1 and accepted) = P(accepted | k = 1) * P(k = 1) = C = C / 1^exponent
                //
                // Case k >= 2:
                //
                //   The left inequality (k - x <= s) is just a short cut
                //   to avoid the more expensive evaluation of the right inequality
                //   (u >= hIntegral(k + 0.5) - h(k)) in many cases.
                //
                //   If the left inequality is true, the right inequality is also true:
                //     Theorem 2 in the paper is valid for all positive exponents, because
                //     the requirements h'(x) = -exponent/x^(exponent + 1) < 0 and
                //     (-1/hInverse'(x))'' = (1+1/exponent) * x^(1/exponent-1) >= 0
                //     are both fulfilled.
                //     Therefore, f(x) = x - hIntegralInverse(hIntegral(x + 0.5) - h(x))
                //     is a non-decreasing function. If k - x <= s holds,
                //     k - x <= s + f(k) - f(2) is obviously also true which is equivalent to
                //     -x <= -hIntegralInverse(hIntegral(k + 0.5) - h(k)),
                //     -hIntegralInverse(u) <= -hIntegralInverse(hIntegral(k + 0.5) - h(k)),
                //     and finally u >= hIntegral(k + 0.5) - h(k).
                //
                //   Hence, the right inequality determines the acceptance rate:
                //   P(accepted | k = m) = h(m) / (hIntegrated(m+1/2) - hIntegrated(m-1/2))
                //   The probability that m is returned is given by
                //   P(k = m and accepted) = P(accepted | k = m) * P(k = m) = C * h(m) = C / m^exponent.
                //
                // In both cases the probabilities are proportional to the probability mass function
                // of the Zipf distribution.

                return k;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Rejection inversion Zipf deviate [" + rng.toString() + "]";
    }

    /**
     * {@code H(x)} is defined as
     * <ul>
     *  <li>{@code (x^(1 - exponent) - 1) / (1 - exponent)}, if {@code exponent != 1}</li>
     *  <li>{@code log(x)}, if {@code exponent == 1}</li>
     * </ul>
     * H(x) is an integral function of h(x), the derivative of H(x) is h(x).
     *
     * @param x Free parameter.
     * @return {@code H(x)}.
     */
    private double hIntegral(final double x) {
        final double logX = Math.log(x);
        return helper2((1 - exponent) * logX) * logX;
    }

    /**
     * {@code h(x) = 1 / x^exponent}
     *
     * @param x Free parameter.
     * @return {@code h(x)}.
     */
    private double h(final double x) {
        return Math.exp(-exponent * Math.log(x));
    }

    /**
     * The inverse function of {@code H(x)}.
     *
     * @param x Free parameter
     * @return y for which {@code H(y) = x}.
     */
    private double hIntegralInverse(final double x) {
        double t = x * (1 - exponent);
        if (t < -1) {
            // Limit value to the range [-1, +inf).
            // t could be smaller than -1 in some rare cases due to numerical errors.
            t = -1;
        }
        return Math.exp(helper1(t) * x);
    }

    /**
     * Helper function that calculates {@code log(1 + x) / x}.
     * <p>
     * A Taylor series expansion is used, if x is close to 0.
     * </p>
     *
     * @param x A value larger than or equal to -1.
     * @return {@code log(1 + x) / x}.
     */
    private static double helper1(final double x) {
        if (Math.abs(x) > TAYLOR_THRESHOLD) {
            return Math.log1p(x) / x;
        } else {
            return 1 - x * (F_1_2 - x * (F_1_3 - F_1_4 * x));
        }
    }

    /**
     * Helper function to calculate {@code (exp(x) - 1) / x}.
     * <p>
     * A Taylor series expansion is used, if x is close to 0.
     * </p>
     *
     * @param x Free parameter.
     * @return {@code (exp(x) - 1) / x} if x is non-zero, or 1 if x = 0.
     */
    private static double helper2(final double x) {
        if (Math.abs(x) > TAYLOR_THRESHOLD) {
            return Math.expm1(x) / x;
        } else {
            return 1 + x * F_1_2 * (1 + x * F_1_3 * (1 + F_1_4 * x));
        }
    }
}
