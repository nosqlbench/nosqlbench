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
import io.nosqlbench.adapter.pulsar.util.*;
import io.nosqlbench.adapters.api.metrics.ReceivedMessageSequenceTracker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.shade.org.apache.avro.AvroRuntimeException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageConsumerOp extends PulsarClientOp {

    private static final Logger logger = LogManager.getLogger(MessageConsumerOp.class);

    private final boolean useTransact;
    private final boolean seqTracking;
    private final Supplier<Transaction> transactSupplier;
    private final String payloadRttField;
    private final EndToEndStartingTimeSource e2eStartingTimeSrc;
    private final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic;
    private final Consumer<?> consumer;
    private final int consumerTimeoutInSec;

    public MessageConsumerOp(final PulsarAdapterMetrics pulsarAdapterMetrics,
                             final PulsarClient pulsarClient,
                             final Schema<?> pulsarSchema,
                             final boolean asyncApi,
                             final boolean useTransact,
                             final boolean seqTracking,
                             final Supplier<Transaction> transactSupplier,
                             final String payloadRttField,
                             final EndToEndStartingTimeSource e2eStartingTimeSrc,
                             final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic,
                             final Consumer<?> consumer,
                             final int consumerTimeoutInSec) {
        super(pulsarAdapterMetrics, pulsarClient, pulsarSchema, asyncApi);

        this.useTransact = useTransact;
        this.seqTracking = seqTracking;
        this.transactSupplier = transactSupplier;
        this.payloadRttField = payloadRttField;
        this.e2eStartingTimeSrc = e2eStartingTimeSrc;
        this.receivedMessageSequenceTrackerForTopic = receivedMessageSequenceTrackerForTopic;
        this.consumer = consumer;
        this.consumerTimeoutInSec = consumerTimeoutInSec;
    }

    @Override
    public Object apply(final long value) {
        Transaction transaction;
        // if you are in a transaction you cannot set the schema per-message
        if (this.useTransact) transaction = this.transactSupplier.get();
        else transaction = null;

        if (!this.asyncApi) try {
            final Message<?> message;

            // wait forever
            if (0 >= consumerTimeoutInSec) message = this.consumer.receive();
            else {
                message = this.consumer.receive(this.consumerTimeoutInSec, TimeUnit.SECONDS);
                if (null == message) if (MessageConsumerOp.logger.isDebugEnabled())
                    MessageConsumerOp.logger.debug("Failed to sync-receive a message before time out ({} seconds)", this.consumerTimeoutInSec);
            }

            this.handleMessage(transaction, message);
        } catch (final Exception e) {
            throw new PulsarAdapterUnexpectedException("Sync message receiving failed - timeout value: " + this.consumerTimeoutInSec + " seconds ");
        }
        else try {
            CompletableFuture<? extends Message<?>> msgRecvFuture = this.consumer.receiveAsync();
            // add commit step
            if (this.useTransact) msgRecvFuture = msgRecvFuture.thenCompose(msg -> {
                    final Context ctx = this.transactionCommitTimer.time();
                    return transaction
                        .commit()
                        .whenComplete((m, e) -> ctx.close())
                        .thenApply(v -> msg);
                }
            );

            msgRecvFuture.thenAccept(message -> {
                try {
                    this.handleMessage(transaction, message);
                } catch (final PulsarClientException | TimeoutException e) {
                    throw new PulsarAdapterAsyncOperationFailedException(e);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException e) {
                    throw new PulsarAdapterAsyncOperationFailedException(e.getCause());
                }
            }).exceptionally(ex -> {
                throw new PulsarAdapterAsyncOperationFailedException(ex);
            });
        } catch (final Exception e) {
            throw new PulsarAdapterUnexpectedException(e);
        }

        return null;
    }

    private void handleMessage(final Transaction transaction, final Message<?> message)
        throws PulsarClientException, InterruptedException, ExecutionException, TimeoutException {

        // acknowledge the message as soon as possible
        if (!this.useTransact) this.consumer.acknowledgeAsync(message.getMessageId())
            .get(this.consumerTimeoutInSec, TimeUnit.SECONDS);
        else {
            this.consumer.acknowledgeAsync(message.getMessageId(), transaction)
                .get(this.consumerTimeoutInSec, TimeUnit.SECONDS);

            // little problem: here we are counting the "commit" time
            // inside the overall time spent for the execution of the consume operation
            // we should refactor this operation as for PulsarProducerOp, and use the passed callback
            // to track with precision the time spent for the operation and for the commit
            try (final Context ctx = this.transactionCommitTimer.time()) {
                transaction.commit().get();
            }
        }

        if (MessageConsumerOp.logger.isDebugEnabled()) {
            final Object decodedPayload = message.getValue();
            if (decodedPayload instanceof GenericObject object) {
                // GenericObject is a wrapper for Primitives, for AVRO/JSON structs and for KeyValu
                // we fall here with a configured AVRO schema or with AUTO_CONSUME
                MessageConsumerOp.logger.debug("({}) message received: msg-key={}; msg-properties={}; msg-payload={}",
                    this.consumer.getConsumerName(),
                    message.getKey(),
                    message.getProperties(),
                    String.valueOf(object.getNativeObject()));
            }
            else MessageConsumerOp.logger.debug("({}) message received: msg-key={}; msg-properties={}; msg-payload={}",
                this.consumer.getConsumerName(),
                message.getKey(),
                message.getProperties(),
                new String(message.getData(), StandardCharsets.UTF_8));
        }

        if (!this.payloadRttField.isEmpty()) {
            boolean done = false;
            final Object decodedPayload = message.getValue();
            Long extractedSendTime = null;
            // if Pulsar is able to decode this it is better to let it do the work
            // because Pulsar caches the Schema, handles Schema evolution
            // as much efficiently as possible
            if (decodedPayload instanceof final GenericRecord pulsarGenericRecord) { // AVRO and AUTO_CONSUME

                Object field = null;
                // KeyValue is a special wrapper in Pulsar to represent a pair of values
                // a Key and a Value
                final Object nativeObject = pulsarGenericRecord.getNativeObject();
                if (nativeObject instanceof KeyValue keyValue) {
                    // look into the Key
                    if (keyValue.getKey() instanceof GenericRecord keyPart) {
                        try {
                            field = keyPart.getField(this.payloadRttField);
                        } catch (final AvroRuntimeException err) {
                            // field is not in the key
                            MessageConsumerOp.logger.error("Cannot find {} in key {}: {}", this.payloadRttField, keyPart, String.valueOf(err));
                        }
                    }
                    // look into the Value
                    if ((keyValue.getValue() instanceof GenericRecord valuePart) && (null == field)) {
                        try {
                            field = valuePart.getField(this.payloadRttField);
                        } catch (final AvroRuntimeException err) {
                            // field is not in the value
                            MessageConsumerOp.logger.error("Cannot find {} in value {}: {}", this.payloadRttField, valuePart, String.valueOf(err));
                        }
                    }
                    if (null == field)
                        throw new RuntimeException("Cannot find field {}" + this.payloadRttField + " in " + keyValue.getKey() + " and " + keyValue.getValue());
                } else field = pulsarGenericRecord.getField(this.payloadRttField);

                if (null != field) if (field instanceof Number) extractedSendTime = ((Number) field).longValue();
                else extractedSendTime = Long.valueOf(field.toString());
                else
                    MessageConsumerOp.logger.error("Cannot find {} in value {}", this.payloadRttField, pulsarGenericRecord);
                done = true;
            }
            if (!done) {
                final org.apache.avro.Schema avroSchema = this.getAvroSchemaFromConfiguration();
                final org.apache.avro.generic.GenericRecord avroGenericRecord =
                    PulsarAvroSchemaUtil.GetGenericRecord_ApacheAvro(avroSchema, message.getData());
                if (avroGenericRecord.hasField(this.payloadRttField))
                    extractedSendTime = (Long) avroGenericRecord.get(this.payloadRttField);
            }
            if (null != extractedSendTime) {
                // fallout expects latencies in "ns" and not in "ms"
                final long delta = TimeUnit.MILLISECONDS
                    .toNanos(System.currentTimeMillis() - extractedSendTime);
                this.payloadRttHistogram.update(delta);
            }
        }

        // keep track end-to-end message processing latency
        if (EndToEndStartingTimeSource.NONE != e2eStartingTimeSrc) {
            long startTimeStamp = 0L;

            switch (this.e2eStartingTimeSrc) {
                case MESSAGE_PUBLISH_TIME:
                    startTimeStamp = message.getPublishTime();
                    break;
                case MESSAGE_EVENT_TIME:
                    startTimeStamp = message.getEventTime();
                    break;
                case MESSAGE_PROPERTY_E2E_STARTING_TIME:
                    final String startingTimeProperty = message.getProperty("e2e_starting_time");
                    startTimeStamp = (null != startingTimeProperty) ? Long.parseLong(startingTimeProperty) : 0L;
                    break;
            }

            if (0L != startTimeStamp) {
                final long e2eMsgLatency = System.currentTimeMillis() - startTimeStamp;
                this.e2eMsgProcLatencyHistogram.update(e2eMsgLatency);
            }
        }

        // keep track of message errors and update error counters
        if (this.seqTracking) {
            this.checkAndUpdateMessageErrorCounter(message);
        }

        final int messageSize = message.getData().length;
        this.messageSizeHistogram.update(messageSize);
    }

    private void checkAndUpdateMessageErrorCounter(final Message<?> message) {
        final String msgSeqIdStr = message.getProperty(PulsarAdapterUtil.MSG_SEQUENCE_NUMBER);

        if ( !StringUtils.isBlank(msgSeqIdStr) ) {
            final long sequenceNumber = Long.parseLong(msgSeqIdStr);
            final ReceivedMessageSequenceTracker receivedMessageSequenceTracker =
                this.receivedMessageSequenceTrackerForTopic.apply(message.getTopicName());
            receivedMessageSequenceTracker.sequenceNumberReceived(sequenceNumber);
        }
    }
}
