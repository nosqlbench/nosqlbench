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

/**
 * Sampling from a log-normal distribution.
 *
 * @since 1.1
 */
public class LogNormalSampler implements ContinuousSampler {
    /** Scale. */
    private final double scale;
    /** Shape. */
    private final double shape;
    /** Gaussian sampling. */
    private final NormalizedGaussianSampler gaussian;

    /**
     * @param gaussian N(0,1) generator.
     * @param scale Scale of the log-normal distribution.
     * @param shape Shape of the log-normal distribution.
     * @throws IllegalArgumentException if {@code scale < 0} or {@code shape <= 0}.
     */
    public LogNormalSampler(NormalizedGaussianSampler gaussian,
                            double scale,
                            double shape) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale is not positive: " + scale);
        }
        if (shape <= 0) {
            throw new IllegalArgumentException("shape is not strictly positive: " + shape);
        }
        this.scale = scale;
        this.shape = shape;
        this.gaussian = gaussian;
    }

    /** {@inheritDoc} */
    @Override
    public double sample() {
        return Math.exp(scale + shape * gaussian.sample());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Log-normal deviate [" + gaussian.toString() + "]";
    }
}
