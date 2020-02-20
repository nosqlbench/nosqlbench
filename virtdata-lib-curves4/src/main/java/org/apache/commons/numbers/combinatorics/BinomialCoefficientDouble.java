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

package org.apache.commons.numbers.combinatorics;

/**
 * Representation of the <a href="http://mathworld.wolfram.com/BinomialCoefficient.html">
 * binomial coefficient</a>, as a {@code double}.
 * It is "{@code n choose k}", the number of {@code k}-element subsets that
 * can be selected from an {@code n}-element set.
 */
public class BinomialCoefficientDouble {
    /**
     * Computes de binomial coefficient.
     * The largest value of {@code n} for which all coefficients can
     * fit into a {@code long} is 66.
     *
     * @param n Size of the set.
     * @param k Size of the subsets to be counted.
     * @return {@code n choose k}.
     * @throws IllegalArgumentException if {@code n < 0}.
     * @throws IllegalArgumentException if {@code k > n}.
     * @throws IllegalArgumentException if the result is too large to be
     * represented by a {@code long}.
     */
    public static double value(int n, int k) {
        BinomialCoefficient.checkBinomial(n, k);

        if (n == k ||
            k == 0) {
            return 1;
        }
        if (k == 1 ||
            k == n - 1) {
            return n;
        }
        if (k > n / 2) {
            return value(n, n - k);
        }
        if (n < 67) {
            return BinomialCoefficient.value(n, k);
        }

        double result = 1;
        for (int i = 1; i <= k; i++) {
            result *= n - k + i;
            result /= i;
        }

        return Math.floor(result + 0.5);
    }
}
