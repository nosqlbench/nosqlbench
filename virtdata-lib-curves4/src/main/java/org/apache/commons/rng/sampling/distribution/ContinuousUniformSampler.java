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
 * Sampling from a uniform distribution.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextDouble()}.</p>
 *
 * @since 1.0
 */
public class ContinuousUniformSampler
    extends SamplerBase
    implements ContinuousSampler {
    /** Lower bound. */
    private final double lo;
    /** Higher bound. */
    private final double hi;
    /** Underlying source of randomness. */
    private final UniformRandomProvider rng;

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param lo Lower bound.
     * @param hi Higher bound.
     */
    public ContinuousUniformSampler(UniformRandomProvider rng,
                                    double lo,
                                    double hi) {
        super(null);
        this.rng = rng;
        this.lo = lo;
        this.hi = hi;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        final double u = rng.nextDouble();
        return u * hi + (1 - u) * lo;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Uniform deviate [" + rng.toString() + "]";
    }
}
