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
import org.apache.commons.numbers.gamma.RegularizedBeta;

/**
 * Implementation of <a href='http://en.wikipedia.org/wiki/Student&apos;s_t-distribution'>Student's t-distribution</a>.
 */
public class TDistribution extends AbstractContinuousDistribution {
    /** The degrees of freedom. */
    private final double degreesOfFreedom;
    /** degreesOfFreedom / 2 */
    private final double dofOver2;
    /** Cached value. */
    private final double factor;
    /** Cached value. */
    private final double mean;
    /** Cached value. */
    private final double variance;

    /**
     * Creates a distribution.
     *
     * @param degreesOfFreedom Degrees of freedom.
     * @throws IllegalArgumentException if {@code degreesOfFreedom <= 0}
     */
    public TDistribution(double degreesOfFreedom) {
        if (degreesOfFreedom <= 0) {
            throw new DistributionException(DistributionException.NEGATIVE,
                                            degreesOfFreedom);
        }
        this.degreesOfFreedom = degreesOfFreedom;

        dofOver2 = 0.5 * degreesOfFreedom;
        factor = LogGamma.value(dofOver2 + 0.5) -
                 0.5 * (Math.log(Math.PI) + Math.log(degreesOfFreedom)) -
                 LogGamma.value(dofOver2);
        mean = degreesOfFreedom > 1 ? 0 :
            Double.NaN;
        variance = degreesOfFreedom > 2 ? degreesOfFreedom / (degreesOfFreedom - 2) :
            degreesOfFreedom > 1 && degreesOfFreedom <= 2 ? Double.POSITIVE_INFINITY :
            Double.NaN;
    }

    /**
     * Access the degrees of freedom.
     *
     * @return the degrees of freedom.
     */
    public double getDegreesOfFreedom() {
        return degreesOfFreedom;
    }

    /** {@inheritDoc} */
    @Override
    public double density(double x) {
        return Math.exp(logDensity(x));
    }

    /** {@inheritDoc} */
    @Override
    public double logDensity(double x) {
        final double nPlus1Over2 = dofOver2 + 0.5;
        return factor - nPlus1Over2 * Math.log1p(x * x / degreesOfFreedom);
    }

    /** {@inheritDoc} */
    @Override
    public double cumulativeProbability(double x) {
        if (x == 0) {
            return 0.5;
        } else {
            final double t =
                RegularizedBeta.value(degreesOfFreedom / (degreesOfFreedom + (x * x)),
                                      dofOver2,
                                      0.5);
            return x < 0 ?
                0.5 * t :
                1 - 0.5 * t;
        }
    }

    /**
     * {@inheritDoc}
     *
     * For degrees of freedom parameter {@code df}, the mean is
     * <ul>
     *  <li>zero if {@code df > 1}, and</li>
     *  <li>undefined ({@code Double.NaN}) otherwise.</li>
     * </ul>
     */
    @Override
    public double getMean() {
        return mean;
    }

    /**
     * {@inheritDoc}
     *
     * For degrees of freedom parameter {@code df}, the variance is
     * <ul>
     *  <li>{@code df / (df - 2)} if {@code df > 2},</li>
     *  <li>infinite ({@code Double.POSITIVE_INFINITY}) if {@code 1 < df <= 2}, and</li>
     *  <li>undefined ({@code Double.NaN}) otherwise.</li>
     * </ul>
     */
    @Override
    public double getVariance() {
        return variance;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always negative infinity..
     *
     * @return lower bound of the support (always
     * {@code Double.NEGATIVE_INFINITY})
     */
    @Override
    public double getSupportLowerBound() {
        return Double.NEGATIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity.
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
