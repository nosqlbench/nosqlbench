package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TypedMessageBuilder;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PulsarBatchProducerOp implements PulsarOp {

    private final Schema<?> pulsarSchema;
    private final String msgKey;
    private final String msgPayload;

    public PulsarBatchProducerOp(Schema<?> schema,
                                 String key,
                                 String payload) {
        this.pulsarSchema = schema;
        this.msgKey = key;
        this.msgPayload = payload;
    }


    @Override
    public void run() {
        if ((msgPayload == null) || msgPayload.isEmpty()) {
            throw new RuntimeException("Message payload (\"msg-value\") can't be empty!");
        }

        List<CompletableFuture<MessageId>> container = PulsarBatchProducerStartOp.threadLocalBatchMsgContainer.get();
        Producer<?> producer = PulsarBatchProducerStartOp.threadLocalProducer.get();
        assert (producer != null) && (container != null);

        TypedMessageBuilder typedMessageBuilder = producer.newMessage(pulsarSchema);
        if ((msgKey != null) && (!msgKey.isEmpty())) {
            typedMessageBuilder = typedMessageBuilder.key(msgKey);
        }

        SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
            GenericRecord payload = AvroUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) pulsarSchema,
                pulsarSchema.getSchemaInfo().getSchemaDefinition(),
                msgPayload
            );
            typedMessageBuilder = typedMessageBuilder.value(payload);
        } else {
            typedMessageBuilder = typedMessageBuilder.value(msgPayload.getBytes(StandardCharsets.UTF_8));
        }

        container.add(typedMessageBuilder.sendAsync());
    }
}
