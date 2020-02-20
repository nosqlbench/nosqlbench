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
 * Discrete uniform distribution sampler.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextInt(int)} when
 * the range {@code (upper - lower) <} {@link Integer#MAX_VALUE}, otherwise
 * {@link UniformRandomProvider#nextInt()}.</p>
 *
 * @since 1.0
 */
public class DiscreteUniformSampler
    extends SamplerBase
    implements DiscreteSampler {

    /** The appropriate uniform sampler for the parameters. */
    private final DiscreteSampler delegate;

    /**
     * Base class for a sampler from a discrete uniform distribution.
     */
    private abstract static class AbstractDiscreteUniformSampler
        implements DiscreteSampler {

        /** Underlying source of randomness. */
        protected final UniformRandomProvider rng;
        /** Lower bound. */
        protected final int lower;

        /**
         * @param rng Generator of uniformly distributed random numbers.
         * @param lower Lower bound (inclusive) of the distribution.
         */
        AbstractDiscreteUniformSampler(UniformRandomProvider rng,
                                       int lower) {
            this.rng = rng;
            this.lower = lower;
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Uniform deviate [" + rng.toString() + "]";
        }
    }

    /**
     * Discrete uniform distribution sampler when the range between lower and upper is small
     * enough to fit in a positive integer.
     */
    private static class SmallRangeDiscreteUniformSampler
        extends AbstractDiscreteUniformSampler {

        /** Maximum range of the sample from the lower bound (exclusive). */
        private final int range;

        /**
         * @param rng Generator of uniformly distributed random numbers.
         * @param lower Lower bound (inclusive) of the distribution.
         * @param range Maximum range of the sample from the lower bound (exclusive).
         */
        SmallRangeDiscreteUniformSampler(UniformRandomProvider rng,
                                         int lower,
                                         int range) {
            super(rng, lower);
            this.range = range;
        }

        @Override
        public int sample() {
            return lower + rng.nextInt(range);
        }
    }

    /**
     * Discrete uniform distribution sampler when the range between lower and upper is too large
     * to fit in a positive integer.
     */
    private static class LargeRangeDiscreteUniformSampler
        extends AbstractDiscreteUniformSampler {

        /** Upper bound. */
        private final int upper;

        /**
         * @param rng Generator of uniformly distributed random numbers.
         * @param lower Lower bound (inclusive) of the distribution.
         * @param upper Upper bound (inclusive) of the distribution.
         */
        LargeRangeDiscreteUniformSampler(UniformRandomProvider rng,
                                         int lower,
                                         int upper) {
            super(rng, lower);
            this.upper = upper;
        }

        @Override
        public int sample() {
            // Use a simple rejection method.
            // This is used when (upper-lower) >= Integer.MAX_VALUE.
            // This will loop on average 2 times in the worst case scenario
            // when (upper-lower) == Integer.MAX_VALUE.
            while (true) {
                final int r = rng.nextInt();
                if (r >= lower &&
                    r <= upper) {
                    return r;
                }
            }
        }
    }

    /**
     * @param rng Generator of uniformly distributed random numbers.
     * @param lower Lower bound (inclusive) of the distribution.
     * @param upper Upper bound (inclusive) of the distribution.
     * @throws IllegalArgumentException if {@code lower > upper}.
     */
    public DiscreteUniformSampler(UniformRandomProvider rng,
                                  int lower,
                                  int upper) {
        super(null);
        if (lower > upper) {
            throw new IllegalArgumentException(lower  + " > " + upper);
        }
        // Choose the algorithm depending on the range
        final int range = (upper - lower) + 1;
        delegate = range <= 0 ?
            // The range is too wide to fit in a positive int (larger
            // than 2^31); use a simple rejection method.
            new LargeRangeDiscreteUniformSampler(rng, lower, upper) :
            // Use a sample from the range added to the lower bound.
            new SmallRangeDiscreteUniformSampler(rng, lower, range);
    }

    /** {@inheritDoc} */
    @Override
    public int sample() {
        return delegate.sample();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return delegate.toString();
    }
}
