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

import org.apache.commons.numbers.gamma.RegularizedGamma;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.DiscreteSampler;
import org.apache.commons.rng.sampling.distribution.PoissonSampler;

/**
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Poisson_distribution">Poisson distribution</a>.
 */
public class PoissonDistribution extends AbstractDiscreteDistribution {
    /** ln(2 &pi;). */
    private static final double LOG_TWO_PI = Math.log(2 * Math.PI);
    /** Default maximum number of iterations. */
    private static final int DEFAULT_MAX_ITERATIONS = 10000000;
    /** Default convergence criterion. */
    private static final double DEFAULT_EPSILON = 1e-12;
    /** Distribution used to compute normal approximation. */
    private final NormalDistribution normal;
    /** Mean of the distribution. */
    private final double mean;
    /** Maximum number of iterations for cumulative probability. */
    private final int maxIterations;
    /** Convergence criterion for cumulative probability. */
    private final double epsilon;

    /**
     * Creates a new Poisson distribution with specified mean.
     *
     * @param p the Poisson mean
     * @throws IllegalArgumentException if {@code p <= 0}.
     */
    public PoissonDistribution(double p) {
        this(p, DEFAULT_EPSILON, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Creates a new Poisson distribution with specified mean, convergence
     * criterion and maximum number of iterations.
     *
     * @param p Poisson mean.
     * @param epsilon Convergence criterion for cumulative probabilities.
     * @param maxIterations Maximum number of iterations for cumulative
     * probabilities.
     * @throws IllegalArgumentException if {@code p <= 0}.
     */
    private PoissonDistribution(double p,
                                double epsilon,
                                int maxIterations) {
        if (p <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE, p);
        }
        mean = p;
        this.epsilon = epsilon;
        this.maxIterations = maxIterations;

        normal = new NormalDistribution(p, Math.sqrt(p));
    }

    /** {@inheritDoc} */
    @Override
    public double probability(int x) {
        final double logProbability = logProbability(x);
        return logProbability == Double.NEGATIVE_INFINITY ? 0 : Math.exp(logProbability);
    }

    /** {@inheritDoc} */
    @Override
    public double logProbability(int x) {
        double ret;
        if (x < 0 || x == Integer.MAX_VALUE) {
            ret = Double.NEGATIVE_INFINITY;
        } else if (x == 0) {
            ret = -mean;
        } else {
            ret = -SaddlePointExpansion.getStirlingError(x) -
                  SaddlePointExpansion.getDeviancePart(x, mean) -
                  0.5 * LOG_TWO_PI - 0.5 * Math.log(x);
        }
        return ret;
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(int x) {
        if (x < 0) {
            return 0;
        }
        if (x == Integer.MAX_VALUE) {
            return 1;
        }
        return RegularizedGamma.Q.value((double) x + 1, mean, epsilon,
                                        maxIterations);
    }

    /**
     * Calculates the Poisson distribution function using a normal
     * approximation. The {@code N(mean, sqrt(mean))} distribution is used
     * to approximate the Poisson distribution. The computation uses
     * "half-correction" (evaluating the normal distribution function at
     * {@code x + 0.5}).
     *
     * @param x Upper bound, inclusive.
     * @return the distribution function value calculated using a normal
     * approximation.
     */
    public double normalApproximateProbability(int x)  {
        // Calculate the probability using half-correction.
        return normal.cumulativeProbability(x + 0.5);
    }

    /** {@inheritDoc} */
    @Override
    public double getMean() {
        return mean;
    }

    /**
     * {@inheritDoc}
     *
     * For mean parameter {@code p}, the variance is {@code p}.
     */
    @Override
    public double getVariance() {
        return mean;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the mean parameter.
     *
     * @return lower bound of the support (always 0)
     */
    @Override
    public int getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is positive infinity,
     * regardless of the parameter values. There is no integer infinity,
     * so this method returns {@code Integer.MAX_VALUE}.
     *
     * @return upper bound of the support (always {@code Integer.MAX_VALUE} for
     * positive infinity)
     */
    @Override
    public int getSupportUpperBound() {
        return Integer.MAX_VALUE;
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

    /**{@inheritDoc} */
    @Override
    public DiscreteDistribution.Sampler createSampler(final UniformRandomProvider rng) {
        return new DiscreteDistribution.Sampler() {
            /**
             * Poisson distribution sampler.
             */
            private final DiscreteSampler sampler = new PoissonSampler(rng, mean);

            /**{@inheritDoc} */
            @Override
            public int sample() {
                return sampler.sample();
            }
        };
    }
}
