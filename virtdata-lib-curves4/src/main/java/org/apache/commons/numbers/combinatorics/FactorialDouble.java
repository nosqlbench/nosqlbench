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
 * Class for computing the natural logarithm of the
 * <a href="http://mathworld.wolfram.com/Factorial.html">factorial of a number</a>.
 * It allows to allocate a cache of precomputed values.
 */
public class FactorialDouble {
    /**
     * Size of precomputed factorials.
     * @see Factorial
     */
    private static final int FACTORIALS_LONG_CACHE_SIZE = 21;
    /**
     * Precomputed values of the function: {@code factorialsDouble[i] = i!}.
     */
    private final double[] factorialsDouble;

    /**
     * Creates an instance, reusing the already computed values if available.
     *
     * @param numValues Number of values of the function to compute.
     * @param cache Cached values.
     * @throw IllegalArgumentException if {@code n < 0}.
     */
    private FactorialDouble(int numValues,
                            double[] cache) {
        if (numValues < 0) {
            throw new CombinatoricsException(CombinatoricsException.NEGATIVE, numValues);
        }

        factorialsDouble = new double[numValues];
        // Initialize first two entries.
        for (int i = 0, max = numValues < 2 ? numValues : 2;
             i < max; i++) {
            factorialsDouble [i] = 1;
        }

        final int beginCopy = 2;
        final int endCopy = cache == null || cache.length <= beginCopy ?
            beginCopy : cache.length <= numValues ?
            cache.length : numValues;

        // Copy available values.
        for (int i = beginCopy; i < endCopy; i++) {
            factorialsDouble[i] = cache[i];
        }

        // Precompute.
        for (int i = endCopy; i < numValues; i++) {
            factorialsDouble[i] = i * factorialsDouble[i - 1];
        }
    }

    /**
     * Creates an instance with no precomputed values.
     * @return instance with no precomputed values
     */
    public static FactorialDouble create() {
        return new FactorialDouble(0, null);
    }

    /**
     * Creates an instance with the specified cache size.
     *
     * @param cacheSize Number of precomputed values of the function.
     * @return a new instance where {@code cacheSize} values have been
     * precomputed.
     * @throws IllegalArgumentException if {@code cacheSize < 0}.
     */
    public FactorialDouble withCache(final int cacheSize) {
        return new FactorialDouble(cacheSize,
                                   factorialsDouble);
    }

    /**
     * Computes the factorial of {@code n}.
     * The result should be small enough to fit into a {@code double}: The
     * largest {@code n} for which {@code n!} does not exceed
     * {@code Double.MAX_VALUE} is 170. {@code Double.POSITIVE_INFINITY} is
     * returned for {@code n > 170}.
     *
     * @param n Argument.
     * @return {@code n!}
     * @throws IllegalArgumentException if {@code n < 0}.
     */
    public double value(int n) {
        if (n < FACTORIALS_LONG_CACHE_SIZE) {
            return Factorial.value(n);
        }

        if (n < factorialsDouble.length) {
            // Use cache of precomputed values.
            return factorialsDouble[n];
        }

        return compute(n);
    }

    /**
     * @param n Argument.
     * @return {@code n!} (approximated as a {@code double}).
     */
    private double compute(int n) {
        int start = 2;
        double result = 1;

        if (factorialsDouble.length > 2) {
            result = factorialsDouble[factorialsDouble.length - 1];
            start = factorialsDouble.length;
        }
        for (int i = start; i <= n; i++) {
            result *= i;
        }
        return result;
     }
}
