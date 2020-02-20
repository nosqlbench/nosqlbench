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
package org.apache.commons.rng.sampling.distribution;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.LargeMeanPoissonSampler.LargeMeanPoissonSamplerState;

/**
 * Create a sampler for the
 * <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson
 * distribution</a> using a cache to minimise construction cost.
 *
 * <p>The cache will return a sampler equivalent to
 * {@link PoissonSampler#PoissonSampler(UniformRandomProvider, double)}.</p>
 *
 * <p>The cache allows the {@link PoissonSampler} construction cost to be minimised
 * for low size Poisson samples. The cache stores state for a range of integers where
 * integer value {@code n} can be used to construct a sampler for the range
 * {@code n <= mean < n+1}.</p>
 *
 * <p>The cache is advantageous under the following conditions:</p>
 *
 * <ul>
 *   <li>The mean of the Poisson distribution falls within a known range.
 *   <li>The sample size to be made with the <strong>same</strong> sampler is
 *       small.
 *   <li>The Poisson samples have different means with the same integer
 *       value(s) after rounding down.
 * </ul>
 *
 * <p>If the sample size to be made with the <strong>same</strong> sampler is large
 * then the construction cost is low compared to the sampling time and the cache
 * has minimal benefit.</p>
 *
 * <p>Performance improvement is dependent on the speed of the
 * {@link UniformRandomProvider}. A fast provider can obtain a two-fold speed
 * improvement for a single-use Poisson sampler.</p>
 *
 * <p>The cache is thread safe. Note that concurrent threads using the cache
 * must ensure a thread safe {@link UniformRandomProvider} is used when creating
 * samplers, e.g. a unique sampler per thread.</p>
 *
 * <p>Sampling uses:</p>
 *
 * <ul>
 *   <li>{@link UniformRandomProvider#nextDouble()}
 *   <li>{@link UniformRandomProvider#nextLong()} (large means only)
 * </ul>
 *
 * @since 1.2
 */
public class PoissonSamplerCache {

    /**
     * The minimum N covered by the cache where
     * {@code N = (int)Math.floor(mean)}.
     */
    private final int minN;
    /**
     * The maximum N covered by the cache where
     * {@code N = (int)Math.floor(mean)}.
     */
    private final int maxN;
    /** The cache of states between {@link minN} and {@link maxN}. */
    private final LargeMeanPoissonSamplerState[] values;

    /**
     * @param minMean The minimum mean covered by the cache.
     * @param maxMean The maximum mean covered by the cache.
     * @throws IllegalArgumentException if {@code maxMean < minMean}
     */
    public PoissonSamplerCache(double minMean,
                               double maxMean) {

        checkMeanRange(minMean, maxMean);

        // The cache can only be used for the LargeMeanPoissonSampler.
        if (maxMean < PoissonSampler.PIVOT) {
            // The upper limit is too small so no cache will be used.
            // This class will just construct new samplers.
            minN = 0;
            maxN = 0;
            values = null;
        } else {
            // Convert the mean into integers.
            // Note the minimum is clipped to the algorithm switch point.
            this.minN = (int) Math.floor(Math.max(minMean, PoissonSampler.PIVOT));
            this.maxN = (int) Math.floor(Math.min(maxMean, Integer.MAX_VALUE));
            values = new LargeMeanPoissonSamplerState[maxN - minN + 1];
        }
    }

    /**
     * @param minN   The minimum N covered by the cache where {@code N = (int)Math.floor(mean)}.
     * @param maxN   The maximum N covered by the cache where {@code N = (int)Math.floor(mean)}.
     * @param states The precomputed states.
     */
    private PoissonSamplerCache(int minN,
                                int maxN,
                                LargeMeanPoissonSamplerState[] states) {
        this.minN = minN;
        this.maxN = maxN;
        // Stored directly as the states were newly created within this class.
        this.values = states;
    }

