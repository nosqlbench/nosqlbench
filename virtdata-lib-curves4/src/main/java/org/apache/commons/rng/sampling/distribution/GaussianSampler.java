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

/**
 * Sampling from a Gaussian distribution with given mean and
 * standard deviation.
 *
 * @since 1.1
 */
public class GaussianSampler implements ContinuousSampler {
    /** Mean. */
    private final double mean;
    /** standardDeviation. */
    private final double standardDeviation;
    /** Normalized Gaussian sampler. */
    private final NormalizedGaussianSampler normalized;

    /**
     * @param normalized Generator of N(0,1) Gaussian distributed random numbers.
     * @param mean Mean of the Gaussian distribution.
     * @param standardDeviation Standard deviation of the Gaussian distribution.
     * @throws IllegalArgumentException if {@code standardDeviation <= 0}
     */
    public GaussianSampler(NormalizedGaussianSampler normalized,
                           double mean,
                           double standardDeviation) {
        if (standardDeviation <= 0) {
            throw new IllegalArgumentException(
                "standard deviation is not strictly positive: " + standardDeviation);
        }
        this.normalized = normalized;
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        return standardDeviation * normalized.sample() + mean;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Gaussian deviate [" + normalized.toString() + "]";
    }
}
