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
import org.apache.commons.rng.sampling.distribution.AhrensDieterExponentialSampler;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;

/**
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Exponential_distribution">exponential distribution</a>.
 */
public class ExponentialDistribution extends AbstractContinuousDistribution {
    /** The mean of this distribution. */
    private final double mean;
    /** The logarithm of the mean, stored to reduce computing time. */
    private final double logMean;

    /**
     * Creates a distribution.
     *
     * @param mean Mean of this distribution.
     * @throws IllegalArgumentException if {@code mean <= 0}.
     */
    public ExponentialDistribution(double mean) {
        if (mean <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE, mean);
        }
        this.mean = mean;
        logMean = Math.log(mean);
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        final double logDensity = logDensity(x);
        return logDensity == Double.NEGATIVE_INFINITY ? 0 : Math.exp(logDensity);
    }

    /** {@inheritDoc} **/
    @Override
    public double logDensity(double x) {
        if (x < 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return -x / mean - logMean;
    }

    /**
     * {@inheritDoc}
     *
     * The implementation of this method is based on:
     * <ul>
     * <li>
     * <a href="http://mathworld.wolfram.com/ExponentialDistribution.html">
     * Exponential Distribution</a>, equation (1).</li>
     * </ul>
     */
    @Override
    public double cumulativeProbability(double x)  {
        double ret;
        if (x <= 0) {
            ret = 0;
        } else {
            ret = 1 - Math.exp(-x / mean);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Returns {@code 0} when {@code p= = 0} and
     * {@code Double.POSITIVE_INFINITY} when {@code p == 1}.
     */
    @Override
    public double inverseCumulativeProbability(double p) {
        double ret;

        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        } else if (p == 1) {
            ret = Double.POSITIVE_INFINITY;
        } else {
            ret = -mean * Math.log(1 - p);
        }

        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public double getMean() {
        return mean;
    }

    /**
     * {@inheritDoc}
     *
     * For mean parameter {@code k}, the variance is {@code k^2}.
     */
    @Override
    public double getVariance() {
        return mean * mean;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the mean parameter.
     *
     * @return lower bound of the support (always 0)
     */
    @Override
    public double getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity
     * no matter the mean parameter.
     *
     * @return upper bound of the support (always Double.POSITIVE_INFINITY)
     */
    @Override
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
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

    /**
     * {@inheritDoc}
     *
     * <p>Sampling algorithm uses the
     *  <a href="http://www.jesus.ox.ac.uk/~clifford/a5/chap1/node5.html">
     *   inversion method</a> to generate exponentially distributed
     *  random values from uniform deviates.
     * </p>
     */
    @Override
    public ContinuousDistribution.Sampler createSampler(final UniformRandomProvider rng) {
        return new ContinuousDistribution.Sampler() {
            /**
             * Exponential distribution sampler.
             */
            private final ContinuousSampler sampler =
                new AhrensDieterExponentialSampler(rng, mean);

            /**{@inheritDoc} */
            @Override
            public double sample() {
                return sampler.sample();
            }
        };
    }
}
