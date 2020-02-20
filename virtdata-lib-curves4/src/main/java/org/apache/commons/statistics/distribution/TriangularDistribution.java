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
 * Implementation of the triangular real distribution.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Triangular_distribution">
 * Triangular distribution (Wikipedia)</a>
 *
 * @since 3.0
 */
public class TriangularDistribution extends AbstractContinuousDistribution {
    /** Serializable version identifier. */
    private static final long serialVersionUID = 20160311L;
    /** Lower limit of this distribution (inclusive). */
    private final double a;
    /** Upper limit of this distribution (inclusive). */
    private final double b;
    /** Mode of this distribution. */
    private final double c;

    /**
     * Creates a distribution.
     *
     * @param a Lower limit of this distribution (inclusive).
     * @param b Upper limit of this distribution (inclusive).
     * @param c Mode of this distribution.
     * @throws IllegalArgumentException if {@code a >= b}, if {@code c > b}
     * or if {@code c < a}.
     */
    public TriangularDistribution(double a,
                                  double c,
                                  double b) {
        if (a >= b) {
            throw new DistributionException(DistributionException.TOO_LARGE,
                                            a, b);
        }
        if (c < a) {
            throw new DistributionException(DistributionException.TOO_SMALL,
                                            c, a);
        }
        if (c > b) {
            throw new DistributionException(DistributionException.TOO_LARGE,
                                            c, b);
        }

        this.a = a;
        this.c = c;
        this.b = b;
    }

    /**
     * Gets the mode.
     *
     * @return the mode of the distribution.
     */
    public double getMode() {
        return c;
    }

    /**
     * {@inheritDoc}
     *
     * For lower limit {@code a}, upper limit {@code b} and mode {@code c}, the
     * PDF is given by
     * <ul>
     * <li>{@code 2 * (x - a) / [(b - a) * (c - a)]} if {@code a <= x < c},</li>
     * <li>{@code 2 / (b - a)} if {@code x = c},</li>
     * <li>{@code 2 * (b - x) / [(b - a) * (b - c)]} if {@code c < x <= b},</li>
     * <li>{@code 0} otherwise.
     * </ul>
     */
    @Override
    public double density(double x) {
        if (x < a) {
            return 0;
        }
        if (a <= x && x < c) {
            double divident = 2 * (x - a);
            double divisor = (b - a) * (c - a);
            return divident / divisor;
        }
        if (x == c) {
            return 2 / (b - a);
        }
        if (c < x && x <= b) {
            double divident = 2 * (b - x);
            double divisor = (b - a) * (b - c);
            return divident / divisor;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * For lower limit {@code a}, upper limit {@code b} and mode {@code c}, the
     * CDF is given by
     * <ul>
     * <li>{@code 0} if {@code x < a},</li>
     * <li>{@code (x - a)^2 / [(b - a) * (c - a)]} if {@code a <= x < c},</li>
     * <li>{@code (c - a) / (b - a)} if {@code x = c},</li>
     * <li>{@code 1 - (b - x)^2 / [(b - a) * (b - c)]} if {@code c < x <= b},</li>
     * <li>{@code 1} if {@code x > b}.</li>
     * </ul>
     */
    @Override
    public double cumulativeProbability(double x)  {
        if (x < a) {
            return 0;
        }
        if (a <= x && x < c) {
            double divident = (x - a) * (x - a);
            double divisor = (b - a) * (c - a);
            return divident / divisor;
        }
        if (x == c) {
            return (c - a) / (b - a);
        }
        if (c < x && x <= b) {
            double divident = (b - x) * (b - x);
            double divisor = (b - a) * (b - c);
            return 1 - (divident / divisor);
        }
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * For lower limit {@code a}, upper limit {@code b}, and mode {@code c},
     * the mean is {@code (a + b + c) / 3}.
     */
    @Override
    public double getMean() {
        return (a + b + c) / 3;
    }

    /**
     * {@inheritDoc}
     *
     * For lower limit {@code a}, upper limit {@code b}, and mode {@code c},
     * the variance is {@code (a^2 + b^2 + c^2 - a * b - a * c - b * c) / 18}.
     */
    @Override
    public double getVariance() {
        return (a * a + b * b + c * c - a * b - a * c - b * c) / 18;
    }

    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is equal to the lower limit parameter
     * {@code a} of the distribution.
     *
     * @return lower bound of the support
     */
    @Override
    public double getSupportLowerBound() {
        return a;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is equal to the upper limit parameter
     * {@code b} of the distribution.
     *
     * @return upper bound of the support
     */
    @Override
    public double getSupportUpperBound() {
        return b;
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
    public double inverseCumulativeProbability(double p) {
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        }
        if (p == 0) {
            return a;
        }
        if (p == 1) {
            return b;
        }
        if (p < (c - a) / (b - a)) {
            return a + Math.sqrt(p * (b - a) * (c - a));
        }
        return b - Math.sqrt((1 - p) * (b - a) * (b - c));
    }
}
