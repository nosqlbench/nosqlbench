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

package org.apache.commons.rng.sampling;

import org.apache.commons.rng.UniformRandomProvider;

/**
 * Utility class for selecting a subset of a sequence of integers.
 */
final class SubsetSamplerUtils {

    /** No public construction. */
    private SubsetSamplerUtils() {}

    /**
     * Checks the subset of length {@code k} from {@code n} is valid.
     *
     * <p>If {@code n <= 0} or {@code k <= 0} or {@code k > n} then no subset
     * is required and an exception is raised.</p>
     *
     * @param n   Size of the set.
     * @param k   Size of the subset.
     * @throws IllegalArgumentException if {@code n <= 0} or {@code k <= 0} or
     *                                  {@code k > n}.
     */
    static void checkSubset(int n,
                            int k) {
        if (n <= 0) {
            throw new IllegalArgumentException("n <= 0 : n=" + n);
        }
        if (k <= 0) {
            throw new IllegalArgumentException("k <= 0 : k=" + k);
        }
        if (k > n) {
            throw new IllegalArgumentException("k > n : k=" + k + ", n=" + n);
        }
    }

    /**
     * Perform a partial Fisher-Yates shuffle of the domain in-place and return
     * either the upper fully shuffled section or the remaining lower partially
     * shuffled section.
     *
     * <p>The returned combination will have a length of {@code steps} for
     * {@code upper=true}, or {@code domain.length - steps} otherwise.</p>
     *
     * <p>Sampling uses {@link UniformRandomProvider#nextInt(int)}.</p>
     *
     * @param domain The domain.
     * @param steps  The number of shuffle steps.
     * @param rng    Generator of uniformly distributed random numbers.
     * @param upper  Set to true to return the upper fully shuffled section.
     * @return a random combination.
     */
    static int[] partialSample(int[] domain,
                               int steps,
                               UniformRandomProvider rng,
                               boolean upper) {
        // Shuffle from the end but limit to a number of steps.
        for (int i = domain.length - 1, j = 0; i > 0 && j < steps; i--, j++) {
            // Swap index i with any position down to 0 (including itself)
            swap(domain, i, rng.nextInt(i + 1));
        }
        final int size = upper ? steps : domain.length - steps;
        final int from = upper ? domain.length - steps : 0;
        final int[] result = new int[size];
        System.arraycopy(domain, from, result, 0, size);
        return result;
    }

    /**
     * Swaps the two specified elements in the specified array.
     *
     * @param array the array
     * @param i     the first index
     * @param j     the second index
     */
    static void swap(int[] array, int i, int j) {
        final int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
    }
}
