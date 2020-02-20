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
 * Interface for a discrete distribution that can be sampled using
 * the <a href="https://en.wikipedia.org/wiki/Inverse_transform_sampling">
 * inversion method</a>.
 *
 * @since 1.0
 */
public interface DiscreteInverseCumulativeProbabilityFunction {
    /**
     * Computes the quantile function of the distribution.
     * For a random variable {@code X} distributed according to this distribution,
     * the returned value is
     * <ul>
     *  <li>\( \inf_{x \in \mathcal{Z}} P(X \le x) \ge p \) for \( 0 \lt p \le 1 \)</li>
     *  <li>\( \inf_{x \in \mathcal{Z}} P(X \le x) \gt 0 \) for \( p = 0 \)</li>
     * </ul>
     *
     * @param p Cumulative probability.
     * @return the smallest {@code p}-quantile of the distribution
     * (largest 0-quantile for {@code p = 0}).
     * @throws IllegalArgumentException if {@code p < 0} or {@code p > 1}.
     */
    int inverseCumulativeProbability(double p);
}
