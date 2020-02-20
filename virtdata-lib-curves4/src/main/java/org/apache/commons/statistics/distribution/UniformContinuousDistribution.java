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

package org.apache.commons.statistics.distribution;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousUniformSampler;

/**
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Uniform_distribution_(continuous)">uniform distribution</a>.
 */
public class UniformContinuousDistribution extends AbstractContinuousDistribution {
    /** Lower bound of this distribution (inclusive). */
    private final double lower;
    /** Upper bound of this distribution (exclusive). */
    private final double upper;

    /**
     * Creates a uniform distribution.
     *
     * @param lower Lower bound of this distribution (inclusive).
     * @param upper Upper bound of this distribution (exclusive).
     * @throws IllegalArgumentException if {@code lower >= upper}.
     */
    public UniformContinuousDistribution(double lower,
                                         double upper) {
        if (lower >= upper) {
            throw new DistributionException(DistributionException.TOO_LARGE,
                                            lower, upper);
        }

        this.lower = lower;
        this.upper = upper;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        if (x < lower ||
            x > upper) {
            return 0;
        }
        return 1 / (upper - lower);
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x)  {
        if (x <= lower) {
            return 0;
        }
        if (x >= upper) {
            return 1;
        }
        return (x - lower) / (upper - lower);
    }

    /** {@inheritDoc} */
    @Override
    public double inverseCumulativeProbability(final double p) {
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        }
        return p * (upper - lower) + lower;
    }

    /**
     * {@inheritDoc}
     *
     * For lower bound {@code lower} and upper bound {@code upper}, the mean is
     * {@code 0.5 * (lower + upper)}.
     */
    @Override
    public double getMean() {
        return 0.5 * (lower + upper);
    }

    /**
     * {@inheritDoc}
     *
     * For lower bound {@code lower} and upper bound {@code upper}, the
     * variance is {@code (upper - lower)^2 / 12}.
     */
    @Override
    public double getVariance() {
        double ul = upper - lower;
        return ul * ul / 12;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is equal to the lower bound parameter
     * of the distribution.
     *
     * @return lower bound of the support
     */
    @Override
    public double getSupportLowerBound() {
        return lower;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is equal to the upper bound parameter
     * of the distribution.
     *
     * @return upper bound of the support
     */
    @Override
    public double getSupportUpperBound() {
        return upper;
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    @Override
    public boolean isSupportConnected() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public ContinuousDistribution.Sampler createSampler(final UniformRandomProvider rng) {
        return new ContinuousDistribution.Sampler() {
            /**
             * Uniform distribution sampler.
             */
            private final ContinuousSampler sampler =
                new ContinuousUniformSampler(rng, lower, upper);

            /**{@inheritDoc} */
            @Override
            public double sample() {
                return sampler.sample();
            }
        };
    }
}
