package io.nosqlbench.engine.api.activityimpl.uniform;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DriverSpaceCache<S> {

    private final ConcurrentHashMap<String,S> cache = new ConcurrentHashMap<>();
    private final Function<String, S> newSpaceFunction;

    public DriverSpaceCache(Function<String,S> newSpaceFunction) {
        this.newSpaceFunction = newSpaceFunction;
    }

    public S get(String name) {
        return cache.computeIfAbsent(name, newSpaceFunction);
    }

}
