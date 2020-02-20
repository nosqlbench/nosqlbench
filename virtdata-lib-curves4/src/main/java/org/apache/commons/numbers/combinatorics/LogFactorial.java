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

import org.apache.commons.numbers.gamma.LogGamma;

/**
 * Class for computing the natural logarithm of the factorial of a number.
 * It allows to allocate a cache of precomputed values.
 * In case of cache miss, computation is performed by a call to
 * {@link LogGamma#value(double)}.
 */
public class LogFactorial {
    /**
     * Size of precomputed factorials.
     * @see Factorial
     */
    private static final int FACTORIALS_CACHE_SIZE = 21;
    /**
     * Precomputed values of the function: {@code logFactorials[i] = Math.log(i!)}.
     */
    private final double[] logFactorials;

    /**
     * Creates an instance, reusing the already computed values if available.
     *
     * @param numValues Number of values of the function to compute.
     * @param cache Cached values.
     * @throw IllegalArgumentException if {@code n < 0}.
     */
    private LogFactorial(int numValues,
                         double[] cache) {
        if (numValues < 0) {
            throw new CombinatoricsException(CombinatoricsException.NEGATIVE, numValues);
        }

        logFactorials = new double[numValues];

        final int beginCopy = 2;
        final int endCopy = cache == null || cache.length <= beginCopy ?
            beginCopy : cache.length <= numValues ?
            cache.length : numValues;

        // Copy available values.
        for (int i = beginCopy; i < endCopy; i++) {
            logFactorials[i] = cache[i];
        }

        // Precompute.
        for (int i = endCopy; i < numValues; i++) {
            logFactorials[i] = logFactorials[i - 1] + Math.log(i);
        }
    }

    /**
     * Creates an instance with no precomputed values.
     * @return instance with no precomputed values
     */
    public static LogFactorial create() {
        return new LogFactorial(0, null);
    }

    /**
     * Creates an instance with the specified cache size.
     *
     * @param cacheSize Number of precomputed values of the function.
     * @return a new instance where {@code cacheSize} values have been
     * precomputed.
     * @throws IllegalArgumentException if {@code cacheSize < 0}.
     */
    public LogFactorial withCache(final int cacheSize) {
        return new LogFactorial(cacheSize, logFactorials);
    }

    /**
     * Computes \( log_e(n!) \).
     *
     * @param n Argument.
     * @return {@code log(n!)}.
     * @throws IllegalArgumentException if {@code n < 0}.
     */
    public double value(int n) {
        if (n < 0) {
            throw new CombinatoricsException(CombinatoricsException.NEGATIVE, n);
        }

        // Use cache of precomputed values.
        if (n < logFactorials.length) {
            return logFactorials[n];
        }

        // Use cache of precomputed factorial values.
        if (n < FACTORIALS_CACHE_SIZE) {
            return Math.log(Factorial.value(n));
        }

        // Delegate.
        return LogGamma.value(n + 1);
    }
}
