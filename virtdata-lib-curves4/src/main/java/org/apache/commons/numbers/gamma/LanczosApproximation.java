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
 * <a href="http://mathworld.wolfram.com/LanczosApproximation.html">
 * Lanczos approximation</a> to the Gamma function.
 *
 * It is related to the Gamma function by the following equation
 * \[
 * \Gamma(x) = \sqrt{2\pi} \, \frac{(g + x + \frac{1}{2})^{x + \frac{1}{2}} \, e^{-(g + x + \frac{1}{2})} \, \mathrm{lanczos}(x)}
 *                                 {x}
 * \]
 * where \( g \) is the Lanczos constant.
 *
 * See equations (1) through (5), and Paul Godfrey's
 * <a href="http://my.fit.edu/~gabdo/gamma.txt">Note on the computation
 * of the convergent Lanczos complex Gamma approximation</a>.
 */
public class LanczosApproximation {
    /** \( g = \frac{607}{128} \). */
    private static final double LANCZOS_G = 607d / 128d;
    /** Lanczos coefficients. */
    private static final double[] LANCZOS = {
        0.99999999999999709182,
        57.156235665862923517,
        -59.597960355475491248,
        14.136097974741747174,
        -0.49191381609762019978,
        .33994649984811888699e-4,
        .46523628927048575665e-4,
        -.98374475304879564677e-4,
        .15808870322491248884e-3,
        -.21026444172410488319e-3,
        .21743961811521264320e-3,
        -.16431810653676389022e-3,
        .84418223983852743293e-4,
        -.26190838401581408670e-4,
        .36899182659531622704e-5,
    };

    /**
     * Computes the Lanczos approximation.
     *
     * @param x Argument.
     * @return the Lanczos approximation.
     */
    public static double value(final double x) {
        double sum = 0;
        for (int i = LANCZOS.length - 1; i > 0; i--) {
            sum += LANCZOS[i] / (x + i);
        }
        return sum + LANCZOS[0];
    }

    /**
     * @return the Lanczos constant \( g = \frac{607}{128} \).
     */
    public static double g() {
        return LANCZOS_G;
    }
}
