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

package org.apache.commons.rng.sampling;

import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.sampling.distribution.NormalizedGaussianSampler;
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler;

/**
 * Generate vectors <a href="http://mathworld.wolfram.com/SpherePointPicking.html">
 * isotropically located on the surface of a sphere</a>.
 *
 * <p>Sampling uses:</p>
 *
 * <ul>
 *   <li>{@link UniformRandomProvider#nextLong()}
 *   <li>{@link UniformRandomProvider#nextDouble()}
 * </ul>
 *
 * @since 1.1
 */
public class UnitSphereSampler {
    /** Sampler used for generating the individual components of the vectors. */
    private final NormalizedGaussianSampler sampler;
    /** Space dimension. */
    private final int dimension;

    /**
     * @param dimension Space dimension.
     * @param rng Generator for the individual components of the vectors.
     * A shallow copy will be stored in this instance.
     * @throws IllegalArgumentException If {@code dimension <= 0}
     */
    public UnitSphereSampler(int dimension,
                             UniformRandomProvider rng) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be strictly positive");
        }

        this.dimension = dimension;
        sampler = new ZigguratNormalizedGaussianSampler(rng);
    }

    /**
     * @return a random normalized Cartesian vector.
     */
    public double[] nextVector() {
        final double[] v = new double[dimension];

        // Pick a point by choosing a standard Gaussian for each element,
        // and then normalize to unit length.
        double normSq = 0;
        for (int i = 0; i < dimension; i++) {
            final double comp = sampler.sample();
            v[i] = comp;
            normSq += comp * comp;
        }

        if (normSq == 0) {
            // Zero-norm vector is discarded.
            // Using recursion as it is highly unlikely to generate more
            // than a few such vectors. It also protects against infinite
            // loop (in case a buggy generator is used), by eventually
            // raising a "StackOverflowError".
            return nextVector();
        }

        final double f = 1 / Math.sqrt(normSq);
        for (int i = 0; i < dimension; i++) {
            v[i] *= f;
        }

        return v;
    }
}
