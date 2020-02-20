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
package org.apache.commons.rng.sampling.distribution;

import org.apache.commons.rng.UniformRandomProvider;

/**
 * Sampling from the <a href="http://mathworld.wolfram.com/GammaDistribution.html">Gamma distribution</a>.
 * <ul>
 *  <li>
 *   For {@code 0 < theta < 1}:
 *   <blockquote>
 *    Ahrens, J. H. and Dieter, U.,
 *    <i>Computer methods for sampling from gamma, beta, Poisson and binomial distributions,</i>
 *    Computing, 12, 223-246, 1974.
 *   </blockquote>
 *  </li>
 *  <li>
 *  For {@code theta >= 1}:
 *   <blockquote>
 *   Marsaglia and Tsang, <i>A Simple Method for Generating
 *   Gamma Variables.</i> ACM Transactions on Mathematical Software,
 *   Volume 26 Issue 3, September, 2000.
 *   </blockquote>
 *  </li>
 * </ul>
 *
 * <p>Sampling uses:</p>
 *
 * <ul>
 *   <li>{@link UniformRandomProvider#nextDouble()} (both algorithms)
 *   <li>{@link UniformRandomProvider#nextLong()} (only for {@code theta >= 1})
 * </ul>
 *
 * @since 1.0
 */
public class AhrensDieterMarsagliaTsangGammaSampler
    extends SamplerBase
    implements ContinuousSampler {
    /** The appropriate gamma sampler for the parameters. */
    private final ContinuousSampler delegate;

    /**
     * Base class for a sampler from the Gamma distribution.
     */
    private abstract static class BaseAhrensDieterMarsagliaTsangGammaSampler
        implements ContinuousSampler {

        /** Underlying source of randomness. */
        protected final UniformRandomProvider rng;
        /** The shape parameter. */
        protected final double theta;
        /** The alpha parameter. */
        protected final double alpha;

        /**
         * @param rng Generator of uniformly distributed random numbers.
         * @param alpha Alpha shape parameter of the distribution.
         * @param theta Theta scale parameter of the distribution.
         * @throws IllegalArgumentException if {@code alpha <= 0} or {@code theta <= 0}
         */
        BaseAhrensDieterMarsagliaTsangGammaSampler(UniformRandomProvider rng,
                                                   double alpha,
                                                   double theta) {
            if (alpha <= 0) {
                throw new IllegalArgumentException("alpha is not strictly positive: " + alpha);
            }
            if (theta <= 0) {
                throw new IllegalArgumentException("theta is not strictly positive: " + theta);
            }
            this.rng = rng;
            this.alpha = alpha;
            this.theta = theta;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Ahrens-Dieter-Marsaglia-Tsang Gamma deviate [" + rng.toString() + "]";
        }
    }

    /**
     * Class to sample from the Gamma distribution when {@code 0 < theta < 1}.
     */
    private static class SmallThetaAhrensDieterMarsagliaTsangGammaSampler
        extends BaseAhrensDieterMarsagliaTsangGammaSampler {

        /** Inverse of "theta". */
        private final double oneOverTheta;
        /** Optimization (see code). */
        private final double bGSOptim;

        /**
         * @param rng Generator of uniformly distributed random numbers.
         * @param alpha Alpha shape parameter of the distribution.
         * @param theta Theta scale parameter of the distribution.
         * @throws IllegalArgumentException if {@code alpha <= 0} or {@code theta <= 0}
         */
        SmallThetaAhrensDieterMarsagliaTsangGammaSampler(UniformRandomProvider rng,
                                                         double alpha,
                                                         double theta) {
            super(rng, alpha, theta);
            oneOverTheta = 1 / theta;
            bGSOptim = 1 + theta / Math.E;
        }

        @Override
        public double sample() {
            // [1]: p. 228, Algorithm GS.

            while (true) {
                // Step 1:
                final double u = rng.nextDouble();
                final double p = bGSOptim * u;

                if (p <= 1) {
                    // Step 2:
                    final double x = Math.pow(p, oneOverTheta);
                    final double u2 = rng.nextDouble();

                    if (u2 > Math.exp(-x)) {
                        // Reject.
                        continue;
                    }
                    return alpha * x;
                }

                // Step 3:
                final double x = -Math.log((bGSOptim - p) * oneOverTheta);
                final double u2 = rng.nextDouble();

                if (u2 > Math.pow(x, theta - 1)) {
                    // Reject.
                    continue;
                }
                return alpha * x;
            }
        }
    }

    /**
     * Class to sample from the Gamma distribution when the {@code theta >= 1}.
     */
    private static class LargeThetaAhrensDieterMarsagliaTsangGammaSampler
        extends BaseAhrensDieterMarsagliaTsangGammaSampler {

        /** 1/3 */
        private static final double ONE_THIRD = 1d / 3;

        /** Optimization (see code). */
        private final double dOptim;
        /** Optimization (see code). */
        private final double cOptim;
        /** Gaussian sampling. */
        private final NormalizedGaussianSampler gaussian;

        /**
         * @param rng Generator of uniformly distributed random numbers.
         * @param alpha Alpha shape parameter of the distribution.
         * @param theta Theta scale parameter of the distribution.
         * @throws IllegalArgumentException if {@code alpha <= 0} or {@code theta <= 0}
         */
        LargeThetaAhrensDieterMarsagliaTsangGammaSampler(UniformRandomProvider rng,
                                                         double alpha,
                                                         double theta) {
            super(rng, alpha, theta);
            gaussian = new ZigguratNormalizedGaussianSampler(rng);
            dOptim = theta - ONE_THIRD;
            cOptim = ONE_THIRD / Math.sqrt(dOptim);
        }

        @Override
        public double sample() {
            while (true) {
                final double x = gaussian.sample();
                final double oPcTx = 1 + cOptim * x;
                final double v = oPcTx * oPcTx * oPcTx;

                if (v <= 0) {
                    continue;
                }

                final double x2 = x * x;
                final double u = rng.nextDouble();

                // Squeeze.
                if (u < 1 - 0.0331 * x2 * x2) {
                    return alpha * dOptim * v;
                }

                if (Math.log(u) < 0.5 * x2 + dOptim * (1 - v + Math.log(v))) {
                    return alpha * dOptim * v;
                }
            }
        }
    }

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param alpha Alpha shape parameter of the distribution.
     * @param theta Theta scale parameter of the distribution.
     * @throws IllegalArgumentException if {@code alpha <= 0} or {@code theta <= 0}
     */
    public AhrensDieterMarsagliaTsangGammaSampler(UniformRandomProvider rng,
                                                  double alpha,
                                                  double theta) {
        super(null);
        delegate = theta < 1 ?
            new SmallThetaAhrensDieterMarsagliaTsangGammaSampler(rng, alpha, theta) :
            new LargeThetaAhrensDieterMarsagliaTsangGammaSampler(rng, alpha, theta);
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        return delegate.sample();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return delegate.toString();
    }
}
