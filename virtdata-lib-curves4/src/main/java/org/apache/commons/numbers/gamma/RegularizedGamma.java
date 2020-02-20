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
package org.apache.commons.numbers.gamma;

import org.apache.commons.numbers.fraction.ContinuedFraction;

/**
 * <a href="http://mathworld.wolfram.com/RegularizedGammaFunction.html">
 * Regularized Gamma functions</a>.
 *
 * Class is immutable.
 */
public class RegularizedGamma {
    /** Maximum allowed numerical error. */
    private static final double DEFAULT_EPSILON = 1e-15;

    /**
     * \( P(a, x) \) <a href="http://mathworld.wolfram.com/RegularizedGammaFunction.html">
     * regularized Gamma function</a>.
     *
     * Class is immutable.
     */
    public static class P {
        /**
         * Computes the regularized gamma function \( P(a, x) \).
         *
         * @param a Argument.
         * @param x Argument.
         * @return \( P(a, x) \).
         */
        public static double value(double a,
                                   double x) {
            return value(a, x, DEFAULT_EPSILON, Integer.MAX_VALUE);
        }

        /**
         * Computes the regularized gamma function \( P(a, x) \).
         *
         * The implementation of this method is based on:
         * <ul>
         *  <li>
         *   <a href="http://mathworld.wolfram.com/RegularizedGammaFunction.html">
         *   Regularized Gamma Function</a>, equation (1)
         *  </li>
         *  <li>
         *   <a href="http://mathworld.wolfram.com/IncompleteGammaFunction.html">
         *   Incomplete Gamma Function</a>, equation (4).
         *  </li>
         *  <li>
         *   <a href="http://mathworld.wolfram.com/ConfluentHypergeometricFunctionoftheFirstKind.html">
         *   Confluent Hypergeometric Function of the First Kind</a>, equation (1).
         *  </li>
         * </ul>
         *
         * @param a Argument.
         * @param x Argument.
         * @param epsilon Tolerance in continued fraction evaluation.
         * @param maxIterations Maximum number of iterations in continued fraction evaluation.
         * @return \( P(a, x) \).
         */
        public static double value(double a,
                                   double x,
                                   double epsilon,
                                   int maxIterations) {
            if (Double.isNaN(a) ||
                Double.isNaN(x) ||
                a <= 0 ||
                x < 0) {
                return Double.NaN;
            } else if (x == 0) {
                return 0;
            } else if (x >= a + 1) {
                // Q should converge faster in this case.
                return 1 - Q.value(a, x, epsilon, maxIterations);
            } else {
                // Series.
                double n = 0; // current element index
                double an = 1 / a; // n-th element in the series
                double sum = an; // partial sum
                while (Math.abs(an / sum) > epsilon &&
                       n < maxIterations &&
                       sum < Double.POSITIVE_INFINITY) {
                    // compute next element in the series
                    n += 1;
                    an *= x / (a + n);

                    // update partial sum
                    sum += an;
                }
                if (n >= maxIterations) {
                    throw new GammaException(GammaException.CONVERGENCE, maxIterations);
                } else if (Double.isInfinite(sum)) {
                    return 1;
                } else {
                    return Math.exp(-x + (a * Math.log(x)) - LogGamma.value(a)) * sum;
                }
            }
        }
    }

    /**
     * Creates the \( Q(a, x) \equiv 1 - P(a, x) \) <a href="http://mathworld.wolfram.com/RegularizedGammaFunction.html">
     * regularized Gamma function</a>.
     *
     * Class is immutable.
     */
    public static class Q {
        /**
         * Computes the regularized gamma function \( Q(a, x) = 1 - P(a, x) \).
         *
         * @param a Argument.
         * @param x Argument.
         * @return \( Q(a, x) \).
         */
        public static double value(double a,
                                   double x) {
            return value(a, x, DEFAULT_EPSILON, Integer.MAX_VALUE);
        }

        /**
         * Computes the regularized gamma function \( Q(a, x) = 1 - P(a, x) \).
         *
         * The implementation of this method is based on:
         * <ul>
         *  <li>
         *   <a href="http://mathworld.wolfram.com/RegularizedGammaFunction.html">
         *   Regularized Gamma Function</a>, equation (1).
         *  </li>
         *  <li>
         *   <a href="http://functions.wolfram.com/GammaBetaErf/GammaRegularized/10/0003/">
         *   Regularized incomplete gamma function: Continued fraction representations
         *   (formula 06.08.10.0003)</a>
         *  </li>
         * </ul>
         *
         * @param a Argument.
         * @param x Argument.
         * @param epsilon Tolerance in continued fraction evaluation.
         * @param maxIterations Maximum number of iterations in continued fraction evaluation.
         * @return \( Q(a, x) \).
         */
        public static double value(final double a,
                                   double x,
                                   double epsilon,
                                   int maxIterations) {
            if (Double.isNaN(a) ||
                Double.isNaN(x) ||
                a <= 0 ||
                x < 0) {
                return Double.NaN;
            } else if (x == 0) {
                return 1;
            } else if (x < a + 1) {
                // P should converge faster in this case.
                return 1 - P.value(a, x, epsilon, maxIterations);
            } else {
                final ContinuedFraction cf = new ContinuedFraction() {
                        /** {@inheritDoc} */
                        @Override
                        protected double getA(int n, double x) {
                            return ((2 * n) + 1) - a + x;
                        }

                        /** {@inheritDoc} */
                        @Override
                        protected double getB(int n, double x) {
                            return n * (a - n);
                        }
                    };

                return Math.exp(-x + (a * Math.log(x)) - LogGamma.value(a)) /
                    cf.evaluate(x, epsilon, maxIterations);
            }
        }
    }
}
