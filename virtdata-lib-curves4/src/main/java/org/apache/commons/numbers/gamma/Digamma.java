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
 * <a href="http://en.wikipedia.org/wiki/Digamma_function">Digamma function</a>.
 * <p>
 * It is defined as the logarithmic derivative of the \( \Gamma \)
 * ({@link Gamma}) function:
 * \( \frac{d}{dx}(\ln \Gamma(x)) = \frac{\Gamma^\prime(x)}{\Gamma(x)} \).
 * </p>
 *
 * @see Gamma
 */
public class Digamma {
    /** <a href="http://en.wikipedia.org/wiki/Euler-Mascheroni_constant">Euler-Mascheroni constant</a>. */
    private static final double GAMMA = 0.577215664901532860606512090082;
    /** C limit. */
    private static final double C_LIMIT = 49;
    /** S limit. */
    private static final double S_LIMIT = 1e-5;
    /** Fraction. */
    private static final double F_M1_12 = -1d / 12;
    /** Fraction. */
    private static final double F_1_120 = 1d / 120;
    /** Fraction. */
    private static final double F_M1_252 = -1d / 252;

    /**
     * Computes the digamma function.
     *
     * This is an independently written implementation of the algorithm described in
     * <a href="http://www.uv.es/~bernardo/1976AppStatist.pdf">Jose Bernardo,
     * Algorithm AS 103: Psi (Digamma) Function, Applied Statistics, 1976</a>.
     * A <a href="https://en.wikipedia.org/wiki/Digamma_function#Reflection_formula">
     * reflection formula</a> is incorporated to improve performance on negative values.
     *
     * Some of the constants have been changed to increase accuracy at the moderate
     * expense of run-time.  The result should be accurate to within {@code 1e-8}.
     * relative tolerance for {@code 0 < x < 1e-5}  and within {@code 1e-8} absolute
     * tolerance otherwise.
     *
     * @param x Argument.
     * @return digamma(x) to within {@code 1e-8} relative or absolute error whichever
     * is larger.
     */
    public static double value(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            return x;
        }

        double digamma = 0;
        if (x < 0) {
            // Use reflection formula to fall back into positive values.
            digamma -= Math.PI / Math.tan(Math.PI * x);
            x = 1 - x;
        }

        if (x > 0 && x <= S_LIMIT) {
            // Use method 5 from Bernardo AS103, accurate to O(x).
            return digamma - GAMMA - 1 / x;
        }

        while (x < C_LIMIT) {
            digamma -= 1 / x;
            x += 1;
        }

        // Use method 4, accurate to O(1/x^8)
        final double inv = 1 / (x * x);
        //            1       1        1         1
        // log(x) -  --- - ------ + ------- - -------
        //           2 x   12 x^2   120 x^4   252 x^6
        digamma += Math.log(x) - 0.5 / x + inv * (F_M1_12 + inv * (F_1_120 + F_M1_252 * inv));

        return digamma;
    }
}
