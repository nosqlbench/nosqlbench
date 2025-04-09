package io.nosqlbench.adapters.api.activityimpl.uniform;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import io.nosqlbench.nb.api.components.core.NBBaseComponent;
import io.nosqlbench.nb.api.engine.metrics.instruments.MetricCategory;
import io.nosqlbench.nb.api.labels.NBLabels;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A cache for Space instances that uses a ConcurrentHashMap to map string keys to spaces.
 * This is used for non-numeric space functions, where the space key is a string.
 * 
 * @param <S> The type of Space to cache
 */
public class ConcurrentStringSpaceCache<S extends Space> extends NBBaseComponent
    implements Iterable<S> {

    private final ConcurrentHashMap<String, S> cache = new ConcurrentHashMap<>();
    private final Function<String, S> valueLoader;

    public ConcurrentStringSpaceCache(DriverAdapter<?, S> adapter, Function<String, S> valueLoader) {
        super(adapter, NBLabels.forKV("string_spacesof", adapter.getAdapterName()));
        this.valueLoader = valueLoader;

        create().gauge(
            "string_spaces",
            () -> (double) cache.size(),
            MetricCategory.Internals,
            "The number of active string-keyed spaces for this adapter"
        );
    }

    /**
     * Get a space for the given key, creating it if it doesn't exist.
     *
     * @param key The key to get the space for
     * @return The space for the key
     */
    public S get(String key) {
        return cache.computeIfAbsent(key, valueLoader);
    }

    @Override
    public @NotNull Iterator<S> iterator() {
        return cache.values().iterator();
    }

    @Override
    protected void teardown() {
        for (S space : this) {
            try {
                space.close();
            } catch (Exception e) {
                // Log and continue
            }
        }
        cache.clear();
    }

    /**
     * Get the number of spaces in the cache.
     *
     * @return The number of spaces
     */
    public int size() {
        return cache.size();
    }
}
