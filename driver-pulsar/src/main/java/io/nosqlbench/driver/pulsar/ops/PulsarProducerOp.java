package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarAction;
import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class PulsarProducerOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarProducerOp.class);

    private final Producer<?> producer;
    private final Schema<?> pulsarSchema;
    private final String msgKey;
    private final String msgPayload;
    private final boolean asyncPulsarOp;

    public PulsarProducerOp(Producer<?> producer,
                            Schema<?> schema,
                            boolean asyncPulsarOp,
                            String key,
                            String payload) {
        this.producer = producer;
        this.pulsarSchema = schema;
        this.msgKey = key;
        this.msgPayload = payload;
        this.asyncPulsarOp = asyncPulsarOp;
    }

    @Override
    public void run() {
        if ( (msgPayload == null) || msgPayload.isEmpty() ) {
            throw new RuntimeException("Message payload (\"msg-value\") can't be empty!");
        }

        TypedMessageBuilder typedMessageBuilder = producer.newMessage(pulsarSchema);
        if ( (msgKey != null) && (!msgKey.isEmpty()) ) {
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
        }
        else {
            typedMessageBuilder = typedMessageBuilder.value(msgPayload.getBytes(StandardCharsets.UTF_8));
        }

        //TODO: add error handling with failed message production
        if (!asyncPulsarOp) {
            try {
                logger.trace("sending message");
                typedMessageBuilder.send();
            } catch (PulsarClientException pce) {
                logger.trace("failed sending message");
                throw new RuntimeException(pce);
            }
        }
        else {
            try {
                CompletableFuture<MessageId> future = typedMessageBuilder.sendAsync();
                future.get();

            /*.thenRun(() -> {
//                System.out.println("Producing message succeeded: key - " + msgKey + "; payload - " + msgPayload);
            }).exceptionally(ex -> {
                System.out.println("Producing message failed: key - " + msgKey + "; payload - " + msgPayload);
                return ex;
            })*/
                ;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
