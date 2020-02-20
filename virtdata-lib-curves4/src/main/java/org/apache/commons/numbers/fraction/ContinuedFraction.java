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
package org.apache.commons.numbers.fraction;

import org.apache.commons.numbers.core.Precision;

/**
 * Provides a generic means to evaluate
 * <a href="http://mathworld.wolfram.com/ContinuedFraction.html">continued fractions</a>.
 * Subclasses must provide the {@link #getA(int,double) a} and {@link #getB(int,double) b}
 * coefficients to evaluate the continued fraction.
 */
public abstract class ContinuedFraction {
    /** Maximum allowed numerical error. */
    private static final double DEFAULT_EPSILON = 1e-9;

    /**
     * Defines the <a href="http://mathworld.wolfram.com/ContinuedFraction.html">
     * {@code n}-th "a" coefficient</a> of the continued fraction.
     *
     * @param n Index of the coefficient to retrieve.
     * @param x Evaluation point.
     * @return the coefficient <code>a<sub>n</sub></code>.
     */
    protected abstract double getA(int n, double x);

    /**
     * Defines the <a href="http://mathworld.wolfram.com/ContinuedFraction.html">
     * {@code n}-th "b" coefficient</a> of the continued fraction.
     *
     * @param n Index of the coefficient to retrieve.
     * @param x Evaluation point.
     * @return the coefficient <code>b<sub>n</sub></code>.
     */
    protected abstract double getB(int n, double x);

    /**
     * Evaluates the continued fraction.
     *
     * @param x Point at which to evaluate the continued fraction.
     * @return the value of the continued fraction evaluated at {@code x}.
     * @throws ArithmeticException if the algorithm fails to converge.
     * @throws ArithmeticException if the maximal number of iterations is reached
     * before the expected convergence is achieved.
     *
     * @see #evaluate(double,double,int)
     */
    public double evaluate(double x) {
        return evaluate(x, DEFAULT_EPSILON, Integer.MAX_VALUE);
    }

    /**
     * Evaluates the continued fraction.
     *
     * @param x the evaluation point.
     * @param epsilon Maximum error allowed.
     * @return the value of the continued fraction evaluated at {@code x}.
     * @throws ArithmeticException if the algorithm fails to converge.
     * @throws ArithmeticException if the maximal number of iterations is reached
     * before the expected convergence is achieved.
     *
     * @see #evaluate(double,double,int)
     */
    public double evaluate(double x, double epsilon) {
        return evaluate(x, epsilon, Integer.MAX_VALUE);
    }

    /**
     * Evaluates the continued fraction at the value x.
     * @param x the evaluation point.
     * @param maxIterations Maximum number of iterations.
     * @return the value of the continued fraction evaluated at {@code x}.
     * @throws ArithmeticException if the algorithm fails to converge.
     * @throws ArithmeticException if the maximal number of iterations is reached
     * before the expected convergence is achieved.
     *
     * @see #evaluate(double,double,int)
     */
    public double evaluate(double x, int maxIterations) {
        return evaluate(x, DEFAULT_EPSILON, maxIterations);
    }

    /**
     * Evaluates the continued fraction.
     * <p>
     * The implementation of this method is based on the modified Lentz algorithm as described
     * on page 18 ff. in:
     * </p>
     *
     * <ul>
     *   <li>
     *   I. J. Thompson,  A. R. Barnett. "Coulomb and Bessel Functions of Complex Arguments and Order."
     *   <a target="_blank" href="http://www.fresco.org.uk/papers/Thompson-JCP64p490.pdf">
     *   http://www.fresco.org.uk/papers/Thompson-JCP64p490.pdf</a>
     *   </li>
     * </ul>
     *
     * @param x Point at which to evaluate the continued fraction.
     * @param epsilon Maximum error allowed.
     * @param maxIterations Maximum number of iterations.
     * @return the value of the continued fraction evaluated at {@code x}.
     * @throws ArithmeticException if the algorithm fails to converge.
     * @throws ArithmeticException if the maximal number of iterations is reached
     * before the expected convergence is achieved.
     */
    public double evaluate(double x, double epsilon, int maxIterations) {
        final double small = 1e-50;
        double hPrev = getA(0, x);

        // use the value of small as epsilon criteria for zero checks
        if (Precision.equals(hPrev, 0.0, small)) {
            hPrev = small;
        }

        int n = 1;
        double dPrev = 0.0;
        double cPrev = hPrev;
        double hN = hPrev;

        while (n <= maxIterations) {
            final double a = getA(n, x);
            final double b = getB(n, x);

            double dN = a + b * dPrev;
            if (Precision.equals(dN, 0.0, small)) {
                dN = small;
            }
            double cN = a + b / cPrev;
            if (Precision.equals(cN, 0.0, small)) {
                cN = small;
            }

            dN = 1 / dN;
            final double deltaN = cN * dN;
            hN = hPrev * deltaN;

            if (Double.isInfinite(hN)) {
                throw new FractionException("Continued fraction convergents diverged to +/- infinity for value {0}",
                                               x);
            }
            if (Double.isNaN(hN)) {
                throw new FractionException("Continued fraction diverged to NaN for value {0}",
                                               x);
            }

            if (Math.abs(deltaN - 1) < epsilon) {
                return hN;
            }

            dPrev = dN;
            cPrev = cN;
            hPrev = hN;
            ++n;
        }

        throw new FractionException("maximal count ({0}) exceeded", maxIterations);
    }
}
