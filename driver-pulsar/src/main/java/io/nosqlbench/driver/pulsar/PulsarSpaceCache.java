package io.nosqlbench.driver.pulsar;

import org.apache.pulsar.client.api.PulsarClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * To enable flexibility in testing methods, each object graph which is used within
 * the pulsar API is kept within a single umbrella called the PulsarSpace.
 * This allows for clients, producers, and consumers to remain connected and
 * cached in a useful way.
 */
public class PulsarSpaceCache {

    // TODO: Implement cache limits
    // TODO: Implement variant cache eviction behaviors (halt, warn, LRU)

    private final PulsarActivity activity;
    private final Supplier<PulsarClient> clientFunc;
    private final ConcurrentHashMap<String, PulsarSpace> clientScopes = new ConcurrentHashMap<>();

    public PulsarSpaceCache(PulsarActivity pulsarActivity, Supplier<PulsarClient> newClient) {
        this.activity = pulsarActivity;
        this.clientFunc = newClient;
    }

    public PulsarSpace getClientSpace(String name) {
        PulsarSpace cspace = clientScopes.computeIfAbsent(name, spaceName -> new PulsarSpace(spaceName, clientFunc));
        return cspace;
    }

    public PulsarActivity getActivity() {
        return activity;
    }


}