    /**
     * Check the mean range.
     *
     * @param minMean The minimum mean covered by the cache.
     * @param maxMean The maximum mean covered by the cache.
     * @throws IllegalArgumentException if {@code maxMean < minMean}
     */
    private static void checkMeanRange(double minMean, double maxMean)
    {
        // Note:
        // Although a mean of 0 is invalid for a Poisson sampler this case
        // is handled to make the cache user friendly. Any low means will
        // be handled by the SmallMeanPoissonSampler and not cached.
        // For this reason it is also OK if the means are negative.

        // Allow minMean == maxMean so that the cache can be used
        // to create samplers with distinct RNGs and the same mean.
        if (maxMean < minMean) {
            throw new IllegalArgumentException(
                    "Max mean: " + maxMean + " < " + minMean);
        }
    }

    /**
     * Creates a new Poisson sampler.
     *
     * <p>The returned sampler will function exactly the
     * same as {@link PoissonSampler#PoissonSampler(UniformRandomProvider, double)}.
     *
     * @param rng  Generator of uniformly distributed random numbers.
     * @param mean Mean.
     * @return A Poisson sampler
     * @throws IllegalArgumentException if {@code mean <= 0} or
     * {@code mean >} {@link Integer#MAX_VALUE}.
     */
    public DiscreteSampler createPoissonSampler(UniformRandomProvider rng,
                                                double mean) {
        // Ensure the same functionality as the PoissonSampler by
        // using a SmallMeanPoissonSampler under the switch point.
        if (mean < PoissonSampler.PIVOT) {
            return new SmallMeanPoissonSampler(rng, mean);
        }
        if (mean > maxN) {
            // Outside the range of the cache.
            // This avoids extra parameter checks and handles the case when
            // the cache is empty or if Math.floor(mean) is not an integer.
            return new LargeMeanPoissonSampler(rng, mean);
        }

        // Convert the mean into an integer.
        final int n = (int) Math.floor(mean);
        if (n < minN) {
            // Outside the lower range of the cache.
            return new LargeMeanPoissonSampler(rng, mean);
        }

        // Look in the cache for a state that can be reused.
        // Note: The cache is offset by minN.
        final int index = n - minN;
        final LargeMeanPoissonSamplerState state = values[index];
        if (state == null) {
            // Create a sampler and store the state for reuse.
            // Do not worry about thread contention
            // as the state is effectively immutable.
            // If recomputed and replaced it will the same.
            final LargeMeanPoissonSampler sampler = new LargeMeanPoissonSampler(rng, mean);
            values[index] = sampler.getState();
            return sampler;
        }
        // Compute the remaining fraction of the mean
        final double lambdaFractional = mean - n;
        return new LargeMeanPoissonSampler(rng, state, lambdaFractional);
    }

    /**
     * Check if the mean is within the range where the cache can minimise the
     * construction cost of the {@link PoissonSampler}.
     *
     * @param mean
     *            the mean
     * @return true, if within the cache range
     */
    public boolean withinRange(double mean) {
        if (mean < PoissonSampler.PIVOT) {
            // Construction is optimal
            return true;
        }
        // Convert the mean into an integer.
        final int n = (int) Math.floor(mean);
        return n <= maxN && n >= minN;
    }

    /**
     * Checks if the cache covers a valid range of mean values.
     *
     * <p>Note that the cache is only valid for one of the Poisson sampling
     * algorithms. In the instance that a range was requested that was too
     * low then there is nothing to cache and this functions returns
     * {@code false}.
     *
     * <p>The cache can still be used to create a {@link PoissonSampler} using
     * {@link #createPoissonSampler(UniformRandomProvider, double)}.
     *
     * <p>This method can be used to determine if the cache has a potential
     * performance benefit.
     *
     * @return true, if the cache covers a range of mean values
     */
    public boolean isValidRange() {
        return values != null;
    }

