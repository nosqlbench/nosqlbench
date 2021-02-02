package io.nosqlbench.driver.pulsar.ops;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

import java.nio.charset.StandardCharsets;

public class PulsarRecvOp implements PulsarOp {
    private final Consumer<byte[]> consumer;
    private final String recvInstructions;

    public PulsarRecvOp(Consumer<byte[]> consumer, String recvInstructions) {
        this.consumer = consumer;
        this.recvInstructions = recvInstructions;
    }

    @Override
    public void run() {
        try {
            Message<byte[]> msgbytes = consumer.receive();
            // TODO: Parameterize the actions taken on a received message
            // TODO: Properly parameterize all pulsar Op types as with Producer<T> and Consumer<T>
            System.out.println("received:" + new String(msgbytes.getValue(), StandardCharsets.UTF_8));
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
    }
}
