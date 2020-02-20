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

import org.apache.commons.numbers.core.ArithmeticUtils;

/**
 * Representation of the <a href="http://mathworld.wolfram.com/BinomialCoefficient.html">
 * binomial coefficient</a>.
 * It is "{@code n choose k}", the number of {@code k}-element subsets that
 * can be selected from an {@code n}-element set.
 */
public class BinomialCoefficient {
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
     * @throws ArithmeticException if the result is too large to be
     * represented by a {@code long}.
     */
    public static long value(int n, int k) {
        checkBinomial(n, k);

        if (n == k ||
            k == 0) {
            return 1;
        }
        if (k == 1 ||
            k == n - 1) {
            return n;
        }
        // Use symmetry for large k.
        if (k > n / 2) {
            return value(n, n - k);
        }

        // We use the formulae:
        // (n choose k) = n! / (n-k)! / k!
        // (n choose k) = ((n-k+1)*...*n) / (1*...*k)
        // which can be written
        // (n choose k) = (n-1 choose k-1) * n / k
        long result = 1;
        if (n <= 61) {
            // For n <= 61, the naive implementation cannot overflow.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                result = result * i / j;
                i++;
            }
        } else if (n <= 66) {
            // For n > 61 but n <= 66, the result cannot overflow,
            // but we must take care not to overflow intermediate values.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                // We know that (result * i) is divisible by j,
                // but (result * i) may overflow, so we split j:
                // Filter out the gcd, d, so j/d and i/d are integer.
                // result is divisible by (j/d) because (j/d)
                // is relative prime to (i/d) and is a divisor of
                // result * (i/d).
                final long d = ArithmeticUtils.gcd(i, j);
                result = (result / (j / d)) * (i / d);
                ++i;
            }
        } else {
            // For n > 66, a result overflow might occur, so we check
            // the multiplication, taking care to not overflow
            // unnecessary.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                final long d = ArithmeticUtils.gcd(i, j);
                result = ArithmeticUtils.mulAndCheck(result / (j / d), i / d);
                ++i;
            }
        }

        return result;
    }

    /**
     * Check binomial preconditions.
     *
     * @param n Size of the set.
     * @param k Size of the subsets to be counted.
     * @throws IllegalArgumentException if {@code n < 0}.
     * @throws IllegalArgumentException if {@code k > n} or {@code k < 0}.
     */
    static void checkBinomial(int n,
                              int k) {
        if (n < 0) {
            throw new CombinatoricsException(CombinatoricsException.NEGATIVE, n);
        }
        if (k > n ||
            k < 0) {
            throw new CombinatoricsException(CombinatoricsException.OUT_OF_RANGE, k, 0, n);
        }
    }
}
