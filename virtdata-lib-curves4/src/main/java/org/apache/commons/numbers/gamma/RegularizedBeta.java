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
 * <a href="http://mathworld.wolfram.com/RegularizedBetaFunction.html">
 * Regularized Beta function</a>.
 * <p>
 * This class is immutable.
 * </p>
 */
public class RegularizedBeta {
    /** Maximum allowed numerical error. */
    private static final double DEFAULT_EPSILON = 1e-14;

    /**
     * Computes the value of the
     * <a href="http://mathworld.wolfram.com/RegularizedBetaFunction.html">
     * regularized beta function</a> I(x, a, b).
     *
     * @param x Value.
     * @param a Parameter {@code a}.
     * @param b Parameter {@code b}.
     * @return the regularized beta function I(x, a, b).
     * @throws ArithmeticException if the algorithm fails to converge.
     */
    public static double value(double x,
                               double a,
                               double b) {
        return value(x, a, b, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }


    /**
     * Computes the value of the
     * <a href="http://mathworld.wolfram.com/RegularizedBetaFunction.html">
     * regularized beta function</a> I(x, a, b).
     *
     * The implementation of this method is based on:
     * <ul>
     *  <li>
     *   <a href="http://mathworld.wolfram.com/RegularizedBetaFunction.html">
     *   Regularized Beta Function</a>.
     *  </li>
     *  <li>
     *   <a href="http://functions.wolfram.com/06.21.10.0001.01">
     *   Regularized Beta Function</a>.
     *  </li>
     * </ul>
     *
     * @param x the value.
     * @param a Parameter {@code a}.
     * @param b Parameter {@code b}.
     * @param epsilon When the absolute value of the nth item in the
     * series is less than epsilon the approximation ceases to calculate
     * further elements in the series.
     * @param maxIterations Maximum number of "iterations" to complete.
     * @return the regularized beta function I(x, a, b).
     * @throws ArithmeticException if the algorithm fails to converge.
     */
    public static double value(double x,
                               final double a,
                               final double b,
                               double epsilon,
                               int maxIterations) {
        if (Double.isNaN(x) ||
            Double.isNaN(a) ||
            Double.isNaN(b) ||
            x < 0 ||
            x > 1 ||
            a <= 0 ||
            b <= 0) {
            return Double.NaN;
        } else if (x > (a + 1) / (2 + b + a) &&
                   1 - x <= (b + 1) / (2 + b + a)) {
            return 1 - value(1 - x, b, a, epsilon, maxIterations);
        } else {
            final ContinuedFraction fraction = new ContinuedFraction() {
                /** {@inheritDoc} */
                @Override
                protected double getB(int n, double x) {
                    if (n % 2 == 0) { // even
                        final double m = n / 2d;
                        return (m * (b - m) * x) /
                            ((a + (2 * m) - 1) * (a + (2 * m)));
                    } else {
                        final double m = (n - 1d) / 2d;
                        return -((a + m) * (a + b + m) * x) /
                            ((a + (2 * m)) * (a + (2 * m) + 1));
                    }
                }

                /** {@inheritDoc} */
                @Override
                protected double getA(int n, double x) {
                    return 1;
                }
            };

            return Math.exp((a * Math.log(x)) + (b * Math.log1p(-x)) -
                            Math.log(a) - LogBeta.value(a, b)) /
                fraction.evaluate(x, epsilon, maxIterations);
        }
    }
}
