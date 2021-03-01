package io.nosqlbench.driver.pulsar;

import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;

import java.util.concurrent.ConcurrentHashMap;

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
    private final ConcurrentHashMap<String, PulsarSpace> clientScopes = new ConcurrentHashMap<>();

    public PulsarSpaceCache(PulsarActivity pulsarActivity) {
        this.activity = pulsarActivity;
    }

    public PulsarSpace getPulsarSpace(String name) {

        String pulsarClientType = activity.getPulsarConf().getPulsarClientType();

        if (pulsarClientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.PRODUCER.toString())) {
            return clientScopes.computeIfAbsent(name, spaceName -> new PulsarProducerSpace(spaceName, activity.getPulsarConf()));
        }
        else if (pulsarClientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.CONSUMER.toString())) {
            return clientScopes.computeIfAbsent(name, spaceName -> new PulsarConsumerSpace(spaceName, activity.getPulsarConf()));
        }
        else if (pulsarClientType.equalsIgnoreCase(PulsarActivityUtil.CLIENT_TYPES.READER.toString())) {
            return clientScopes.computeIfAbsent(name, spaceName -> new PulsarReaderSpace(spaceName, activity.getPulsarConf()));
        }
        // TODO: add support for websocket-producer and managed-ledger
        else {
            throw new RuntimeException("Unsupported Pulsar client type: " + pulsarClientType);
        }
    }

    public PulsarActivity getActivity() {
        return activity;
    }
}
