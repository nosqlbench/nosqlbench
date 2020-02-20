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
 * Computes \( \log_e(\Gamma(a+b)) \).
 * <p>
 * This class is immutable.
 * </p>
 */
class LogGammaSum {
    /**
     * Computes the value of log Γ(a + b) for 1 ≤ a, b ≤ 2.
     * Based on the <em>NSWC Library of Mathematics Subroutines</em>
     * implementation, {@code DGSMLN}.
     *
     * @param a First argument.
     * @param b Second argument.
     * @return the value of {@code log(Gamma(a + b))}.
     * @throws IllegalArgumentException if {@code a} or {@code b} is lower than 1
     * or larger than 2.
     */
    static double value(double a,
                        double b) {
        if (a < 1 ||
            a > 2) {
            throw new GammaException(GammaException.OUT_OF_RANGE, a, 1, 2);
        }
        if (b < 1 ||
            b > 2) {
            throw new GammaException(GammaException.OUT_OF_RANGE, b, 1, 2);
        }

        final double x = (a - 1) + (b - 1);
        if (x <= 0.5) {
            return LogGamma1p.value(1 + x);
        } else if (x <= 1.5) {
            return LogGamma1p.value(x) + Math.log1p(x);
        } else {
            return LogGamma1p.value(x - 1) + Math.log(x * (1 + x));
        }
    }
}
