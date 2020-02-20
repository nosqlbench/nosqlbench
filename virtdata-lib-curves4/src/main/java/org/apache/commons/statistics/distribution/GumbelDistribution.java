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

/**
 * This class implements the <a href="http://en.wikipedia.org/wiki/Gumbel_distribution">Gumbel distribution</a>.
 */
public class GumbelDistribution extends AbstractContinuousDistribution {
    /** &pi;<sup>2</sup>/6. */
    private static final double PI_SQUARED_OVER_SIX = Math.PI * Math.PI / 6;
    /**
     * <a href="http://mathworld.wolfram.com/Euler-MascheroniConstantApproximations.html">
     * Approximation of Euler's constant</a>.
     */
    private static final double EULER = Math.PI / (2 * Math.E);
    /** Location parameter. */
    private final double mu;
    /** Scale parameter. */
    private final double beta;

    /**
     * Creates a distribution.
     *
     * @param mu location parameter
     * @param beta scale parameter (must be positive)
     * @throws IllegalArgumentException if {@code beta <= 0}
     */
    public GumbelDistribution(double mu,
                              double beta) {
        if (beta <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE, beta);
        }

        this.beta = beta;
        this.mu = mu;
    }

    /**
     * Gets the location parameter.
     *
     * @return the location parameter.
     */
    public double getLocation() {
        return mu;
    }

    /**
     * Gets the scale parameter.
     *
     * @return the scale parameter.
     */
    public double getScale() {
        return beta;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        final double z = (x - mu) / beta;
        final double t = Math.exp(-z);
        return Math.exp(-z - t) / beta;
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x) {
        final double z = (x - mu) / beta;
        return Math.exp(-Math.exp(-z));
    }

    /** {@inheritDoc} */
    @Override
    public double inverseCumulativeProbability(double p) {
        if (p < 0 || p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        } else if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        } else if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return mu - Math.log(-Math.log(p)) * beta;
    }

    /** {@inheritDoc} */
    @Override
    public double getMean() {
        return mu + EULER * beta;
    }

    /** {@inheritDoc} */
    @Override
    public double getVariance() {
        return PI_SQUARED_OVER_SIX * beta * beta;
    }

    /** {@inheritDoc} */
    @Override
    public double getSupportLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSupportConnected() {
        return true;
    }

}
