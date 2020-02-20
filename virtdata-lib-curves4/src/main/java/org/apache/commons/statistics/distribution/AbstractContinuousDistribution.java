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

import org.apache.commons.numbers.core.Precision;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.ContinuousInverseCumulativeProbabilityFunction;
import org.apache.commons.rng.sampling.distribution.ContinuousSampler;
import org.apache.commons.rng.sampling.distribution.InverseTransformContinuousSampler;

import java.util.function.DoubleUnaryOperator;

/**
 * Base class for probability distributions on the reals.
 * Default implementations are provided for some of the methods
 * that do not vary from distribution to distribution.
 *
 * This base class provides a default factory method for creating
 * a {@link ContinuousDistribution.Sampler sampler instance} that uses the
 * <a href="http://en.wikipedia.org/wiki/Inverse_transform_sampling">
 * inversion method</a> for generating random samples that follow the
 * distribution.
 */
abstract class AbstractContinuousDistribution
    implements ContinuousDistribution {
    /**
     * {@inheritDoc}
     *
     * The default implementation returns
     * <ul>
     * <li>{@link #getSupportLowerBound()} for {@code p = 0},</li>
     * <li>{@link #getSupportUpperBound()} for {@code p = 1}.</li>
     * </ul>
     */
    @Override
    public double inverseCumulativeProbability(final double p) {
        /*
         * IMPLEMENTATION NOTES
         * --------------------
         * Where applicable, use is made of the one-sided Chebyshev inequality
         * to bracket the root. This inequality states that
         * P(X - mu >= k * sig) <= 1 / (1 + k^2),
         * mu: mean, sig: standard deviation. Equivalently
         * 1 - P(X < mu + k * sig) <= 1 / (1 + k^2),
         * F(mu + k * sig) >= k^2 / (1 + k^2).
         *
         * For k = sqrt(p / (1 - p)), we find
         * F(mu + k * sig) >= p,
         * and (mu + k * sig) is an upper-bound for the root.
         *
         * Then, introducing Y = -X, mean(Y) = -mu, sd(Y) = sig, and
         * P(Y >= -mu + k * sig) <= 1 / (1 + k^2),
         * P(-X >= -mu + k * sig) <= 1 / (1 + k^2),
         * P(X <= mu - k * sig) <= 1 / (1 + k^2),
         * F(mu - k * sig) <= 1 / (1 + k^2).
         *
         * For k = sqrt((1 - p) / p), we find
         * F(mu - k * sig) <= p,
         * and (mu - k * sig) is a lower-bound for the root.
         *
         * In cases where the Chebyshev inequality does not apply, geometric
         * progressions 1, 2, 4, ... and -1, -2, -4, ... are used to bracket
         * the root.
         */
        if (p < 0 ||
            p > 1) {
            throw new DistributionException(DistributionException.OUT_OF_RANGE, p, 0, 1);
        }

        double lowerBound = getSupportLowerBound();
        if (p == 0) {
            return lowerBound;
        }

        double upperBound = getSupportUpperBound();
        if (p == 1) {
            return upperBound;
        }

        final double mu = getMean();
        final double sig = Math.sqrt(getVariance());
        final boolean chebyshevApplies;
        chebyshevApplies = !(Double.isInfinite(mu) ||
                             Double.isNaN(mu) ||
                             Double.isInfinite(sig) ||
                             Double.isNaN(sig));

        if (lowerBound == Double.NEGATIVE_INFINITY) {
            if (chebyshevApplies) {
                lowerBound = mu - sig * Math.sqrt((1 - p) / p);
            } else {
                lowerBound = -1;
                while (cumulativeProbability(lowerBound) >= p) {
                    lowerBound *= 2;
                }
            }
        }

        if (upperBound == Double.POSITIVE_INFINITY) {
            if (chebyshevApplies) {
                upperBound = mu + sig * Math.sqrt(p / (1 - p));
            } else {
                upperBound = 1;
                while (cumulativeProbability(upperBound) < p) {
                    upperBound *= 2;
                }
            }
        }

        // XXX Values copied from defaults in class
        // "o.a.c.math4.analysis.solvers.BaseAbstractUnivariateSolver"
        final double solverRelativeAccuracy = 1e-14;
        final double solverAbsoluteAccuracy = 1e-9;
        final double solverFunctionValueAccuracy = 1e-15;

        double x = new BrentSolver(solverRelativeAccuracy,
                                   solverAbsoluteAccuracy,
                                   solverFunctionValueAccuracy)
            .solve((arg) -> cumulativeProbability(arg) - p,
                   lowerBound,
                   0.5 * (lowerBound + upperBound),
                   upperBound);

        if (!isSupportConnected()) {
            /* Test for plateau. */
            final double dx = solverAbsoluteAccuracy;
            if (x - dx >= getSupportLowerBound()) {
                double px = cumulativeProbability(x);
                if (cumulativeProbability(x - dx) == px) {
                    upperBound = x;
                    while (upperBound - lowerBound > dx) {
                        final double midPoint = 0.5 * (lowerBound + upperBound);
                        if (cumulativeProbability(midPoint) < px) {
                            lowerBound = midPoint;
                        } else {
                            upperBound = midPoint;
                        }
                    }
                    return upperBound;
                }
            }
        }
        return x;
    }

    /**
     * Utility function for allocating an array and filling it with {@code n}
     * samples generated by the given {@code sampler}.
     *
     * @param n Number of samples.
     * @param sampler Sampler.
     * @return an array of size {@code n}.
     */
    public static double[] sample(int n,
                                  ContinuousDistribution.Sampler sampler) {
        final double[] samples = new double[n];
        for (int i = 0; i < n; i++) {
            samples[i] = sampler.sample();
        }
        return samples;
    }

    /**{@inheritDoc} */
    @Override
    public ContinuousDistribution.Sampler createSampler(final UniformRandomProvider rng) {
        return new ContinuousDistribution.Sampler() {
            /**
             * Inversion method distribution sampler.
             */
            private final ContinuousSampler sampler =
                new InverseTransformContinuousSampler(rng, createICPF());

            /** {@inheritDoc} */
            @Override
            public double sample() {
                return sampler.sample();
            }
        };
    }

    /**
     * @return an instance for use by {@link #createSampler(UniformRandomProvider)}
     */
    private ContinuousInverseCumulativeProbabilityFunction createICPF() {
        return new ContinuousInverseCumulativeProbabilityFunction() {
            /** {@inheritDoc} */
            @Override
            public double inverseCumulativeProbability(double p) {
                return AbstractContinuousDistribution.this.inverseCumulativeProbability(p);
            }
        };
    }

    /**
     * This class implements the <a href="http://mathworld.wolfram.com/BrentsMethod.html">
     * Brent algorithm</a> for finding zeros of real univariate functions.
     * The function should be continuous but not necessarily smooth.
     * The {@code solve} method returns a zero {@code x} of the function {@code f}
     * in the given interval {@code [a, b]} to within a tolerance
     * {@code 2 eps abs(x) + t} where {@code eps} is the relative accuracy and
     * {@code t} is the absolute accuracy.
     * <p>The given interval must bracket the root.</p>
     * <p>
     *  The reference implementation is given in chapter 4 of
     *  <blockquote>
     *   <b>Algorithms for Minimization Without Derivatives</b>,
     *   <em>Richard P. Brent</em>,
     *   Dover, 2002
     *  </blockquote>
     *
     * Used by {@link AbstractContinuousDistribution#inverseCumulativeProbability(double)}.
     */
    private static class BrentSolver {
        /** Relative accuracy. */
        private final double relativeAccuracy;
        /** Absolutee accuracy. */
        private final double absoluteAccuracy;
        /** Function accuracy. */
        private final double functionValueAccuracy;

        /**
         * Construct a solver.
         *
         * @param relativeAccuracy Relative accuracy.
         * @param absoluteAccuracy Absolute accuracy.
         * @param functionValueAccuracy Function value accuracy.
         */
        BrentSolver(double relativeAccuracy,
                    double absoluteAccuracy,
                    double functionValueAccuracy) {
            this.relativeAccuracy = relativeAccuracy;
            this.absoluteAccuracy = absoluteAccuracy;
            this.functionValueAccuracy = functionValueAccuracy;
        }

        /**
         * @param func Function to solve.
         * @param min Lower bound.
         * @param initial Initial guess.
         * @param max Upper bound.
         * @return the root.
         */
        double solve(DoubleUnaryOperator func,
                     double min,
                     double initial,
                     double max) {
            if (min > max) {
                throw new DistributionException(DistributionException.TOO_LARGE, min, max);
            }
            if (initial < min ||
                initial > max) {
                throw new DistributionException(DistributionException.OUT_OF_RANGE, initial, min, max);
            }

            // Return the initial guess if it is good enough.
            double yInitial = func.applyAsDouble(initial);
            if (Math.abs(yInitial) <= functionValueAccuracy) {
                return initial;
            }

            // Return the first endpoint if it is good enough.
            double yMin = func.applyAsDouble(min);
            if (Math.abs(yMin) <= functionValueAccuracy) {
                return min;
            }

            // Reduce interval if min and initial bracket the root.
            if (yInitial * yMin < 0) {
                return brent(func, min, initial, yMin, yInitial);
            }

            // Return the second endpoint if it is good enough.
            double yMax = func.applyAsDouble(max);
            if (Math.abs(yMax) <= functionValueAccuracy) {
                return max;
            }

            // Reduce interval if initial and max bracket the root.
            if (yInitial * yMax < 0) {
                return brent(func, initial, max, yInitial, yMax);
            }

            throw new DistributionException(DistributionException.BRACKETING, min, yMin, max, yMax);
        }

        /**
         * Search for a zero inside the provided interval.
         * This implementation is based on the algorithm described at page 58 of
         * the book
         * <blockquote>
         *  <b>Algorithms for Minimization Without Derivatives</b>,
         *  <it>Richard P. Brent</it>,
         *  Dover 0-486-41998-3
         * </blockquote>
         *
         * @param func Function to solve.
         * @param lo Lower bound of the search interval.
         * @param hi Higher bound of the search interval.
         * @param fLo Function value at the lower bound of the search interval.
         * @param fHi Function value at the higher bound of the search interval.
         * @return the value where the function is zero.
         */
        private double brent(DoubleUnaryOperator func,
                             double lo, double hi,
                             double fLo, double fHi) {
            double a = lo;
            double fa = fLo;
            double b = hi;
            double fb = fHi;
            double c = a;
            double fc = fa;
            double d = b - a;
            double e = d;

            final double t = absoluteAccuracy;
            final double eps = relativeAccuracy;

            while (true) {
                if (Math.abs(fc) < Math.abs(fb)) {
                    a = b;
                    b = c;
                    c = a;
                    fa = fb;
                    fb = fc;
                    fc = fa;
                }

                final double tol = 2 * eps * Math.abs(b) + t;
                final double m = 0.5 * (c - b);

                if (Math.abs(m) <= tol ||
                    Precision.equals(fb, 0))  {
                    return b;
                }
                if (Math.abs(e) < tol ||
                    Math.abs(fa) <= Math.abs(fb)) {
                    // Force bisection.
                    d = m;
                    e = d;
                } else {
                    double s = fb / fa;
                    double p;
                    double q;
                    // The equality test (a == c) is intentional,
                    // it is part of the original Brent's method and
                    // it should NOT be replaced by proximity test.
                    if (a == c) {
                        // Linear interpolation.
                        p = 2 * m * s;
                        q = 1 - s;
                    } else {
                        // Inverse quadratic interpolation.
                        q = fa / fc;
                        final double r = fb / fc;
                        p = s * (2 * m * q * (q - r) - (b - a) * (r - 1));
                        q = (q - 1) * (r - 1) * (s - 1);
                    }
                    if (p > 0) {
                        q = -q;
                    } else {
                        p = -p;
                    }
                    s = e;
                    e = d;
                    if (p >= 1.5 * m * q - Math.abs(tol * q) ||
                        p >= Math.abs(0.5 * s * q)) {
                        // Inverse quadratic interpolation gives a value
                        // in the wrong direction, or progress is slow.
                        // Fall back to bisection.
                        d = m;
                        e = d;
                    } else {
                        d = p / q;
                    }
                }
                a = b;
                fa = fb;

                if (Math.abs(d) > tol) {
                    b += d;
                } else if (m > 0) {
                    b += tol;
                } else {
                    b -= tol;
                }
                fb = func.applyAsDouble(b);
                if ((fb > 0 && fc > 0) ||
                    (fb <= 0 && fc <= 0)) {
                    c = a;
                    fc = fa;
                    d = b - a;
                    e = d;
                }
            }
        }
    }
}
