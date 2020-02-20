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

/**
 * Interface for distributions on the integers.
 */
public interface DiscreteDistribution {

    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code log(P(X = x))}, where
     * {@code log} is the natural logarithm.
     *
     * @param x Point at which the PMF is evaluated.
     * @return the logarithm of the value of the probability mass function at
     * {@code x}.
     */
    default double logProbability(int x) {
        return Math.log(probability(x));
    }

    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(X = x)}.
     * In other words, this method represents the probability mass function (PMF)
     * for the distribution.
     *
     * @param x Point at which the PMF is evaluated.
     * @return the value of the probability mass function at {@code x}.
     */
    double probability(int x);

    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(x0 < X <= x1)}.
     *
     * @param x0 Lower bound (exclusive).
     * @param x1 Upper bound (inclusive).
     * @return the probability that a random variable with this distribution
     * will take a value between {@code x0} and {@code x1}, excluding the lower
     * and including the upper endpoint.
     * @throws IllegalArgumentException if {@code x0 > x1}.
     */
    double probability(int x0, int x1);

    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(X <= x)}.
     * In other, words, this method represents the (cumulative) distribution
     * function (CDF) for this distribution.
     *
     * @param x Point at which the CDF is evaluated.
     * @return the probability that a random variable with this distribution
     * takes a value less than or equal to {@code x}.
     */
    double cumulativeProbability(int x);

    /**
     * Computes the quantile function of this distribution.
     * For a random variable {@code X} distributed according to this distribution,
     * the returned value is
     * <ul>
     * <li>{@code inf{x in Z | P(X<=x) >= p}} for {@code 0 < p <= 1},</li>
     * <li>{@code inf{x in Z | P(X<=x) > 0}} for {@code p = 0}.</li>
     * </ul>
     * If the result exceeds the range of the data type {@code int},
     * then {@code Integer.MIN_VALUE} or {@code Integer.MAX_VALUE} is returned.
     *
     * @param p Cumulative probability.
     * @return the smallest {@code p}-quantile of this distribution
     * (largest 0-quantile for {@code p = 0}).
     * @throws IllegalArgumentException if {@code p < 0} or {@code p > 1}.
     */
    int inverseCumulativeProbability(double p);

    /**
     * Gets the mean of this distribution.
     *
     * @return the mean, or {@code Double.NaN} if it is not defined.
     */
    double getMean();

    /**
     * Gets the variance of this distribution.
     *
     * @return the variance, or {@code Double.NaN} if it is not defined.
     */
    double getVariance();

    /**
     * Gets the lower bound of the support.
     * This method must return the same value as
     * {@code inverseCumulativeProbability(0)}, i.e.
     * {@code inf {x in Z | P(X <= x) > 0}}.
     * By convention, {@code Integer.MIN_VALUE} should be substituted
     * for negative infinity.
     *
     * @return the lower bound of the support.
     */
    int getSupportLowerBound();

    /**
     * Gets the upper bound of the support.
     * This method must return the same value as
     * {@code inverseCumulativeProbability(1)}, i.e.
     * {@code inf {x in R | P(X <= x) = 1}}.
     * By convention, {@code Integer.MAX_VALUE} should be substituted
     * for positive infinity.
     *
     * @return the upper bound of the support.
     */
    int getSupportUpperBound();

    /**
     * Indicates whether the support is connected, i.e. whether all
     * integers between the lower and upper bound of the support are
     * included in the support.
     *
     * @return whether the support is connected.
     */
    boolean isSupportConnected();

    /**
     * Creates a sampler.
     *
     * @param rng Generator of uniformly distributed numbers.
     * @return a sampler that produces random numbers according this
     * distribution.
     */
    Sampler createSampler(UniformRandomProvider rng);

    /**
     * Sampling functionality.
     */
    interface Sampler {
        /**
         * Generates a random value sampled from this distribution.
         *
         * @return a random value.
         */
        int sample();
    }
}
