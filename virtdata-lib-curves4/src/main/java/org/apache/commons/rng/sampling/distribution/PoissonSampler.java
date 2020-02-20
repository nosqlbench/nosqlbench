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
 *  <li>
 *   For large means, we use the rejection algorithm described in
 *   <blockquote>
 *    Devroye, Luc. (1981). <i>The Computer Generation of Poisson Random Variables</i><br>
 *    <strong>Computing</strong> vol. 26 pp. 197-207.
 *   </blockquote>
 *  </li>
 * </ul>
 *
 * <p>Sampling uses:</p>
 *
 * <ul>
 *   <li>{@link UniformRandomProvider#nextDouble()}
 *   <li>{@link UniformRandomProvider#nextLong()} (large means only)
 * </ul>
 *
 * @since 1.0
 */
public class PoissonSampler
    extends SamplerBase
    implements DiscreteSampler {

    /**
     * Value for switching sampling algorithm.
     *
     * <p>Package scope for the {@link PoissonSamplerCache}.
     */
    static final double PIVOT = 40;
    /** The internal Poisson sampler. */
    private final DiscreteSampler poissonSamplerDelegate;

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param mean Mean.
     * @throws IllegalArgumentException if {@code mean <= 0} or
     * {@code mean >} {@link Integer#MAX_VALUE}.
     */
    public PoissonSampler(UniformRandomProvider rng,
                          double mean) {
        super(null);

        // Delegate all work to specialised samplers.
        // These should check the input arguments.
        poissonSamplerDelegate = mean < PIVOT ?
            new SmallMeanPoissonSampler(rng, mean) :
            new LargeMeanPoissonSampler(rng, mean);
    }

    /** {@inheritDoc} */
    @Override
    public int sample() {
        return poissonSamplerDelegate.sample();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return poissonSamplerDelegate.toString();
    }
}
