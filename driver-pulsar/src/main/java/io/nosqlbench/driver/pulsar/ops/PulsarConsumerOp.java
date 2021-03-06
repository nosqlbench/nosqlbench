package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.common.schema.SchemaType;

public class PulsarConsumerOp implements PulsarOp {
    private final Consumer<?> consumer;
    private final Schema<?> pulsarSchema;
    private final boolean asyncPulsarOp;

    public PulsarConsumerOp(Consumer<?> consumer, Schema<?> schema, boolean asyncPulsarOp) {
        this.consumer = consumer;
        this.pulsarSchema = schema;
        this.asyncPulsarOp = asyncPulsarOp;
    }

    public void syncConsume() {
        try {
            Message<?> message = consumer.receive();

            SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
            if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                org.apache.avro.generic.GenericRecord avroGenericRecord =
                    AvroUtil.GetGenericRecord_ApacheAvro(avroDefStr, message.getData());

                System.out.println("msg-key=" + message.getKey() + "  msg-payload=" + avroGenericRecord.toString());
            } else {
                System.out.println("msg-key=" + message.getKey() + "  msg-payload=" + new String(message.getData()));
            }

            consumer.acknowledge(message.getMessageId());
        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
    }

    public void asyncConsume() {
        //TODO: add support for async consume
    }

    @Override
    public void run() {
        if (!asyncPulsarOp)
            syncConsume();
        else
            asyncConsume();
    }
}
