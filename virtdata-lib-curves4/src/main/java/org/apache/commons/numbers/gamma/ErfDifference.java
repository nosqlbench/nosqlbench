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
 * Computes the difference between {@link Erf error function values}.
 */
public class ErfDifference {
    /**
     * This number solves {@code erf(x) = 0.5} within 1 ulp.
     * More precisely, the current implementations of
     * {@link Erf#value(double)} and {@link Erfc#value(double)} satisfy:
     * <ul>
     *  <li>{@code Erf.value(X_CRIT) < 0.5},</li>
     *  <li>{@code Erf.value(Math.nextUp(X_CRIT) > 0.5},</li>
     *  <li>{@code Erfc.value(X_CRIT) = 0.5}, and</li>
     *  <li>{@code Erfc.value(Math.nextUp(X_CRIT) < 0.5}</li>
     * </ul>
     */
    private static final double X_CRIT = 0.4769362762044697;

    /**
     * The implementation uses either {@link Erf} or {@link Erfc},
     * depending on which provides the most precise result.
     *
     * @param x1 First value.
     * @param x2 Second value.
     * @return {@link Erf#value(double) Erf.value(x2) - Erf.value(x1)}.
     */
    public static double value(double x1,
                               double x2) {
        if (x1 > x2) {
            return -value(x2, x1);
        } else {
            if (x1 < -X_CRIT) {
                if (x2 < 0) {
                    return Erfc.value(-x2) - Erfc.value(-x1);
                } else {
                    return Erf.value(x2) - Erf.value(x1);
                }
            } else {
                if (x2 > X_CRIT &&
                    x1 > 0) {
                    return Erfc.value(x1) - Erfc.value(x2);
                } else {
                    return Erf.value(x2) - Erf.value(x1);
                }
            }
        }
    }
}
