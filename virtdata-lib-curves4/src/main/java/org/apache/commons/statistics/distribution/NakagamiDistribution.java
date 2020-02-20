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

import org.apache.commons.numbers.gamma.Gamma;
import org.apache.commons.numbers.gamma.RegularizedGamma;

/**
 * This class implements the <a href="http://en.wikipedia.org/wiki/Nakagami_distribution">Nakagami distribution</a>.
 */
public class NakagamiDistribution extends AbstractContinuousDistribution {
    /** The shape parameter. */
    private final double mu;
    /** The scale parameter. */
    private final double omega;

    /**
     * Creates a distribution.
     *
     * @param mu shape parameter
     * @param omega scale parameter (must be positive)
     * @throws IllegalArgumentException  if {@code mu < 0.5} or if
     * {@code omega <= 0}.
     */
    public NakagamiDistribution(double mu,
                                double omega) {
        if (mu < 0.5) {
            throw new DistributionException(DistributionException.TOO_SMALL, mu, 0.5);
        }
        if (omega <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE, omega);
        }

        this.mu = mu;
        this.omega = omega;
    }

    /**
     * Access the shape parameter, {@code mu}.
     *
     * @return the shape parameter.
     */
    public double getShape() {
        return mu;
    }

    /**
     * Access the scale parameter, {@code omega}.
     *
     * @return the scale parameter.
     */
    public double getScale() {
        return omega;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        if (x <= 0) {
            return 0.0;
        }
        return 2.0 * Math.pow(mu, mu) / (Gamma.value(mu) * Math.pow(omega, mu)) *
                     Math.pow(x, 2 * mu - 1) * Math.exp(-mu * x * x / omega);
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x) {
        return RegularizedGamma.P.value(mu, mu * x * x / omega);
    }

    /** {@inheritDoc} */
    @Override
    public double getMean() {
        return Gamma.value(mu + 0.5) / Gamma.value(mu) * Math.sqrt(omega / mu);
    }

    /** {@inheritDoc} */
    @Override
    public double getVariance() {
        double v = Gamma.value(mu + 0.5) / Gamma.value(mu);
        return omega * (1 - 1 / mu * v * v);
    }

    /** {@inheritDoc} */
    @Override
    public double getSupportLowerBound() {
        return 0;
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
