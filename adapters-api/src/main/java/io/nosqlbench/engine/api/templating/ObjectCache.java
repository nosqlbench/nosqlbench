package io.nosqlbench.engine.api.templating;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * An object cache to memoize returned objects into a concurrent hash map by name.
 * This is meant to be used when you want to lazily initialize an instance of something
 * by name that is likely to be re-used over the lifetime of an owning object.
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
