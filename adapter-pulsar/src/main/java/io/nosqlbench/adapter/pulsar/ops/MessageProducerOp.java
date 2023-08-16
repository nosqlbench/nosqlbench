/*
 * Copyright (c) 2022-2023 nosqlbench
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

import com.codahale.metrics.Timer.Context;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterAsyncOperationFailedException;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapters.api.metrics.EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE;
import io.nosqlbench.adapters.api.metrics.MessageSequenceNumberSendingHandler;
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

    private static final Logger logger = LogManager.getLogger("MessageProducerOp");

    private final boolean useTransact;
    private final boolean seqTracking;
    private final Supplier<Transaction> transactSupplier;
    private final Set<MSG_SEQ_ERROR_SIMU_TYPE> errSimuTypeSet;
    private final Producer<?> producer;
    private final String msgKey;
    private final String msgPropRawJsonStr;
    private final String msgValue;

    private final Map<String, String> msgProperties = new HashMap<>();
    private final ThreadLocal<Map<String, MessageSequenceNumberSendingHandler>> MessageSequenceNumberSendingHandlersThreadLocal =
        ThreadLocal.withInitial(HashMap::new);

    public MessageProducerOp(final PulsarAdapterMetrics pulsarAdapterMetrics,
                             final PulsarClient pulsarClient,
                             final Schema<?> pulsarSchema,
                             final boolean asyncApi,
                             final boolean useTransact,
                             final boolean seqTracking,
                             final Supplier<Transaction> transactSupplier,
                             final Set<MSG_SEQ_ERROR_SIMU_TYPE> errSimuTypeSet,
                             final Producer<?> producer,
                             final String msgKey,
                             final String msgProp,
                             final String msgValue) {
        super(pulsarAdapterMetrics, pulsarClient, pulsarSchema, asyncApi);

        this.useTransact = useTransact;
        this.seqTracking = seqTracking;
        this.transactSupplier = transactSupplier;
        this.errSimuTypeSet = errSimuTypeSet;
        this.producer = producer;
        this.msgKey = msgKey;
        msgPropRawJsonStr = msgProp;
        this.msgValue = msgValue;

        this.getMsgPropMapFromRawJsonStr();
    }

    private MessageSequenceNumberSendingHandler getMessageSequenceNumberSendingHandler(final String topicName) {
        return this.MessageSequenceNumberSendingHandlersThreadLocal.get()
            .computeIfAbsent(topicName, k -> new MessageSequenceNumberSendingHandler());
    }

    // Check if msgPropJonStr is valid JSON string with a collection of key/value pairs
    // - if Yes, convert it to a map
    // - otherwise, log an error message and ignore message properties without throwing a runtime exception
    private void getMsgPropMapFromRawJsonStr() {
        if (!StringUtils.isBlank(this.msgPropRawJsonStr)) try {
            this.msgProperties.putAll(PulsarAdapterUtil.convertJsonToMap(this.msgPropRawJsonStr));
        } catch (final Exception e) {
            MessageProducerOp.logger.error(
                "Error parsing message property JSON string {}, ignore message properties!",
                this.msgPropRawJsonStr);
        }

        if (this.seqTracking) {
            final long nextSequenceNumber = this.getMessageSequenceNumberSendingHandler(this.producer.getTopic())
                .getNextSequenceNumber(this.errSimuTypeSet);
            this.msgProperties.put(PulsarAdapterUtil.MSG_SEQUENCE_NUMBER, String.valueOf(nextSequenceNumber));
        }
    }

    @Override
    public Object apply(final long value) {

        TypedMessageBuilder typedMessageBuilder;

        Transaction transaction;
        if (this.useTransact) {
            // if you are in a transaction you cannot set the schema per-message
            transaction = this.transactSupplier.get();
            typedMessageBuilder = this.producer.newMessage(transaction);
        }
        else {
            transaction = null;
            typedMessageBuilder = this.producer.newMessage(this.pulsarSchema);
        }

        // set message key
        if ( !StringUtils.isBlank(this.msgKey) && !(this.pulsarSchema instanceof KeyValueSchema) )
            typedMessageBuilder = typedMessageBuilder.key(this.msgKey);

        // set message properties
        if (!StringUtils.isBlank(this.msgPropRawJsonStr) || this.seqTracking)
            typedMessageBuilder = typedMessageBuilder.properties(this.msgProperties);

        // set message payload
        final int messageSize;
        final SchemaType schemaType = this.pulsarSchema.getSchemaInfo().getType();
        if (this.pulsarSchema instanceof KeyValueSchema keyValueSchema) {
            final org.apache.avro.Schema avroSchema = this.getAvroSchemaFromConfiguration();
            final GenericRecord payload = PulsarAvroSchemaUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) keyValueSchema.getValueSchema(),
                avroSchema,
                this.msgValue
            );

            final org.apache.avro.Schema avroSchemaForKey = this.getKeyAvroSchemaFromConfiguration();
            final GenericRecord key = PulsarAvroSchemaUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) keyValueSchema.getKeySchema(),
                avroSchemaForKey,
                this.msgKey
            );

            typedMessageBuilder = typedMessageBuilder.value(new KeyValue(key, payload));
            // TODO: add a way to calculate the message size for KEY_VALUE messages
            messageSize = this.msgKey.length() + this.msgValue.length();
        }
        else if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType.name())) {
            final GenericRecord payload = PulsarAvroSchemaUtil.GetGenericRecord_PulsarAvro(
                (GenericAvroSchema) this.pulsarSchema,
                this.pulsarSchema.getSchemaInfo().getSchemaDefinition(),
                this.msgValue
            );
            typedMessageBuilder = typedMessageBuilder.value(payload);
            // TODO: add a way to calculate the message size for AVRO messages
            messageSize = this.msgValue.length();
        } else {
            final byte[] array = this.msgValue.getBytes(StandardCharsets.UTF_8);
            typedMessageBuilder = typedMessageBuilder.value(array);
            messageSize = array.length;
        }

        this.messageSizeHistogram.update(messageSize);

        //TODO: add error handling with failed message production
        if (!this.asyncApi) try {
            MessageProducerOp.logger.trace("Sending message");
            typedMessageBuilder.send();

            if (this.useTransact) try (final Context ctx = this.transactionCommitTimer.time()) {
                transaction.commit().get();
            }

            if (MessageProducerOp.logger.isDebugEnabled())
                if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType.name())) {
                    final org.apache.avro.Schema avroSchema = this.getAvroSchemaFromConfiguration();
                    final org.apache.avro.generic.GenericRecord avroGenericRecord =
                        PulsarAvroSchemaUtil.GetGenericRecord_ApacheAvro(avroSchema, this.msgValue);

                    MessageProducerOp.logger.debug("({}) Sync message sent: msg-key={}; msg-properties={}; msg-payload={})",
                        this.producer.getProducerName(),
                        this.msgKey,
                        this.msgProperties,
                        avroGenericRecord.toString());
                } else
                    MessageProducerOp.logger.debug("({}) Sync message sent; msg-key={}; msg-properties={}; msg-payload={}",
                        this.producer.getProducerName(),
                        this.msgKey,
                        this.msgProperties,
                        this.msgValue);
        } catch (final PulsarClientException | ExecutionException | InterruptedException pce) {
            final String errMsg =
                "Sync message sending failed: " +
                    "key - " + this.msgKey + "; " +
                    "properties - " + this.msgProperties + "; " +
                    "payload - " + this.msgValue;

            MessageProducerOp.logger.trace(errMsg);

            throw new PulsarAdapterUnexpectedException(errMsg);
        }
        else try {
            // we rely on blockIfQueueIsFull in order to throttle the request in this case
            CompletableFuture<?> future = typedMessageBuilder.sendAsync();

            // add commit step
            if (this.useTransact) future = future.thenCompose(msg -> {
                    final Context ctx = this.transactionCommitTimer.time();
                    return transaction
                        .commit()
                        .whenComplete((m, e) -> ctx.close())
                        .thenApply(v -> msg);
                }
            );

            future.whenComplete((messageId, error) -> {
                if (MessageProducerOp.logger.isDebugEnabled())
                    if (PulsarAdapterUtil.isAvroSchemaTypeStr(schemaType.name())) {
                        final org.apache.avro.Schema avroSchema = this.getAvroSchemaFromConfiguration();
                        final org.apache.avro.generic.GenericRecord avroGenericRecord =
                            PulsarAvroSchemaUtil.GetGenericRecord_ApacheAvro(avroSchema, this.msgValue);

                        MessageProducerOp.logger.debug("({}) Aysnc message sent: msg-key={}; msg-properties={}; msg-payload={})",
                            this.producer.getProducerName(),
                            this.msgKey,
                            this.msgProperties,
                            avroGenericRecord.toString());
                    } else
                        MessageProducerOp.logger.debug("({}) Aysnc message sent: msg-key={}; msg-properties={}; msg-payload={}",
                            this.producer.getProducerName(),
                            this.msgKey,
                            this.msgProperties,
                            this.msgValue);
            }).exceptionally(ex -> {
                MessageProducerOp.logger.error("Async message sending failed: key - {}; properties - {}; payload - {}", this.msgKey, this.msgProperties, this.msgValue);

                throw new PulsarAdapterAsyncOperationFailedException(ex);
            });
        } catch (final Exception e) {
            throw new PulsarAdapterUnexpectedException(e);
        }

        return null;
    }
}
