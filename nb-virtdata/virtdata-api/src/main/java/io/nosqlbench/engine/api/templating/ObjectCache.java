/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.engine.api.templating;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An object cache to memoize returned objects into a concurrent hash map by name.
 * This is meant to be used when you want to lazily initialize an instance of something
 * by name that is likely to be re-used over the lifetime of an owning object,
 * and for which the value cardinality has a knowable and reasonable maximum.
 *
 * @param <T> The type of object.
 */
public class ObjectCache<T> implements Function<String,T> {

    private final ConcurrentHashMap<String,T> cache = new ConcurrentHashMap<>();

    private final Function<String, T> newInstanceFunction;

    public ObjectCache(Function<String,T> newInstanceFunction) {
        this.newInstanceFunction = newInstanceFunction;
    }

    @Override
    public T apply(String name) {
        return cache.computeIfAbsent(name, newInstanceFunction);
    }
}