    /**
     * Gets the minimum mean covered by the cache.
     *
     * <p>This value is the inclusive lower bound and is equal to
     * the lowest integer-valued mean that is covered by the cache.
     *
     * <p>Note that this value may not match the value passed to the constructor
     * due to the following reasons:
     *
     * <ul>
     *   <li>At small mean values a different algorithm is used for Poisson
     *       sampling and the cache is unnecessary.
     *   <li>The minimum is always an integer so may be below the constructor
     *       minimum mean.
     * </ul>
     *
     * <p>If {@link #isValidRange()} returns {@code true} the cache will store
     * state to reduce construction cost of samplers in
     * the range {@link #getMinMean()} inclusive to {@link #getMaxMean()}
     * inclusive. Otherwise this method returns 0;
     *
     * @return The minimum mean covered by the cache.
     */
    public double getMinMean()
    {
        return minN;
    }

    /**
     * Gets the maximum mean covered by the cache.
     *
     * <p>This value is the inclusive upper bound and is equal to
     * the double value below the first integer-valued mean that is
     * above range covered by the cache.
     *
     * <p>Note that this value may not match the value passed to the constructor
     * due to the following reasons:
     * <ul>
     *   <li>At small mean values a different algorithm is used for Poisson
     *       sampling and the cache is unnecessary.
     *   <li>The maximum is always the double value below an integer so
     *       may be above the constructor maximum mean.
     * </ul>
     *
     * <p>If {@link #isValidRange()} returns {@code true} the cache will store
     * state to reduce construction cost of samplers in
     * the range {@link #getMinMean()} inclusive to {@link #getMaxMean()}
     * inclusive. Otherwise this method returns 0;
     *
     * @return The maximum mean covered by the cache.
     */
    public double getMaxMean()
    {
        if (isValidRange()) {
            return Math.nextAfter(maxN + 1.0, -1);
        }
        return 0;
    }

    /**
     * Gets the minimum mean value that can be cached.
     *
     * <p>Any {@link PoissonSampler} created with a mean below this level will not
     * have any state that can be cached.
     *
     * @return the minimum cached mean
     */
    public static double getMinimumCachedMean() {
        return PoissonSampler.PIVOT;
    }

    /**
     * Create a new {@link PoissonSamplerCache} with the given range
     * reusing the current cache values.
     *
     * <p>This will create a new object even if the range is smaller or the
     * same as the current cache.
     *
     * @param minMean The minimum mean covered by the cache.
     * @param maxMean The maximum mean covered by the cache.
     * @throws IllegalArgumentException if {@code maxMean < minMean}
     * @return the poisson sampler cache
     */
    public PoissonSamplerCache withRange(double minMean,
                                         double maxMean) {
        if (values == null) {
            // Nothing to reuse
            return new PoissonSamplerCache(minMean, maxMean);
        }
        checkMeanRange(minMean, maxMean);

        // The cache can only be used for the LargeMeanPoissonSampler.
        if (maxMean < PoissonSampler.PIVOT) {
            return new PoissonSamplerCache(0, 0);
        }

        // Convert the mean into integers.
        // Note the minimum is clipped to the algorithm switch point.
        final int withMinN = (int) Math.floor(Math.max(minMean, PoissonSampler.PIVOT));
        final int withMaxN = (int) Math.floor(maxMean);
        final LargeMeanPoissonSamplerState[] states =
                new LargeMeanPoissonSamplerState[withMaxN - withMinN + 1];

        // Preserve values from the current array to the next
        int currentIndex;
        int nextIndex;
        if (this.minN <= withMinN) {
            // The current array starts before the new array
            currentIndex = withMinN - this.minN;
            nextIndex = 0;
        } else {
            // The new array starts before the current array
            currentIndex = 0;
            nextIndex = this.minN - withMinN;
        }
        final int length = Math.min(values.length - currentIndex, states.length - nextIndex);
        if (length > 0) {
            System.arraycopy(values, currentIndex, states, nextIndex, length);
        }

        return new PoissonSamplerCache(withMinN, withMaxN, states);
    }
}
