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
 * Function \( \ln \Gamma(x) \).
 *
 * Class is immutable.
 */
public class LogGamma {
    /** Lanczos constant. */
    private static final double LANCZOS_G = 607d / 128d;
    /** Performance. */
    private static final double HALF_LOG_2_PI = 0.5 * Math.log(2.0 * Math.PI);

    /**
     * Computes the function \( \ln \Gamma(x) \) for {@code x >= 0}.
     *
     * For {@code x <= 8}, the implementation is based on the double precision
     * implementation in the <em>NSWC Library of Mathematics Subroutines</em>,
     * {@code DGAMLN}. For {@code x >= 8}, the implementation is based on
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
    public static double value(double x) {
        if (Double.isNaN(x) || (x <= 0.0)) {
            return Double.NaN;
        } else if (x < 0.5) {
            return LogGamma1p.value(x) - Math.log(x);
        } else if (x <= 2.5) {
            return LogGamma1p.value((x - 0.5) - 0.5);
        } else if (x <= 8.0) {
            final int n = (int) Math.floor(x - 1.5);
            double prod = 1.0;
            for (int i = 1; i <= n; i++) {
                prod *= x - i;
            }
            return LogGamma1p.value(x - (n + 1)) + Math.log(prod);
        } else {
            final double sum = LanczosApproximation.value(x);
            final double tmp = x + LANCZOS_G + .5;
            return ((x + .5) * Math.log(tmp)) - tmp +
                HALF_LOG_2_PI + Math.log(sum / x);
        }
    }
}
