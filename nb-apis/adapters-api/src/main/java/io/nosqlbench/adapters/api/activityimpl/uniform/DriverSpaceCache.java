/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapters.api.activityimpl.uniform;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * <p>A DriverSpaceCache is simply a place to hold something like a
 * client instance and all associated objects for quick and easy access. Each
 * space cache is simply a named and separate cache of objects. This is provided
 * as a convenient way to keep object state around which may be needed during the
 * course of executing operations with a driver or API. By naming each space, it
 * becomes possible for tests to create and use separate logical instances of
 * a client API for advanced testing. The default instance should simply be named
 * {@code default}</p>
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
public class DriverSpaceCache<S> {

    private final ConcurrentHashMap<String, S> cache = new ConcurrentHashMap<>();

    private final Function<String, S> newSpaceFunction;

    public DriverSpaceCache(Function<String, S> newSpaceFunction) {
        this.newSpaceFunction = newSpaceFunction;
    }

    public S get(String name) {
        return cache.computeIfAbsent(name, newSpaceFunction);
    }

    public Map<String, S> getElements() {
        return Collections.unmodifiableMap(cache);
    }

}
