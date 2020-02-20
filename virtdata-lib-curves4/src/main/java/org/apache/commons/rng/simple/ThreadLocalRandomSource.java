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
package org.apache.commons.rng.simple;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.EnumMap;

/**
 * This class provides a thread-local {@link UniformRandomProvider}.
 *
 * <p>The {@link UniformRandomProvider} is created once-per-thread using the default
 * construction method {@link RandomSource#create(RandomSource)}.
 *
 * <p>Example:</p>
 * <pre><code>
 * import org.apache.commons.rng.simple.RandomSource;
 * import org.apache.commons.rng.simple.ThreadLocalRandomSource;
 * import org.apache.commons.rng.sampling.distribution.PoissonSampler;
 *
 * // Access a thread-safe random number generator
 * UniformRandomProvider rng = ThreadLocalRandomSource.current(RandomSource.SPLIT_MIX_64);
 *
 * // One-time Poisson sample
 * double mean = 12.3;
 * int counts = new PoissonSampler(rng, mean).sample();
 * </code></pre>
 *
 * <p>Note if the {@link RandomSource} requires additional arguments then it is not
 * supported. The same can be achieved using:</p>
 *
 * <pre><code>
 * import org.apache.commons.rng.simple.RandomSource;
 * import org.apache.commons.rng.sampling.distribution.PoissonSampler;
 *
 * // Provide a thread-safe random number generator with data arguments
 * private static ThreadLocal&lt;UniformRandomProvider&gt; rng =
 *     new ThreadLocal&lt;UniformRandomProvider&gt;() {
 *         &#64;Override
 *         protected UniformRandomProvider initialValue() {
 *             return RandomSource.create(RandomSource.TWO_CMRES_SELECT, null, 3, 4);
 *         }
 *     };
 *
 * // One-time Poisson sample using a thread-safe random number generator
 * double mean = 12.3;
 * int counts = new PoissonSampler(rng.get(), mean).sample();
 * </code></pre>
 *
 * @since 1.3
 */
public final class ThreadLocalRandomSource {
    /**
     * A map containing the {@link ThreadLocal} instance for each {@link RandomSource}.
     *
     * <p>This should only be modified to create new instances in a synchronized block.
     */
    private static EnumMap<RandomSource, ThreadLocal<UniformRandomProvider>> sources =
        new EnumMap<RandomSource,
                    ThreadLocal<UniformRandomProvider>>(RandomSource.class);

    /** No public construction. */
    private ThreadLocalRandomSource() {}

    /**
     * Extend the {@link ThreadLocal} to allow creation of the desired {@link RandomSource}.
     */
    private static class ThreadLocalRng extends ThreadLocal<UniformRandomProvider> {
        /** The source. */
        private final RandomSource source;

        /**
         * Create a new instance.
         *
         * @param source the source
         */
        ThreadLocalRng(RandomSource source) {
            this.source = source;
        }

        @Override
        protected UniformRandomProvider initialValue() {
            // Create with the default seed generation method
            return RandomSource.create(source, null);
        }
    }

    /**
     * Returns the current thread's copy of the given {@code source}. If there is no
     * value for the current thread, it is first initialized to the value returned
     * by {@link RandomSource#create(RandomSource)}.
     *
     * <p>Note if the {@code source} requires additional arguments then it is not
     * supported.
     *
     * @param source the source
     * @return the current thread's value of the {@code source}.
     * @throws IllegalArgumentException if the source is null or the source requires arguments
     */
    public static UniformRandomProvider current(RandomSource source) {
        ThreadLocal<UniformRandomProvider> rng = sources.get(source);
        // Implement double-checked locking:
        // https://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java
        if (rng == null) {
            // Do the checks on the source here since it is an edge case
            // and the EnumMap handles null (returning null).
            if (source == null) {
                throw new IllegalArgumentException("Random source is null");
            }

            synchronized (sources) {
                rng = sources.get(source);
                if (rng == null) {
                    rng = new ThreadLocalRng(source);
                    sources.put(source, rng);
                }
            }
        }
        return rng.get();
    }
}
