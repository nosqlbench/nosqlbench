/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.pulsar.ops;

import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterAsyncOperationFailedException;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.util.MessageSequenceNumberSendingHandler;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.PulsarAvroSchemaUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class MessageProducerOp extends PulsarClientOp {

    private final static Logger logger = LogManager.getLogger("MessageProducerOp");

    private final boolean useTransact;
    private final boolean seqTracking;
    private final Supplier<Transaction> transactSupplier;
    private final Set<PulsarAdapterUtil.SEQ_ERROR_SIMU_TYPE> errSimuTypeSet;
    private final Producer<?> producer;
    private final String msgKey;
    private final String msgPropRawJsonStr;
    private final String msgValue;

    private final Map<String, String> msgProperties = new HashMap<>();
    private final ThreadLocal<Map<String, MessageSequenceNumberSendingHandler>> MessageSequenceNumberSendingHandlersThreadLocal =
        ThreadLocal.withInitial(HashMap::new);

    public MessageProducerOp(PulsarAdapterMetrics pulsarAdapterMetrics,
                             PulsarClient pulsarClient,
                             Schema<?> pulsarSchema,
                             boolean asyncApi,
                             boolean useTransact,
                             boolean seqTracking,
                             Supplier<Transaction> transactSupplier,
                             Set<PulsarAdapterUtil.SEQ_ERROR_SIMU_TYPE> errSimuTypeSet,
                             Producer<?> producer,
                             String msgKey,
                             String msgProp,
                             String msgValue) {
        super(pulsarAdapterMetrics, pulsarClient, pulsarSchema, asyncApi);

        this.useTransact = useTransact;
        this.seqTracking = seqTracking;
        this.transactSupplier = transactSupplier;
        this.errSimuTypeSet = errSimuTypeSet;
        this.producer = producer;
        this.msgKey = msgKey;
        this.msgPropRawJsonStr = msgProp;
        this.msgValue = msgValue;

        getMsgPropMapFromRawJsonStr();
    }

    private MessageSequenceNumberSendingHandler getMessageSequenceNumberSendingHandler(String topicName) {
        return MessageSequenceNumberSendingHandlersThreadLocal.get()
            .computeIfAbsent(topicName, k -> new MessageSequenceNumberSendingHandler());
    }

    // Check if msgPropJonStr is valid JSON string with a collection of key/value pairs
    // - if Yes, convert it to a map
    // - otherwise, log an error message and ignore message properties without throwing a runtime exception
    private void getMsgPropMapFromRawJsonStr() {
        if (!StringUtils.isBlank(msgPropRawJsonStr)) {
            try {
                msgProperties.putAll(PulsarAdapterUtil.convertJsonToMap(msgPropRawJsonStr));
            }
            catch (Exception e) {
                logger.error(
                    "Error parsing message property JSON string {}, ignore message properties!",
                    msgPropRawJsonStr);
            }
        }

        if (seqTracking) {
            long nextSequenceNumber = getMessageSequenceNumberSendingHandler(producer.getTopic())
                .getNextSequenceNumber(errSimuTypeSet);
            msgProperties.put(PulsarAdapterUtil.MSG_SEQUENCE_NUMBER, String.valueOf(nextSequenceNumber));
        }
    }

    @Override
    public Object apply(long value) {

        TypedMessageBuilder typedMessageBuilder;

        final Transaction transaction;
        if (useTransact) {
            // if you are in a transaction you cannot set the schema per-message
            transaction = transactSupplier.get();
            typedMessageBuilder = producer.newMessage(transaction);
        }
        else {
            transaction = null;
            typedMessageBuilder = producer.newMessage(pulsarSchema);
        }

        // set message key
        if ( !StringUtils.isBlank(msgKey) && !(pulsarSchema instanceof KeyValueSchema) ) {
            typedMessageBuilder = typedMessageBuilder.key(msgKey);
        }

        // set message properties
        if ( !msgPropRawJsonStr.isEmpty() ) {
            typedMessageBuilder = typedMessageBuilder.properties(msgProperties);
        }

        // set message payload
        int messageSize;
        SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();
        if (pulsarSchema instanceof KeyValueSchema) {

//            // {KEY IN JSON}||{VALUE IN JSON}
//            int separator = msgValue.indexOf("}||{");
//            if (separator < 0) {
//                throw new IllegalArgumentException("KeyValue payload MUST be in form {KEY IN JSON}||{VALUE IN JSON} (with 2 pipes that separate the KEY part from the VALUE part)");
//            }
//            String keyInput = msgValue.substring(0, separator + 1);
//            String valueInput = msgValue.substring(separator + 3);

            KeyValueSchema keyValueSchema = (KeyValueSchema) pulsarSchema;
            org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
            GenericRecord payload = PulsarAvroSchemaUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) keyValueSchema.getValueSchema(),
                avroSchema,
                msgValue
            );

            org.apache.avro.Schema avroSchemaForKey = getKeyAvroSchemaFromConfiguration();
            GenericRecord key = PulsarAvroSchemaUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) keyValueSchema.getKeySchema(),
                avroSchemaForKey,
                msgKey
            );

            typedMessageBuilder = typedMessageBuilder.value(new KeyValue(key, payload));
            // TODO: add a way to calculate the message size for KEY_VALUE messages
            messageSize = msgKey.length() + msgValue.length();
        }
        else if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType.name())) {
            GenericRecord payload = PulsarAvroSchemaUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) pulsarSchema,
                pulsarSchema.getSchemaInfo().getSchemaDefinition(),
                msgValue
            );
            typedMessageBuilder = typedMessageBuilder.value(payload);
            // TODO: add a way to calculate the message size for AVRO messages
            messageSize = msgValue.length();
        } else {
            byte[] array = msgValue.getBytes(StandardCharsets.UTF_8);
            typedMessageBuilder = typedMessageBuilder.value(array);
            messageSize = array.length;
        }

        messageSizeHistogram.update(messageSize);

        //TODO: add error handling with failed message production
        if (!asyncApi) {
            try {
                logger.trace("Sending message");
                typedMessageBuilder.send();

                if (useTransact) {
                    try (Timer.Context ctx = transactionCommitTimer.time()) {
                        transaction.commit().get();
                    }
                }

                if (logger.isDebugEnabled()) {
                    if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType.name())) {
                        org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
                        org.apache.avro.generic.GenericRecord avroGenericRecord =
                            PulsarAvroSchemaUtil.GetGenericRecord_ApacheAvro(avroSchema, msgValue);

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
                            msgValue);
                    }
                }
            }
            catch (PulsarClientException | ExecutionException | InterruptedException pce) {
                String errMsg =
                    "Sync message sending failed: " +
                        "key - " + msgKey + "; " +
                        "properties - " + msgProperties + "; " +
                        "payload - " + msgValue;

                logger.trace(errMsg);

                throw new PulsarAdapterUnexpectedException(errMsg);
            }
        }
        else {
            try {
                // we rely on blockIfQueueIsFull in order to throttle the request in this case
                CompletableFuture<?> future = typedMessageBuilder.sendAsync();

                if (useTransact) {
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
                        if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType.name())) {
                            org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
                            org.apache.avro.generic.GenericRecord avroGenericRecord =
                                PulsarAvroSchemaUtil.GetGenericRecord_ApacheAvro(avroSchema, msgValue);

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
                                msgValue);
                        }
                    }
                }).exceptionally(ex -> {
                    logger.error("Async message sending failed: " +
                        "key - " + msgKey + "; " +
                        "properties - " + msgProperties + "; " +
                        "payload - " + msgValue);

                    throw new PulsarAdapterAsyncOperationFailedException(ex);
                });
            }
            catch (Exception e) {
                throw new PulsarAdapterUnexpectedException(e);
            }
        }

        return null;
    }
}
