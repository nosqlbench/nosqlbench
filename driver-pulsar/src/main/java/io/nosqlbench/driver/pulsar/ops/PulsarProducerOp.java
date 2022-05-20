package io.nosqlbench.driver.pulsar.ops;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
import org.apache.pulsar.client.api.schema.KeyValueSchema;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.KeyValue;
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

    private org.apache.avro.Schema avroSchema;
    private org.apache.avro.Schema avroKeySchema;

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
        if (pulsarSchema instanceof KeyValueSchema) {

            // {KEY IN JSON}||{VALUE IN JSON}
            int separator = msgPayload.indexOf("}||{");
            if (separator < 0) {
                throw new IllegalArgumentException("KeyValue payload MUST be in form {KEY IN JSON}||{VALUE IN JSON} (with 2 pipes that separate the KEY part from the VALUE part)");
            }
            String keyInput = msgPayload.substring(0, separator + 1);
            String valueInput = msgPayload.substring(separator + 3);

            KeyValueSchema keyValueSchema = (KeyValueSchema) pulsarSchema;
            org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
            GenericRecord payload = AvroUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) keyValueSchema.getValueSchema(),
                avroSchema,
                valueInput
            );

            org.apache.avro.Schema avroSchemaForKey = getKeyAvroSchemaFromConfiguration();
            GenericRecord key = AvroUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) keyValueSchema.getKeySchema(),
                avroSchemaForKey,
                keyInput
            );
            typedMessageBuilder = typedMessageBuilder.value(new KeyValue(key, payload));
            // TODO: add a way to calculate the message size for KEY_VALUE messages
            messageSize = msgPayload.length();
        } else if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
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
                        org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
                        org.apache.avro.generic.GenericRecord avroGenericRecord =
                            AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, msgPayload);

                        logger.debug("({}) Sync message sent: msg-key={}; msg-properties={}; msg-payload={})",
                            producer.getProducerName(),
                            msgKey,
                            msgProperties,
                            avroGenericRecord.toString());
                    }
                    else {
                        logger.debug("({}) Sync message sent; msg-key={}; msg-properties={}; msg-payload={}",
                            producer.getProducerName(),
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
                            org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
                            org.apache.avro.generic.GenericRecord avroGenericRecord =
                                AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, msgPayload);

                            logger.debug("({}) Aysnc message sent: msg-key={}; msg-properties={}; msg-payload={})",
                                producer.getProducerName(),
                                msgKey,
                                msgProperties,
                                avroGenericRecord.toString());
                        }
                        else {
                            logger.debug("({}) Aysnc message sent: msg-key={}; msg-properties={}; msg-payload={}",
                                producer.getProducerName(),
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

    private org.apache.avro.Schema getAvroSchemaFromConfiguration() {
        // no need for synchronization, this is only a cache
        // in case of the race we will parse the string twice, not a big
        if (avroSchema == null) {
            if (pulsarSchema.getSchemaInfo().getType() == SchemaType.KEY_VALUE) {
                KeyValueSchema kvSchema = (KeyValueSchema) pulsarSchema;
                Schema valueSchema = kvSchema.getValueSchema();
                String avroDefStr = valueSchema.getSchemaInfo().getSchemaDefinition();
                avroSchema = AvroUtil.GetSchema_ApacheAvro(avroDefStr);
            } else {
                String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
                avroSchema = AvroUtil.GetSchema_ApacheAvro(avroDefStr);
            }
        }
        return avroSchema;
    }

    private org.apache.avro.Schema getKeyAvroSchemaFromConfiguration() {
        // no need for synchronization, this is only a cache
        // in case of the race we will parse the string twice, not a big
        if (avroKeySchema == null) {
            if (pulsarSchema.getSchemaInfo().getType() == SchemaType.KEY_VALUE) {
                KeyValueSchema kvSchema = (KeyValueSchema) pulsarSchema;
                Schema keySchema = kvSchema.getKeySchema();
                String avroDefStr = keySchema.getSchemaInfo().getSchemaDefinition();
                avroKeySchema = AvroUtil.GetSchema_ApacheAvro(avroDefStr);
            } else {
                throw new RuntimeException("We are not using KEY_VALUE schema, so no Schema for the Key!");
            }
        }
        return avroKeySchema;
    }
}
