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

package io.nosqlbench.driver.pulsar.ops;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.exception.PulsarDriverUnexpectedException;
import io.nosqlbench.driver.pulsar.util.AvroUtil;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.client.api.transaction.Transaction;
import org.apache.pulsar.common.schema.SchemaType;

public class PulsarConsumerOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarConsumerOp.class);

    private final PulsarActivity pulsarActivity;

    private final boolean asyncPulsarOp;
    private final boolean useTransaction;
    private final boolean seqTracking;
    private final Supplier<Transaction> transactionSupplier;

    private final Consumer<?> consumer;
    private final Schema<?> pulsarSchema;
    private final int timeoutSeconds;
    private final EndToEndStartingTimeSource endToEndStartingTimeSource;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;
    private final Timer transactionCommitTimer;

    // keep track of end-to-end message latency
    private final Histogram e2eMsgProcLatencyHistogram;

    private final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic;
    private final Histogram payloadRttHistogram;
    private final String payloadRttTrackingField;

    private org.apache.avro.Schema avroSchema;

    public PulsarConsumerOp(
        PulsarActivity pulsarActivity,
        boolean asyncPulsarOp,
        boolean useTransaction,
        boolean seqTracking,
        Supplier<Transaction> transactionSupplier,
        Consumer<?> consumer,
        Schema<?> schema,
        int timeoutSeconds,
        EndToEndStartingTimeSource endToEndStartingTimeSource,
        Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic,
        String payloadRttTrackingField)
    {
        this.pulsarActivity = pulsarActivity;

        this.asyncPulsarOp = asyncPulsarOp;
        this.useTransaction = useTransaction;
        this.seqTracking = seqTracking;
        this.transactionSupplier = transactionSupplier;

        this.consumer = consumer;
        this.pulsarSchema = schema;
        this.timeoutSeconds = timeoutSeconds;
        this.endToEndStartingTimeSource = endToEndStartingTimeSource;

        this.bytesCounter = pulsarActivity.getBytesCounter();
        this.messageSizeHistogram = pulsarActivity.getMessageSizeHistogram();
        this.transactionCommitTimer = pulsarActivity.getCommitTransactionTimer();

        this.e2eMsgProcLatencyHistogram = pulsarActivity.getE2eMsgProcLatencyHistogram();
        this.payloadRttHistogram = pulsarActivity.getPayloadRttHistogram();
        this.receivedMessageSequenceTrackerForTopic = receivedMessageSequenceTrackerForTopic;
        this.payloadRttTrackingField = payloadRttTrackingField;
    }

    private void checkAndUpdateMessageErrorCounter(Message<?> message) {
        String msgSeqIdStr = message.getProperty(PulsarActivityUtil.MSG_SEQUENCE_NUMBER);

        if ( !StringUtils.isBlank(msgSeqIdStr) ) {
            long sequenceNumber = Long.parseLong(msgSeqIdStr);
            ReceivedMessageSequenceTracker receivedMessageSequenceTracker = receivedMessageSequenceTrackerForTopic.apply(message.getTopicName());
            receivedMessageSequenceTracker.sequenceNumberReceived(sequenceNumber);
        }
    }

    @Override
    public void run(Runnable timeTracker) {

        final Transaction transaction;
        if (useTransaction) {
            // if you are in a transaction you cannot set the schema per-message
            transaction = transactionSupplier.get();
        }
        else {
            transaction = null;
        }

        if (!asyncPulsarOp) {
            try {
                Message<?> message;

                if (timeoutSeconds <= 0) {
                    // wait forever
                    message = consumer.receive();
                }
                else {
                    message = consumer
                        .receive(timeoutSeconds, TimeUnit.SECONDS);
                    if (message == null) {
                        throw new TimeoutException("Did not receive a message within "+timeoutSeconds+" seconds");
                    }
                }

                handleMessage(transaction, message);
            }
            catch (Exception e) {
                logger.error(
                    "Sync message receiving failed - timeout value: {} seconds ", timeoutSeconds, e);
                throw new PulsarDriverUnexpectedException("" +
                    "Sync message receiving failed - timeout value: " + timeoutSeconds + " seconds ");
            }
        }
        else {
            try {
                CompletableFuture<? extends Message<?>> msgRecvFuture = consumer.receiveAsync();
                if (useTransaction) {
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
                throw new PulsarDriverUnexpectedException(e);
            }
        }
    }

    private void handleMessage(Transaction transaction, Message<?> message)
        throws PulsarClientException, InterruptedException, ExecutionException, TimeoutException {

        // acknowledge the message as soon as possible
        if (!useTransaction) {
            consumer.acknowledgeAsync(message.getMessageId())
                .get(timeoutSeconds, TimeUnit.SECONDS);
        } else {
            consumer.acknowledgeAsync(message.getMessageId(), transaction)
                .get(timeoutSeconds, TimeUnit.SECONDS);

            // little problem: here we are counting the "commit" time
            // inside the overall time spent for the execution of the consume operation
            // we should refactor this operation as for PulsarProducerOp, and use the passed callback
            // to track with precision the time spent for the operation and for the commit
            try (Timer.Context ctx = transactionCommitTimer.time()) {
                transaction.commit().get();
            }
        }

        if (logger.isDebugEnabled()) {
            SchemaType schemaType = pulsarSchema.getSchemaInfo().getType();

            if (PulsarActivityUtil.isAvroSchemaTypeStr(schemaType.name())) {
                org.apache.avro.Schema avroSchema = getSchemaFromConfiguration();
                org.apache.avro.generic.GenericRecord avroGenericRecord =
                    AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, message.getData());

                logger.debug("({}) message received: msg-key={}; msg-properties={}; msg-payload={}",
                    consumer.getConsumerName(),
                    message.getKey(),
                    message.getProperties(),
                    avroGenericRecord.toString());
            }
            else {
                logger.debug("({}) message received: msg-key={}; msg-properties={}; msg-payload={}",
                    consumer.getConsumerName(),
                    message.getKey(),
                    message.getProperties(),
                    new String(message.getData()));
            }
        }

        if (!payloadRttTrackingField.isEmpty()) {
            Object decodedPayload = message.getValue();
            Long extractedSendTime = null;
            // if Pulsar is able to decode this it is better to let it do the work
            // because Pulsar caches the Schema, handles Schema evolution
            // as much efficiently as possible
            if (decodedPayload instanceof GenericRecord) {
                GenericRecord pulsarGenericRecord = (GenericRecord) decodedPayload;
                Object field = pulsarGenericRecord.getField(payloadRttTrackingField);
                if (field != null) {
                    if (field instanceof Number) {
                        extractedSendTime = ((Number) field).longValue();
                    } else {
                        extractedSendTime = Long.valueOf(field.toString());
                    }
                }
            } else {
                org.apache.avro.Schema avroSchema = getSchemaFromConfiguration();
                org.apache.avro.generic.GenericRecord avroGenericRecord =
                    AvroUtil.GetGenericRecord_ApacheAvro(avroSchema, message.getData());
                if (avroGenericRecord.hasField(payloadRttTrackingField)) {
                    extractedSendTime = (Long) avroGenericRecord.get(payloadRttTrackingField);
                }
            }
            if (extractedSendTime != null) {
                long delta = System.currentTimeMillis() - extractedSendTime;
                payloadRttHistogram.update(delta);
            }
        }

        // keep track end-to-end message processing latency
        if (endToEndStartingTimeSource != EndToEndStartingTimeSource.NONE) {
            long startTimeStamp = 0L;
            switch (endToEndStartingTimeSource) {
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
        bytesCounter.inc(messageSize);
        messageSizeHistogram.update(messageSize);
    }

    private org.apache.avro.Schema getSchemaFromConfiguration() {
        String avroDefStr = pulsarSchema.getSchemaInfo().getSchemaDefinition();
        // no need for synchronization, this is only a cache
        // in case of the race we will parse the string twice, not a big
        if (avroSchema == null) {
            avroSchema =  AvroUtil.GetSchema_ApacheAvro(avroDefStr);
        }
        return avroSchema;
    }

}
