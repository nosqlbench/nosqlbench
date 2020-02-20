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
 * Sampling from a <a href="https://en.wikipedia.org/wiki/Log-normal_distribution">
 * log-normal distribution</a>.
 * Uses {@link BoxMullerNormalizedGaussianSampler} as the underlying sampler.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.0
 *
 * @deprecated Since version 1.1. Please use {@link LogNormalSampler} instead.
 */
@Deprecated
public class BoxMullerLogNormalSampler
    extends SamplerBase
    implements ContinuousSampler {
    /** Delegate. */
    private final ContinuousSampler sampler;

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param scale Scale of the log-normal distribution.
     * @param shape Shape of the log-normal distribution.
     * @throws IllegalArgumentException if {@code scale < 0} or {@code shape <= 0}.
     */
    public BoxMullerLogNormalSampler(UniformRandomProvider rng,
                                     double scale,
                                     double shape) {
        super(null);
        sampler = new LogNormalSampler(new BoxMullerNormalizedGaussianSampler(rng),
                                       scale, shape);
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        return sampler.sample();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return sampler.toString();
    }
}
