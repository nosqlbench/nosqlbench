package io.nosqlbench.driver.pulsar.ops;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClientException;

import java.nio.charset.StandardCharsets;

public class PulsarSendOp implements PulsarOp {
    private final Producer<byte[]> producer;
    private final String msg;

    public PulsarSendOp(Producer<byte[]> producer, String msg) {
        this.producer = producer;
        this.msg = msg;
    }

    @Override
    public void run() {
        try {
            MessageId mid = producer.send(msg.getBytes(StandardCharsets.UTF_8));
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
    }
}
