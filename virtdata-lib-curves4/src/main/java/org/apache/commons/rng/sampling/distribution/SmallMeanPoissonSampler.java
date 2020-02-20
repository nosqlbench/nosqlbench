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

/**
 * Sampler for the <a href="http://mathworld.wolfram.com/PoissonDistribution.html">Poisson distribution</a>.
 *
 * <ul>
 *  <li>
 *   For small means, a Poisson process is simulated using uniform deviates, as
 *   described <a href="http://mathaa.epfl.ch/cours/PMMI2001/interactive/rng7.htm">here</a>.
 *   The Poisson process (and hence, the returned value) is bounded by 1000 * mean.
 *  </li>
 * </ul>
 *
 * <p>This sampler is suitable for {@code mean < 40}.
 * For large means, {@link LargeMeanPoissonSampler} should be used instead.</p>
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.1
 */
public class SmallMeanPoissonSampler
    implements DiscreteSampler {
    /**
     * Pre-compute {@code Math.exp(-mean)}.
     * Note: This is the probability of the Poisson sample {@code P(n=0)}.
     */
    private final double p0;
    /** Pre-compute {@code 1000 * mean} as the upper limit of the sample. */
    private final int limit;
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    /**
     * @param rng  Generator of uniformly distributed random numbers.
     * @param mean Mean.
     * @throws IllegalArgumentException if {@code mean <= 0} or {@code Math.exp(-mean)} is not positive.
     */
    public SmallMeanPoissonSampler(UniformRandomProvider rng,
                                   double mean) {
        this.rng = rng;
        if (mean <= 0) {
            throw new IllegalArgumentException("mean is not strictly positive: " + mean);
        }
        p0 = Math.exp(-mean);
        if (p0 > 0) {
            // The returned sample is bounded by 1000 * mean
            limit = (int) Math.ceil(1000 * mean);
        } else {
            // This excludes NaN values for the mean
            throw new IllegalArgumentException("No p(x=0) probability for mean: " + mean);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int sample() {
        int n = 0;
        double r = 1;

        while (n < limit) {
            r *= rng.nextDouble();
            if (r >= p0) {
                n++;
            } else {
                break;
            }
        }
        return n;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Small Mean Poisson deviate [" + rng.toString() + "]";
    }
}
