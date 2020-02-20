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

/**
 * <h3>
 *  Adapted and stripped down copy of class
 *  {@code "org.apache.commons.math4.special.Gamma"}.
 *  TODO: Include it in a "core" component upon which high-level functionality
 *  such as sampling can depend.
 * </h3>
 *
 * <p>
 * This is a utility class that provides computation methods related to the
 * &Gamma; (Gamma) family of functions.
 * </p>
 */
class InternalGamma { // Class is package-private on purpose; do not make it public.
    /**
     * Constant \( g = \frac{607}{128} \) in the Lanczos approximation.
     */
    public static final double LANCZOS_G = 607.0 / 128.0;

    /** Lanczos coefficients */
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

    /** Avoid repeated computation of log of 2 PI in logGamma */
    private static final double HALF_LOG_2_PI = 0.5 * Math.log(2.0 * Math.PI);

    /**
     * Class contains only static methods.
     */
    private InternalGamma() {}

    /**
     * Computes the function \( \ln \Gamma(x) \) for \( x > 0 \).
     *
     * <p>
     * For \( x \leq 8 \), the implementation is based on the double precision
     * implementation in the <em>NSWC Library of Mathematics Subroutines</em>,
     * {@code DGAMLN}. For \( x \geq 8 \), the implementation is based on
     * </p>
     *
     * <ul>
     * <li><a href="http://mathworld.wolfram.com/GammaFunction.html">Gamma
     *     Function</a>, equation (28).</li>
     * <li><a href="http://mathworld.wolfram.com/LanczosApproximation.html">
     *     Lanczos Approximation</a>, equations (1) through (5).</li>
     * <li><a href="http://my.fit.edu/~gabdo/gamma.txt">Paul Godfrey, A note on
     *     the computation of the convergent Lanczos complex Gamma
     *     approximation</a></li>
     * </ul>
     *
     * @param x Argument.
     * @return \( \ln \Gamma(x) \), or {@code NaN} if {@code x <= 0}.
     */
    public static double logGamma(double x) {
        // Stripped-down version of the same method defined in "Commons Math":
        // Unused "if" branches (for when x < 8) have been removed here since
        // this method is only used (by class "InternalUtils") in order to
        // compute log(n!) for x > 20.

        final double sum = lanczos(x);
        final double tmp = x + LANCZOS_G + 0.5;
        return (x + 0.5) * Math.log(tmp) - tmp +  HALF_LOG_2_PI + Math.log(sum / x);
    }

    /**
     * Computes the Lanczos approximation used to compute the gamma function.
     *
     * <p>
     * The Lanczos approximation is related to the Gamma function by the
     * following equation
     * \[
     * \Gamma(x) = \sqrt{2\pi} \, \frac{(g + x + \frac{1}{2})^{x + \frac{1}{2}} \, e^{-(g + x + \frac{1}{2})} \, \mathrm{lanczos}(x)}
     *                                 {x}
     * \]
     * where \(g\) is the Lanczos constant.
     * </p>
     *
     * @param x Argument.
     * @return The Lanczos approximation.
     *
     * @see <a href="http://mathworld.wolfram.com/LanczosApproximation.html">Lanczos Approximation</a>
     * equations (1) through (5), and Paul Godfrey's
     * <a href="http://my.fit.edu/~gabdo/gamma.txt">Note on the computation
     * of the convergent Lanczos complex Gamma approximation</a>
     */
    private static double lanczos(final double x) {
        double sum = 0.0;
        for (int i = LANCZOS.length - 1; i > 0; --i) {
            sum += LANCZOS[i] / (x + i);
        }
        return sum + LANCZOS[0];
    }
}
