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
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Logistic_distribution">Logistic distribution</a>.
 */
public class LogisticDistribution extends AbstractContinuousDistribution {
    /** &pi;<sup>2</sup>/3. */
    private static final double PI_SQUARED_OVER_THREE = Math.PI * Math.PI / 3;
    /** Location parameter. */
    private final double mu;
    /** Scale parameter. */
    private final double scale;
    /** Inverse of "scale". */
    private final double oneOverScale;

    /**
     * Creates a distribution.
     *
     * @param mu Location parameter.
     * @param scale Scale parameter (must be positive).
     * @throws IllegalArgumentException if {@code scale <= 0}.
     */
    public LogisticDistribution(double mu,
                                double scale) {
        if (scale <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE,
                                            scale);
        }

        this.mu = mu;
        this.scale = scale;
        this.oneOverScale = 1 / scale;
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
        return scale;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        final double z = oneOverScale * (x - mu);
        final double v = Math.exp(-z);
        return oneOverScale * v / ((1 + v) * (1 + v));
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x) {
        final double z = oneOverScale * (x - mu);
        return 1 / (1 + Math.exp(-z));
    }

    /** {@inheritDoc} */
    @Override
    public double inverseCumulativeProbability(double p) {
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        } else if (p == 0) {
            return 0;
        } else if (p == 1) {
            return Double.POSITIVE_INFINITY;
        } else {
            return scale * Math.log(p / (1 - p)) + mu;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getMean() {
        return mu;
    }

    /** {@inheritDoc} */
    @Override
    public double getVariance() {
        return oneOverScale * oneOverScale * PI_SQUARED_OVER_THREE;
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
