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
 * <a href="https://en.wikipedia.org/wiki/Box%E2%80%93Muller_transform">
 * Box-Muller algorithm</a> for sampling from a Gaussian distribution.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.0
 *
 * @deprecated Since version 1.1. Please use {@link BoxMullerNormalizedGaussianSampler}
 * and {@link GaussianSampler} instead.
 */
@Deprecated
public class BoxMullerGaussianSampler
    extends SamplerBase
    implements ContinuousSampler {
    /** Next gaussian. */
    private double nextGaussian = Double.NaN;
    /** Mean. */
    private final double mean;
    /** standardDeviation. */
    private final double standardDeviation;
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param mean Mean of the Gaussian distribution.
     * @param standardDeviation Standard deviation of the Gaussian distribution.
     * @throws IllegalArgumentException if {@code standardDeviation <= 0}
     */
    public BoxMullerGaussianSampler(UniformRandomProvider rng,
                                    double mean,
                                    double standardDeviation) {
        super(null);
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException("standard deviation is not strictly positive: " +
                standardDeviation);
        }
        this.rng = rng;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        final double random;
        if (Double.isNaN(nextGaussian)) {
            // Generate a pair of Gaussian numbers.

            final double x = rng.nextDouble();
            final double y = rng.nextDouble();
            final double alpha = 2 * Math.PI * x;
            final double r = Math.sqrt(-2 * Math.log(y));

            // Return the first element of the generated pair.
            random = r * Math.cos(alpha);

            // Keep second element of the pair for next invocation.
            nextGaussian = r * Math.sin(alpha);
        } else {
            // Use the second element of the pair (generated at the
            // previous invocation).
            random = nextGaussian;

            // Both elements of the pair have been used.
            nextGaussian = Double.NaN;
        }

        return standardDeviation * random + mean;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Box-Muller Gaussian deviate [" + rng.toString() + "]";
    }
}
