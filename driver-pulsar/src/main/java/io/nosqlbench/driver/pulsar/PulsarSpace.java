package io.nosqlbench.driver.pulsar;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * An instance of a pulsar client, along with all the cached objects which are normally
 * associated with it during a client session in a typical application.
 * A PulsarSpace is simply a named and cached set of objects which must be used together.
 */
public class PulsarSpace {
    private final String name;

    private final Supplier<PulsarClient> clientFunc;
    private final ConcurrentHashMap<String, Producer<?>> producers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Consumer<?>> consumers = new ConcurrentHashMap<>();

    private PulsarClient _client;

    public PulsarSpace(String name, Supplier<PulsarClient> clientFunc) {
        this.name = name;
        this.clientFunc = clientFunc;
    }

    public PulsarClient getClient() {
        if (_client == null) {
            _client = clientFunc.get();
        }
        return _client;
    }

    public Producer<?> getProducer(String pname, String topicName) {
        Producer<?> producer = producers.computeIfAbsent(
            pname, n -> {
                try {
                    // TODO: parameterize producer settings
                    return getClient().newProducer().topic(topicName).create();
                } catch (PulsarClientException e) {
                    throw new RuntimeException(e);
                }
            }
        );
        return producer;
    }

    public Consumer<?> getConsumer(String pname, String topicName) {
        Consumer<?> consumer = consumers.computeIfAbsent(
            pname, n -> {
                try {
                    // TODO: parameterize subscription name and other settings
                    return getClient().newConsumer().topic(topicName).subscriptionName("testsub").subscribe();
                } catch (PulsarClientException e) {
                    throw new RuntimeException(e);
                }
            }
        );
        return consumer;
    }
}
