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
import java.util.function.LongFunction;

/**
 * <P>Native driver state in NoSQLBench is stored in a context called a {@link Space}, with each driver adapter
 * providing its own implementation. The easiest way to create one of these is to derive from {@link BaseSpace}
 * The generic type parameters for the DriverAdapter API propogate through all the related interfaces so that
 * op mappers, dispensers, and spaces are familiar to each other. This makes it easy, for example, to use
 * support functions from a special adapter implementation in a type-safe and convenient way.
 * </P>
 *
 * <p>A Driver Space Cache is simply a place to hold something like a client instance and all associated objects for
 * quick and easy access. Each space cache is simply a named and separate cache of objects. This is provided as a
 * convenient way to keep object state around which may be needed during the course of executing operations with a
 * driver or API. By naming each space, it becomes possible for tests to create and use separate logical instances of a
 * client API for advanced testing. The default instance should simply be named {@code default}</p>
 *
 * <p>Most native drivers use some combination of fluent, functional, and declarative patterns. These usually require
 * you to keep access to a set of core state-holding objects in order to construct new elements to drive operations
 * with. An example of this would be creating an executable operation from a session object. It is necessary to keep the
 * session around in for when you create new statements. Maintaining the session object is considered an essential part
 * of idiomatic and efficient use of the API. Further, you may have builders or factories that are created from the
 * session which should be cached as well. Keeping all these objects together requires attaching them to a cohesive
 * owning object -- That is the space cache.</p>
 *
 * @param <S>
 *     The type which will represent the cache for a given type of adapter.
 */
public class ConcurrentSpaceCache<S extends Space> extends NBBaseComponent implements Iterable<S> {

    private final ConcurrentIndexCache<S> cache;

    public ConcurrentSpaceCache(DriverAdapter<?, S> adapter, LongFunction<S> valueLoader) {
        super(adapter, NBLabels.forKV("spacesof", adapter.getAdapterName()));
        this.cache = new ConcurrentIndexCache<>("spacesof_" + adapter.getAdapterName(), valueLoader);

        create().gauge(
            "spaces",
            () -> (double) cache.size(),
            MetricCategory.Internals,
            "The number of active spaces for this adapter"
        );
    }

    public S get(long l) {
        return cache.get(l);
    }

    @Override
    public @NotNull Iterator<S> iterator() {
        return cache.iterator();
    }
}
