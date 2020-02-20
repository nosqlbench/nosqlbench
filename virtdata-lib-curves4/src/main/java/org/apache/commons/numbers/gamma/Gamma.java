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


/**
 * <a href="http://mathworld.wolfram.com/GammaFunction.html">Gamma
 * function</a>.
 * <p>
 * The <a href="http://mathworld.wolfram.com/GammaFunction.html">gamma
 * function</a> can be seen to extend the factorial function to cover real and
 * complex numbers, but with its argument shifted by {@code -1}. This
 * implementation supports real numbers.
 * </p>
 * <p>
 * This class is immutable.
 * </p>
 */
public class Gamma {
    /** &radic;(2&pi;). */
    private static final double SQRT_TWO_PI = 2.506628274631000502;

    /**
     * Computes the value of \( \Gamma(x) \).
     * <p>
     * Based on the <em>NSWC Library of Mathematics Subroutines</em> double
     * precision implementation, {@code DGAMMA}.
     *
     * @param x Argument.
     * @return \( \Gamma(x) \)
     */
    public static double value(final double x) {

        if ((x == Math.rint(x)) && (x <= 0.0)) {
            return Double.NaN;
        }

        final double absX = Math.abs(x);
        if (absX <= 20) {
            if (x >= 1) {
                /*
                 * From the recurrence relation
                 * Gamma(x) = (x - 1) * ... * (x - n) * Gamma(x - n),
                 * then
                 * Gamma(t) = 1 / [1 + InvGamma1pm1.value(t - 1)],
                 * where t = x - n. This means that t must satisfy
                 * -0.5 <= t - 1 <= 1.5.
                 */
                double prod = 1;
                double t = x;
                while (t > 2.5) {
                    t -= 1;
                    prod *= t;
                }
                return prod / (1 + InvGamma1pm1.value(t - 1));
            } else {
                /*
                 * From the recurrence relation
                 * Gamma(x) = Gamma(x + n + 1) / [x * (x + 1) * ... * (x + n)]
                 * then
                 * Gamma(x + n + 1) = 1 / [1 + InvGamma1pm1.value(x + n)],
                 * which requires -0.5 <= x + n <= 1.5.
                 */
                double prod = x;
                double t = x;
                while (t < -0.5) {
                    t += 1;
                    prod *= t;
                }
                return 1 / (prod * (1 + InvGamma1pm1.value(t)));
            }
        } else {
            final double y = absX + LanczosApproximation.g() + 0.5;
            final double gammaAbs = SQRT_TWO_PI / absX *
                                    Math.pow(y, absX + 0.5) *
                                    Math.exp(-y) * LanczosApproximation.value(absX);
            if (x > 0) {
                return gammaAbs;
            } else {
                /*
                 * From the reflection formula
                 * Gamma(x) * Gamma(1 - x) * sin(pi * x) = pi,
                 * and the recurrence relation
                 * Gamma(1 - x) = -x * Gamma(-x),
                 * it is found
                 * Gamma(x) = -pi / [x * sin(pi * x) * Gamma(-x)].
                 */
                return -Math.PI / (x * Math.sin(Math.PI * x) * gammaAbs);
            }
        }
    }
}
