package io.nosqlbench.adapter.cqld4;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintain a cache of objects to use in a CQLD4 context.
 */
public class Cqld4SpaceCache {
    private final ConcurrentHashMap<String, Cqld4Space> clientscopes = new ConcurrentHashMap<>();
    private final Cqld4DriverAdapter adapter;

    public Cqld4SpaceCache(Cqld4DriverAdapter adapter) {
        this.adapter = adapter;
    }

    public Cqld4Space getSpace(String name) {
        return clientscopes.computeIfAbsent(name, newName -> {
            return new Cqld4Space(adapter);
        });
    }
}
