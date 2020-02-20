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
 * Implementation of the <a href="http://en.wikipedia.org/wiki/Cauchy_distribution">Cauchy distribution</a>.
 */
public class CauchyDistribution extends AbstractContinuousDistribution {
    /** The median of this distribution. */
    private final double median;
    /** The scale of this distribution. */
    private final double scale;

    /**
     * Creates a distribution.
     *
     * @param median Median for this distribution.
     * @param scale Scale parameter for this distribution.
     * @throws IllegalArgumentException if {@code scale <= 0}.
     */
    public CauchyDistribution(double median,
                              double scale) {
        if (scale <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE, scale);
        }
        this.scale = scale;
        this.median = median;
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x) {
        return 0.5 + (Math.atan((x - median) / scale) / Math.PI);
    }

    /**
     * Access the median.
     *
     * @return the median for this distribution.
     */
    public double getMedian() {
        return median;
    }

    /**
     * Access the scale parameter.
     *
     * @return the scale parameter for this distribution.
     */
    public double getScale() {
        return scale;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        final double dev = x - median;
        return (1 / Math.PI) * (scale / (dev * dev + scale * scale));
    }

    /**
     * {@inheritDoc}
     *
     * Returns {@code Double.NEGATIVE_INFINITY} when {@code p == 0}
     * and {@code Double.POSITIVE_INFINITY} when {@code p == 1}.
     */
    @Override
    public double inverseCumulativeProbability(double p) {
        double ret;
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        } else if (p == 0) {
            ret = Double.NEGATIVE_INFINITY;
        } else  if (p == 1) {
            ret = Double.POSITIVE_INFINITY;
        } else {
            ret = median + scale * Math.tan(Math.PI * (p - .5));
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * The mean is always undefined no matter the parameters.
     *
     * @return mean (always Double.NaN)
     */
    @Override
    public double getMean() {
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     *
     * The variance is always undefined no matter the parameters.
     *
     * @return variance (always Double.NaN)
     */
    @Override
    public double getVariance() {
        return Double.NaN;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always negative infinity no matter
     * the parameters.
     *
     * @return lower bound of the support (always Double.NEGATIVE_INFINITY)
     */
    @Override
    public double getSupportLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity no matter
     * the parameters.
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
}
