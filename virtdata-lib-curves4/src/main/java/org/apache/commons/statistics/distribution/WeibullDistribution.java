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

import org.apache.commons.numbers.gamma.LogGamma;

/**
 * Implementation of the Weibull distribution. This implementation uses the
 * two parameter form of the distribution defined by
 * <a href="http://mathworld.wolfram.com/WeibullDistribution.html">
 * Weibull Distribution</a>, equations (1) and (2).
 *
 * @see <a href="http://en.wikipedia.org/wiki/Weibull_distribution">Weibull distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/WeibullDistribution.html">Weibull distribution (MathWorld)</a>
 *
 * @since 1.1
 */
public class WeibullDistribution extends AbstractContinuousDistribution {
    /** The shape parameter. */
    private final double shape;
    /** The scale parameter. */
    private final double scale;

    /**
     * Creates a distribution.
     *
     * @param alpha Shape parameter.
     * @param beta Scale parameter.
     * @throws IllegalArgumentException if {@code alpha <= 0} or {@code beta <= 0}.
     */
    public WeibullDistribution(double alpha,
                               double beta) {
        if (alpha <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE,
                                            alpha);
        }
        if (beta <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE,
                                            beta);
        }
        scale = beta;
        shape = alpha;
    }

    /**
     * Access the shape parameter, {@code alpha}.
     *
     * @return the shape parameter, {@code alpha}.
     */
    public double getShape() {
        return shape;
    }

    /**
     * Access the scale parameter, {@code beta}.
     *
     * @return the scale parameter, {@code beta}.
     */
    public double getScale() {
        return scale;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        if (x < 0) {
            return 0;
        }

        final double xscale = x / scale;
        final double xscalepow = Math.pow(xscale, shape - 1);

        /*
         * Math.pow(x / scale, shape) =
         * Math.pow(xscale, shape) =
         * Math.pow(xscale, shape - 1) * xscale
         */
        final double xscalepowshape = xscalepow * xscale;

        return (shape / scale) * xscalepow * Math.exp(-xscalepowshape);
    }

    /** {@inheritDoc} */
    @Override
    public double logDensity(double x) {
        if (x < 0) {
            return Double.NEGATIVE_INFINITY;
        }

        final double xscale = x / scale;
        final double logxscalepow = Math.log(xscale) * (shape - 1);

        /*
         * Math.pow(x / scale, shape) =
         * Math.pow(xscale, shape) =
         * Math.pow(xscale, shape - 1) * xscale
         */
        final double xscalepowshape = Math.exp(logxscalepow) * xscale;

        return Math.log(shape / scale) + logxscalepow - xscalepowshape;
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x) {
        double ret;
        if (x <= 0.0) {
            ret = 0.0;
        } else {
            ret = 1.0 - Math.exp(-Math.pow(x / scale, shape));
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Returns {@code 0} when {@code p == 0} and
     * {@code Double.POSITIVE_INFINITY} when {@code p == 1}.
     */
    @Override
    public double inverseCumulativeProbability(double p) {
        double ret;
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        } else if (p == 0) {
            ret = 0.0;
        } else  if (p == 1) {
            ret = Double.POSITIVE_INFINITY;
        } else {
            ret = scale * Math.pow(-Math.log1p(-p), 1.0 / shape);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * The mean is {@code scale * Gamma(1 + (1 / shape))}, where {@code Gamma()}
     * is the Gamma-function.
     */
    @Override
    public double getMean() {
        final double sh = getShape();
        final double sc = getScale();

        return sc * Math.exp(LogGamma.value(1 + (1 / sh)));
    }

    /**
     * {@inheritDoc}
     *
     * The variance is {@code scale^2 * Gamma(1 + (2 / shape)) - mean^2}
     * where {@code Gamma()} is the Gamma-function.
     */
    @Override
    public double getVariance() {
        final double sh = getShape();
        final double sc = getScale();
        final double mn = getMean();

        return (sc * sc) * Math.exp(LogGamma.value(1 + (2 / sh))) -
               (mn * mn);
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
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
     * no matter the parameters.
     *
     * @return upper bound of the support (always
     * {@code Double.POSITIVE_INFINITY})
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

