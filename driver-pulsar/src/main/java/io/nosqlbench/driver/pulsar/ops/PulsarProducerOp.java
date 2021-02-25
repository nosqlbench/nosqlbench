package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;

public class PulsarProducerOp implements PulsarOp {
    private final Producer<?> producer;
    private final Schema<?> pulsarSchema;
    private final String msgKey;
    private final String msgPayload;

    public PulsarProducerOp(Producer<?> producer, Schema<?> schema, String key, String payload) {
        this.producer = producer;
        this.pulsarSchema = schema;
        this.msgKey = key;
        this.msgPayload = payload;
    }

    @Override
    public void run() {
        try {
            if ( (msgPayload == null) || msgPayload.isEmpty() ) {
                throw new RuntimeException("Message payload (\"msg-value\" can't be empty!");
            }

            TypedMessageBuilder typedMessageBuilder = producer.newMessage(pulsarSchema);
            if ( (msgKey != null) && (!msgKey.isEmpty()) ) {
                typedMessageBuilder = typedMessageBuilder.key(msgKey);
            }

            SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
            if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                org.apache.avro.generic.GenericRecord avroGenericRecord =
                    AvroUtil.GetGenericRecord_ApacheAvro(avroDefStr, msgPayload);

                GenericRecord payload = AvroUtil.GetGenericRecord_PulsarAvro(
                    (GenericAvroSchema) pulsarSchema, avroGenericRecord);

                typedMessageBuilder = typedMessageBuilder.value(payload);
            }
            else {
                typedMessageBuilder = typedMessageBuilder.value(msgPayload.getBytes(StandardCharsets.UTF_8));
            }

            typedMessageBuilder.send();

        } catch (PulsarClientException e) {
            throw new RuntimeException(e);
        }
    }
}
