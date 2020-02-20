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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sampling from a {@link Collection}.
 *
 * <p>Sampling uses {@link UniformRandomProvider#nextInt(int)}.</p>
 *
 * @param <T> Type of items in the collection.
 *
 * @since 1.0
 */
public class CollectionSampler<T> {
    /** Collection to be sampled from. */
    private final List<T> items;
    /** RNG. */
    private final UniformRandomProvider rng;

    /**
     * Creates a sampler.
     *
     * @param rng Generator of uniformly distributed random numbers.
     * @param collection Collection to be sampled.
     * A (shallow) copy will be stored in the created instance.
     * @throws IllegalArgumentException if {@code collection} is empty.
     */
    public CollectionSampler(UniformRandomProvider rng,
                             Collection<T> collection) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException("Empty collection");
        }

        this.rng = rng;
        items = new ArrayList<T>(collection);
    }

    /**
     * Picks one of the items from the
     * {@link #CollectionSampler(UniformRandomProvider,Collection)
     * collection passed to the constructor}.
     *
     * @return a random sample.
     */
    public T sample() {
        return items.get(rng.nextInt(items.size()));
    }
}
