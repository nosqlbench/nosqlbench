package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;

public class PulsarConsumerOp implements PulsarOp {
    private final Consumer<?> consumer;
    private final Schema<?> pulsarSchema;

    public PulsarConsumerOp(Consumer<?> consumer, Schema<?> schema) {
        this.consumer = consumer;
        this.pulsarSchema = schema;
    }

    @Override
    public void run() {
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
}
