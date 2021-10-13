package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverParamException;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverUnexpectedException;
import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaType;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class PulsarProducerOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarProducerOp.class);

    private final PulsarActivity pulsarActivity;

    private final boolean asyncPulsarOp;
    private final boolean useTransaction;
    private final Supplier<Transaction> transactionSupplier;

    private final Producer<?> producer;
    private final Schema<?> pulsarSchema;
    private final String msgKey;
    private final Map<String, String> msgProperties;
    private final String msgPayload;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;
    private final Timer transactionCommitTimer;

    public PulsarProducerOp( PulsarActivity pulsarActivity,
                             boolean asyncPulsarOp,
                             boolean useTransaction,
                             Supplier<Transaction> transactionSupplier,
                             Producer<?> producer,
                             Schema<?> schema,
                             String key,
                             Map<String, String> msgProperties,
                             String payload) {
        this.pulsarActivity = pulsarActivity;

        this.asyncPulsarOp = asyncPulsarOp;
        this.useTransaction = useTransaction;
        this.transactionSupplier = transactionSupplier;

        this.producer = producer;
        this.pulsarSchema = schema;
        this.msgKey = key;
        this.msgProperties = msgProperties;
        this.msgPayload = payload;

        this.bytesCounter = pulsarActivity.getBytesCounter();
        this.messageSizeHistogram = pulsarActivity.getMessageSizeHistogram();
        this.transactionCommitTimer = pulsarActivity.getCommitTransactionTimer();
    }

    @Override
    public void run(Runnable timeTracker) {
        if ( StringUtils.isBlank(msgPayload)) {
            throw new PulsarDriverParamException("Message payload (\"msg-value\") can't be empty!");
        }

        TypedMessageBuilder typedMessageBuilder;

        final Transaction transaction;
        if (useTransaction) {
            // if you are in a transaction you cannot set the schema per-message
            transaction = transactionSupplier.get();
            typedMessageBuilder = producer.newMessage(transaction);
        }
        else {
            transaction = null;
            typedMessageBuilder = producer.newMessage(pulsarSchema);
        }

        // set message key
        if (!StringUtils.isBlank(msgKey)) {
            typedMessageBuilder = typedMessageBuilder.key(msgKey);
        }

        // set message properties
        if ( !msgProperties.isEmpty() ) {
            typedMessageBuilder = typedMessageBuilder.properties(msgProperties);
        }

        // set message payload
        int messageSize;
        SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
            GenericRecord payload = AvroUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) pulsarSchema,
                pulsarSchema.getSchemaInfo().getSchemaDefinition(),
                msgPayload
            );
            typedMessageBuilder = typedMessageBuilder.value(payload);
            // TODO: add a way to calculate the message size for AVRO messages
            messageSize = msgPayload.length();
        } else {
            byte[] array = msgPayload.getBytes(StandardCharsets.UTF_8);
            typedMessageBuilder = typedMessageBuilder.value(array);
            messageSize = array.length;
        }
        messageSizeHistogram.update(messageSize);
        bytesCounter.inc(messageSize);

        //TODO: add error handling with failed message production
        if (!asyncPulsarOp) {
            try {
                logger.trace("Sending message");
                typedMessageBuilder.send();

                if (useTransaction) {
                    try (Timer.Context ctx = transactionCommitTimer.time()) {
                        transaction.commit().get();
                    }
                }

                if (logger.isDebugEnabled()) {
                    if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                        String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                        org.apache.avro.Schema avroSchema =
                            AvroUtil.GetSchema_ApacheAvro(avroDefStr);
                        org.apache.avro.generic.GenericRecord avroGenericRecord =
                            AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, msgPayload);

                        logger.debug("Sync message sent: msg-key={}; msg-properties={}; msg-payload={})",
                            msgKey,
                            msgProperties,
                            avroGenericRecord.toString());
                    }
                    else {
                        logger.debug("Sync message sent: msg-key={}; msg-properties={}; msg-payload={}",
                            msgKey,
                            msgProperties,
                            msgPayload);
                    }
                }
            }
            catch (PulsarClientException | ExecutionException | InterruptedException pce) {
                String errMsg =
                    "Sync message sending failed: " +
                    "key - " + msgKey + "; " +
                    "properties - " + msgProperties + "; " +
                    "payload - " + msgPayload;

                logger.trace(errMsg);

                throw new PulsarDriverUnexpectedException(errMsg);
            }

            timeTracker.run();
        }
        else {
            try {
                // we rely on blockIfQueueIsFull in order to throttle the request in this case
                CompletableFuture<?> future = typedMessageBuilder.sendAsync();

                if (useTransaction) {
                    // add commit step
                    future = future.thenCompose(msg -> {
                        Timer.Context ctx = transactionCommitTimer.time();
                        return transaction
                            .commit()
                            .whenComplete((m,e) -> ctx.close())
                            .thenApply(v-> msg);
                        }
                    );
                }

                future.whenComplete((messageId, error) -> {
                    if (logger.isDebugEnabled()) {
                        if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                            String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                            org.apache.avro.Schema avroSchema =
                                AvroUtil.GetSchema_ApacheAvro(avroDefStr);
                            org.apache.avro.generic.GenericRecord avroGenericRecord =
                                AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, msgPayload);

                            logger.debug("Aysnc message sent: msg-key={}; msg-properties={}; msg-payload={})",
                                msgKey,
                                msgProperties,
                                avroGenericRecord.toString());
                        }
                        else {
                            logger.debug("Aysnc message sent: msg-key={}; msg-properties={}; msg-payload={}",
                                msgKey,
                                msgProperties,
                                msgPayload);
                        }
                    }

                    timeTracker.run();
                }).exceptionally(ex -> {
                    logger.error("Async message sending failed: " +
                        "key - " + msgKey + "; " +
                        "properties - " + msgProperties + "; " +
                        "payload - " + msgPayload);

                    pulsarActivity.asyncOperationFailed(ex);
                    return null;
                });
            }
            catch (Exception e) {
                throw new PulsarDriverUnexpectedException(e);
            }
        }
    }
}
