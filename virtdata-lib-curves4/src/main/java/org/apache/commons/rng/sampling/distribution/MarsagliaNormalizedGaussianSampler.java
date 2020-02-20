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
 * <a href="https://en.wikipedia.org/wiki/Marsaglia_polar_method">
 * Marsaglia polar method</a> for sampling from a Gaussian distribution
 * with mean 0 and standard deviation 1.
 * This is a variation of the algorithm implemented in
 * {@link BoxMullerNormalizedGaussianSampler}.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.1
 */
public class MarsagliaNormalizedGaussianSampler
    implements NormalizedGaussianSampler {
    /** Next gaussian. */
    private double nextGaussian = Double.NaN;
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    /**
     * @param rng Generator of uniformly distributed random numbers.
     */
    public MarsagliaNormalizedGaussianSampler(UniformRandomProvider rng) {
        this.rng = rng;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        if (Double.isNaN(nextGaussian)) {
            // Rejection scheme for selecting a pair that lies within the unit circle.
            while (true) {
                // Generate a pair of numbers within [-1 , 1).
                final double x = 2 * rng.nextDouble() - 1;
                final double y = 2 * rng.nextDouble() - 1;
                final double r2 = x * x + y * y;

                if (r2 < 1 && r2 > 0) {
                    // Pair (x, y) is within unit circle.
                    final double alpha = Math.sqrt(-2 * Math.log(r2) / r2);

                    // Keep second element of the pair for next invocation.
                    nextGaussian = alpha * y;

                    // Return the first element of the generated pair.
                    return alpha * x;
                }

                // Pair is not within the unit circle: Generate another one.
            }
        } else {
            // Use the second element of the pair (generated at the
            // previous invocation).
            final double r = nextGaussian;

            // Both elements of the pair have been used.
            nextGaussian = Double.NaN;

            return r;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Box-Muller (with rejection) normalized Gaussian deviate [" + rng.toString() + "]";
    }
}
