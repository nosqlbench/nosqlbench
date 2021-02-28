package io.nosqlbench.driver.pulsar;

import io.nosqlbench.driver.pulsar.util.PulsarNBClientConf;
import org.apache.pulsar.client.api.Reader;

import java.util.concurrent.ConcurrentHashMap;

public class PulsarReaderSpace extends PulsarSpace {

    private final ConcurrentHashMap<String, Reader<?>> readers = new ConcurrentHashMap<>();

    public PulsarReaderSpace(String name, PulsarNBClientConf pulsarClientConf) {
        super(name, pulsarClientConf);
    }
}
