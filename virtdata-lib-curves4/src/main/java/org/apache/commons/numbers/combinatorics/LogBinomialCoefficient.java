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
 * Natural logarithm of the <a href="http://mathworld.wolfram.com/BinomialCoefficient.html">
 * binomial coefficient</a>.
 * It is "{@code n choose k}", the number of {@code k}-element subsets that
 * can be selected from an {@code n}-element set.
 */
public class LogBinomialCoefficient {
    /**
     * Computes the logarithm of the binomial coefficient.
     * The largest value of {@code n} for which all coefficients can
     * fit into a {@code long} is 66.
     *
     * @param n Size of the set.
     * @param k Size of the subsets to be counted.
     * @return {@code log(n choose k)}.
     * @throws IllegalArgumentException if {@code n < 0}.
     * @throws IllegalArgumentException if {@code k > n}.
     * @throws IllegalArgumentException if the result is too large to be
     * represented by a {@code long}.
     */
    public static double value(int n, int k) {
        BinomialCoefficient.checkBinomial(n, k);

        if (n == k ||
            k == 0) {
            return 0;
        }
        if (k == 1 ||
            k == n - 1) {
            return Math.log(n);
        }

        // For values small enough to do exact integer computation,
        // return the log of the exact value.
        if (n < 67) {
            return Math.log(BinomialCoefficient.value(n, k));
        }

        // Logarithm of "BinomialCoefficientDouble" for values that
        // will not overflow.
        if (n < 1030) {
            return Math.log(BinomialCoefficientDouble.value(n, k));
        }

        if (k > n / 2) {
            return value(n, n - k);
        }

        // Sum for values that could overflow.
        double logSum = 0;

        // n! / (n - k)!
        for (int i = n - k + 1; i <= n; i++) {
            logSum += Math.log(i);
        }

        // Divide by k!
        for (int i = 2; i <= k; i++) {
            logSum -= Math.log(i);
        }

        return logSum;
    }
}
