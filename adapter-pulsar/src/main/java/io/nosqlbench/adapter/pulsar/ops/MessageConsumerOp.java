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
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.common.schema.KeyValue;
import org.apache.pulsar.shade.org.apache.avro.AvroRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageConsumerOp extends PulsarClientOp {

    private final static Logger logger = LogManager.getLogger(MessageConsumerOp.class);

    private final boolean useTransact;
    private final boolean seqTracking;
    private final Supplier<Transaction> transactSupplier;
    private final String payloadRttField;
    private final EndToEndStartingTimeSource e2eStartingTimeSrc;
    private final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic;
    private final Consumer<?> consumer;
    private final int consumerTimeoutInSec;

    public MessageConsumerOp(PulsarAdapterMetrics pulsarAdapterMetrics,
                             PulsarClient pulsarClient,
                             Schema<?> pulsarSchema,
                             boolean asyncApi,
                             boolean useTransact,
                             boolean seqTracking,
                             Supplier<Transaction> transactSupplier,
                             String payloadRttField,
                             EndToEndStartingTimeSource e2eStartingTimeSrc,
                             Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic,
                             Consumer<?> consumer,
                             int consumerTimeoutInSec) {
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
    public Object apply(long value) {
        final Transaction transaction;
        if (useTransact) {
            // if you are in a transaction you cannot set the schema per-message
            transaction = transactSupplier.get();
        }
        else {
            transaction = null;
        }

        if (!asyncApi) {
            try {
                Message<?> message;

                if (consumerTimeoutInSec <= 0) {
                    // wait forever
                    message = consumer.receive();
                }
                else {
                    message = consumer.receive(consumerTimeoutInSec, TimeUnit.SECONDS);
                    if (message == null) {
                        if ( logger.isDebugEnabled() ) {
                            logger.debug("Failed to sync-receive a message before time out ({} seconds)", consumerTimeoutInSec);
                        }
                    }
                }

                handleMessage(transaction, message);
            }
            catch (Exception e) {
                throw new PulsarAdapterUnexpectedException("" +
                    "Sync message receiving failed - timeout value: " + consumerTimeoutInSec + " seconds ");
            }
        }
        else {
            try {
                CompletableFuture<? extends Message<?>> msgRecvFuture = consumer.receiveAsync();
                if (useTransact) {
                    // add commit step
                    msgRecvFuture = msgRecvFuture.thenCompose(msg -> {
                            Timer.Context ctx = transactionCommitTimer.time();
                            return transaction
                                .commit()
                                .whenComplete((m,e) -> ctx.close())
                                .thenApply(v-> msg);
                        }
                    );
                }

                msgRecvFuture.thenAccept(message -> {
                    try {
                        handleMessage(transaction, message);
                    } catch (PulsarClientException | TimeoutException e) {
                        pulsarActivity.asyncOperationFailed(e);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        pulsarActivity.asyncOperationFailed(e.getCause());
                    }
                }).exceptionally(ex -> {
                    pulsarActivity.asyncOperationFailed(ex);
                    return null;
                });
            }
            catch (Exception e) {
                throw new PulsarAdapterUnexpectedException(e);
            }
        }

        return null;
    }

    private void handleMessage(Transaction transaction, Message<?> message)
        throws PulsarClientException, InterruptedException, ExecutionException, TimeoutException {

        // acknowledge the message as soon as possible
        if (!useTransact) {
            consumer.acknowledgeAsync(message.getMessageId())
                .get(consumerTimeoutInSec, TimeUnit.SECONDS);
        } else {
            consumer.acknowledgeAsync(message.getMessageId(), transaction)
                .get(consumerTimeoutInSec, TimeUnit.SECONDS);

            // little problem: here we are counting the "commit" time
            // inside the overall time spent for the execution of the consume operation
            // we should refactor this operation as for PulsarProducerOp, and use the passed callback
            // to track with precision the time spent for the operation and for the commit
            try (Timer.Context ctx = transactionCommitTimer.time()) {
                transaction.commit().get();
            }
        }

        if (logger.isDebugEnabled()) {
            Object decodedPayload = message.getValue();
            if (decodedPayload instanceof GenericObject) {
                // GenericObject is a wrapper for Primitives, for AVRO/JSON structs and for KeyValu
                // we fall here with a configured AVRO schema or with AUTO_CONSUME
                GenericObject object = (GenericObject) decodedPayload;
                logger.debug("({}) message received: msg-key={}; msg-properties={}; msg-payload={}",
                    consumer.getConsumerName(),
                    message.getKey(),
                    message.getProperties(),
                    object.getNativeObject() + "");
            }
            else {
                logger.debug("({}) message received: msg-key={}; msg-properties={}; msg-payload={}",
                    consumer.getConsumerName(),
                    message.getKey(),
                    message.getProperties(),
                    new String(message.getData()));
            }
        }

        if (!payloadRttField.isEmpty()) {
            boolean done = false;
            Object decodedPayload = message.getValue();
            Long extractedSendTime = null;
            // if Pulsar is able to decode this it is better to let it do the work
            // because Pulsar caches the Schema, handles Schema evolution
            // as much efficiently as possible
            if (decodedPayload instanceof GenericRecord) { // AVRO and AUTO_CONSUME
                final GenericRecord pulsarGenericRecord = (GenericRecord) decodedPayload;

                Object field = null;
                // KeyValue is a special wrapper in Pulsar to represent a pair of values
                // a Key and a Value
                Object nativeObject = pulsarGenericRecord.getNativeObject();
                if (nativeObject instanceof KeyValue) {
                    KeyValue keyValue = (KeyValue) nativeObject;
                    // look into the Key
                    if (keyValue.getKey() instanceof GenericRecord) {
                        GenericRecord keyPart = (GenericRecord) keyValue.getKey();
                        try {
                            field = keyPart.getField(payloadRttField);
                        } catch (AvroRuntimeException err) {
                            // field is not in the key
                            logger.error("Cannot find {} in key {}: {}", payloadRttField, keyPart, err + "");
                        }
                    }
                    // look into the Value
                    if (keyValue.getValue() instanceof GenericRecord && field == null) {
                        GenericRecord valuePart = (GenericRecord) keyValue.getValue();
                        try {
                            field = valuePart.getField(payloadRttField);
                        } catch (AvroRuntimeException err) {
                            // field is not in the value
                            logger.error("Cannot find {} in value {}: {}", payloadRttField, valuePart, err + "");
                        }
                    }
                    if (field == null) {
                        throw new RuntimeException("Cannot find field {}" + payloadRttField + " in " + keyValue.getKey() + " and " + keyValue.getValue());
                    }
                } else {
                    field = pulsarGenericRecord.getField(payloadRttField);
                }

                if (field != null) {
                    if (field instanceof Number) {
                        extractedSendTime = ((Number) field).longValue();
                    } else {
                        extractedSendTime = Long.valueOf(field.toString());
                    }
                } else {
                    logger.error("Cannot find {} in value {}", payloadRttField, pulsarGenericRecord);
                }
                done = true;
            }
            if (!done) {
                org.apache.avro.Schema avroSchema = getAvroSchemaFromConfiguration();
                org.apache.avro.generic.GenericRecord avroGenericRecord =
                    PulsarAvroSchemaUtil.GetGenericRecord_ApacheAvro(avroSchema, message.getData());
                if (avroGenericRecord.hasField(payloadRttField)) {
                    extractedSendTime = (Long) avroGenericRecord.get(payloadRttField);
                }
            }
            if (extractedSendTime != null) {
                // fallout expects latencies in "ns" and not in "ms"
                long delta = TimeUnit.MILLISECONDS
                    .toNanos(System.currentTimeMillis() - extractedSendTime);
                payloadRttHistogram.update(delta);
            }
        }

        // keep track end-to-end message processing latency
        if (e2eStartingTimeSrc != EndToEndStartingTimeSource.NONE) {
            long startTimeStamp = 0L;

            switch (e2eStartingTimeSrc) {
                case MESSAGE_PUBLISH_TIME:
                    startTimeStamp = message.getPublishTime();
                    break;
                case MESSAGE_EVENT_TIME:
                    startTimeStamp = message.getEventTime();
                    break;
                case MESSAGE_PROPERTY_E2E_STARTING_TIME:
                    String startingTimeProperty = message.getProperty("e2e_starting_time");
                    startTimeStamp = startingTimeProperty != null ? Long.parseLong(startingTimeProperty) : 0L;
                    break;
            }

            if (startTimeStamp != 0L) {
                long e2eMsgLatency = System.currentTimeMillis() - startTimeStamp;
                e2eMsgProcLatencyHistogram.update(e2eMsgLatency);
            }
        }

        // keep track of message errors and update error counters
        if (seqTracking) checkAndUpdateMessageErrorCounter(message);

        int messageSize = message.getData().length;
        messageSizeHistogram.update(messageSize);
    }

    private void checkAndUpdateMessageErrorCounter(Message<?> message) {
        String msgSeqIdStr = message.getProperty(PulsarAdapterUtil.MSG_SEQUENCE_NUMBER);

        if ( !StringUtils.isBlank(msgSeqIdStr) ) {
            long sequenceNumber = Long.parseLong(msgSeqIdStr);
            ReceivedMessageSequenceTracker receivedMessageSequenceTracker =
                receivedMessageSequenceTrackerForTopic.apply(message.getTopicName());
            receivedMessageSequenceTracker.sequenceNumberReceived(sequenceNumber);
        }
    }
}
