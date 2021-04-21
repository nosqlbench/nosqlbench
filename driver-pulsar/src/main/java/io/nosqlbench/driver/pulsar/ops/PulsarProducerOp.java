package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class PulsarProducerOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarProducerOp.class);

    private final Producer<?> producer;
    private final Schema<?> pulsarSchema;
    private final String msgKey;
    private final String msgPayload;
    private final boolean asyncPulsarOp;
    private final Counter bytesCounter;
    private final Histogram messagesizeHistogram;
    private final PulsarActivity pulsarActivity;
    private final boolean useTransaction;
    private final Supplier<Transaction> transactionSupplier;

    public PulsarProducerOp(Producer<?> producer,
                            Schema<?> schema,
                            boolean asyncPulsarOp,
                            boolean useTransaction,
                            Supplier<Transaction> transactionSupplier,
                            String key,
                            String payload,
                            PulsarActivity pulsarActivity) {
        this.producer = producer;
        this.pulsarSchema = schema;
        this.msgKey = key;
        this.msgPayload = payload;
        this.asyncPulsarOp = asyncPulsarOp;
        this.pulsarActivity = pulsarActivity;
        this.bytesCounter = pulsarActivity.getBytesCounter();
        this.messagesizeHistogram = pulsarActivity.getMessagesizeHistogram();
        this.useTransaction = useTransaction;
        this.transactionSupplier = transactionSupplier;
    }

    @Override
    public void run(Runnable timeTracker) {
        if ((msgPayload == null) || msgPayload.isEmpty()) {
            throw new RuntimeException("Message payload (\"msg-value\") can't be empty!");
        }
        TypedMessageBuilder typedMessageBuilder;
        final Transaction transaction;
        if (useTransaction) {
            // if you are in a transaction you cannot set the schema per-message
            transaction = transactionSupplier.get();
            typedMessageBuilder = producer.newMessage(transaction);
        } else {
            transaction = null;
            typedMessageBuilder = producer.newMessage(pulsarSchema);
        }
        if ((msgKey != null) && (!msgKey.isEmpty())) {
            typedMessageBuilder = typedMessageBuilder.key(msgKey);
        }

        int messagesize;
        SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
            GenericRecord payload = AvroUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) pulsarSchema,
                pulsarSchema.getSchemaInfo().getSchemaDefinition(),
                msgPayload
            );
            typedMessageBuilder = typedMessageBuilder.value(payload);
            // TODO: add a way to calculate the message size for AVRO messages
            messagesize = msgPayload.length();
        } else {
            byte[] array = msgPayload.getBytes(StandardCharsets.UTF_8);
            typedMessageBuilder = typedMessageBuilder.value(array);
            messagesize = array.length;
        }
        messagesizeHistogram.update(messagesize);
        bytesCounter.inc(messagesize);

        //TODO: add error handling with failed message production
        if (!asyncPulsarOp) {
            try {
                logger.trace("sending message");
                typedMessageBuilder.send();
                if (useTransaction) {
                    try (Timer.Context ctx = pulsarActivity.getCommitTransactionTimer().time();) {
                        transaction.commit().get();
                    }
                }
            } catch (PulsarClientException | ExecutionException | InterruptedException pce) {
                logger.trace("failed sending message");
                throw new RuntimeException(pce);
            }
            timeTracker.run();
        } else {
            try {
                // we rely on blockIfQueueIsFull in order to throttle the request in this case
                CompletableFuture<?> future = typedMessageBuilder.sendAsync();
                if (useTransaction) {
                    // add commit step
                    future = future.thenCompose(msg -> {
                        Timer.Context ctx = pulsarActivity.getCommitTransactionTimer().time();;
                        return transaction
                            .commit()
                            .whenComplete((m,e) -> {
                                ctx.close();
                            })
                            .thenApply(v-> msg);
                        }
                    );
                }
                future.whenComplete((messageId, error) -> {
                    timeTracker.run();
                }).exceptionally(ex -> {
                    logger.error("Producing message failed: key - " + msgKey + "; payload - " + msgPayload);
                    pulsarActivity.asyncOperationFailed(ex);
                    return null;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
