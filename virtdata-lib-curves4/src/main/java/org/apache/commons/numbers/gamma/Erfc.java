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
 * <a href="http://mathworld.wolfram.com/Erfc.html">Complementary error function</a>.
 */
public class Erfc {
    /**
     * <p>
     * This implementation computes erfc(x) using the
     * {@link RegularizedGamma.Q#value(double, double, double, int) regularized gamma function},
     * following <a href="http://mathworld.wolfram.com/Erf.html">Erf</a>, equation (3).
     * </p>
     *
     * <p>
     * The value returned is always between 0 and 2 (inclusive).
     * If {@code abs(x) > 40}, then {@code erf(x)} is indistinguishable from
     * either 0 or 2 at {@code double} precision, so the appropriate extreme
     * value is returned.
     * </p>
     *
     * @param x Value.
     * @return the complementary error function.
     * @throws ArithmeticException if the algorithm fails to converge.
     *
     * @see RegularizedGamma.Q#value(double, double, double, int)
     */
    public static double value(double x) {
        if (Math.abs(x) > 40) {
            return x > 0 ? 0 : 2;
        }
        final double ret = RegularizedGamma.Q.value(0.5, x * x, 1e-15, 10000);
        return x < 0 ?
            2 - ret :
            ret;
    }
}

