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

package io.nosqlbench.adapter.kafka.ops;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterUnexpectedException;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapters.api.metrics.MessageSequenceNumberSendingHandler;
import io.nosqlbench.adapters.api.metrics.EndToEndMetricsAdapterUtil;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.InterruptException;

public class OpTimeTrackKafkaProducer extends OpTimeTrackKafkaClient {

    private static final Logger logger = LogManager.getLogger("OpTimeTrackKafkaProducer");

    private final boolean transactionEnabled;

    private final boolean asyncMsgAck;
    private final boolean transactEnabledConfig;
    private final int txnBatchNum;
    private final ThreadLocal<Map<String, MessageSequenceNumberSendingHandler>> MessageSequenceNumberSendingHandlersThreadLocal =
        ThreadLocal.withInitial(HashMap::new);
    private final boolean seqTracking;
    private final Set<EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE> errSimuTypeSet;

    enum TxnProcResult {
        SUCCESS,
        RECOVERABLE_ERROR,
        FATAL_ERROR,
        UNKNOWN_ERROR
    }


    // Keep track the transaction count per thread
    private static final ThreadLocal<Integer>
        txnBatchTrackingCntTL = ThreadLocal.withInitial(() -> 0);

    private static final ThreadLocal<TxnProcResult>
        txnProcResultTL = ThreadLocal.withInitial(() -> TxnProcResult.SUCCESS);

    private final KafkaProducer<String, String> producer;

    public OpTimeTrackKafkaProducer(final KafkaSpace kafkaSpace,
                                    final boolean asyncMsgAck,
                                    final boolean transactEnabledConfig,
                                    final int txnBatchNum,
                                    final boolean seqTracking,
                                    final Set<EndToEndMetricsAdapterUtil.MSG_SEQ_ERROR_SIMU_TYPE> errSimuTypeSet,
                                    final KafkaProducer<String, String> producer) {
        super(kafkaSpace);
        this.asyncMsgAck = asyncMsgAck;
        this.transactEnabledConfig = transactEnabledConfig;
        this.txnBatchNum = txnBatchNum;
        this.seqTracking = seqTracking;
        this.errSimuTypeSet = errSimuTypeSet;
        transactionEnabled = transactEnabledConfig && 2 < txnBatchNum;
        this.producer = producer;
    }

    public static int getTxnBatchTrackingCntTL() {
        return OpTimeTrackKafkaProducer.txnBatchTrackingCntTL.get();
    }
    public static void incTxnBatchTrackingCnt() {
        OpTimeTrackKafkaProducer.txnBatchTrackingCntTL.set(OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL() + 1);
    }
    public static void resetTxnBatchTrackingCnt() {
        OpTimeTrackKafkaProducer.txnBatchTrackingCntTL.set(0);
    }

    public static TxnProcResult getTxnProcResultTL() {
        return OpTimeTrackKafkaProducer.txnProcResultTL.get();
    }
    public static void setTxnProcResultTL(final TxnProcResult result) {
        OpTimeTrackKafkaProducer.txnProcResultTL.set(result);
    }
    public static void resetTxnProcResultTL(final TxnProcResult result) {
        OpTimeTrackKafkaProducer.txnProcResultTL.set(TxnProcResult.SUCCESS);
    }

    private void processMsgTransaction(final long cycle, final KafkaProducer<String, String> producer) {
        TxnProcResult result = TxnProcResult.SUCCESS;

        if (this.transactionEnabled) {
            final int txnBatchTackingCnt = OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL();

            try {
                if (0 == txnBatchTackingCnt) {
                    // Start a new transaction when first starting the processing
                    producer.beginTransaction();
                    if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                        OpTimeTrackKafkaProducer.logger.debug("New transaction started ( {}, {}, {}, {}, {} )",
                            cycle, producer, this.transactEnabledConfig, this.txnBatchNum, OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL());
                } else if ((0 == (txnBatchTackingCnt % (txnBatchNum - 1))) ||
                    (cycle == (this.kafkaSpace.getTotalCycleNum() - 1))) synchronized (this) {
                    // Commit the current transaction
                    if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                        OpTimeTrackKafkaProducer.logger.debug("Start committing transaction ... ( {}, {}, {}, {}, {} )",
                            cycle, producer, this.transactEnabledConfig, this.txnBatchNum, OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL());
                    producer.commitTransaction();
                    if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                        OpTimeTrackKafkaProducer.logger.debug("Transaction committed ( {}, {}, {}, {}, {} )",
                            cycle, producer, this.transactEnabledConfig, this.txnBatchNum, OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL());

                    // Start a new transaction
                    producer.beginTransaction();
                    if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                        OpTimeTrackKafkaProducer.logger.debug("New transaction started ( {}, {}, {}, {}, {} )",
                            cycle, producer, this.transactEnabledConfig, this.txnBatchNum, OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL());
                }
            }
            catch (final Exception e) {
                e.printStackTrace();
                if ( e instanceof IllegalStateException ||
                    e instanceof ProducerFencedException ||
                    e instanceof UnsupportedOperationException ||
                    e instanceof AuthorizationException) result = TxnProcResult.FATAL_ERROR;
                else if ( e instanceof TimeoutException ||
                    e instanceof  InterruptException) result = TxnProcResult.RECOVERABLE_ERROR;
                else result = TxnProcResult.UNKNOWN_ERROR;
            }
        }

        OpTimeTrackKafkaProducer.setTxnProcResultTL(result);
    }

    @Override
    void cycleMsgProcess(final long cycle, final Object cycleObj) {
        // For producer, cycleObj represents a "message" (ProducerRecord)
        assert null != cycleObj;

        if (this.kafkaSpace.isShuttingDown()) {
            if (this.transactionEnabled) try {
                this.producer.abortTransaction();
                if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                    OpTimeTrackKafkaProducer.logger.debug("Abort open transaction while shutting down ( {}, {}, {}, {}, {} )",
                        cycle, this.producer, this.transactEnabledConfig, this.txnBatchNum, OpTimeTrackKafkaProducer.getTxnBatchTrackingCntTL());
            } catch (final Exception e) {
                e.printStackTrace();
            }
            return;
        }

        this.processMsgTransaction(cycle, this.producer);
        final TxnProcResult result = OpTimeTrackKafkaProducer.getTxnProcResultTL();

        if (TxnProcResult.RECOVERABLE_ERROR == result) try {
            this.producer.abortTransaction();
        } catch (final Exception e) {
            throw new KafkaAdapterUnexpectedException("Aborting transaction failed!");
        }
        else if (TxnProcResult.FATAL_ERROR == result)
            throw new KafkaAdapterUnexpectedException("Fatal error when initializing or committing transactions!");
        else if (TxnProcResult.UNKNOWN_ERROR == result)
            OpTimeTrackKafkaProducer.logger.debug("Unexpected error when initializing or committing transactions!");

        final ProducerRecord<String, String> message = (ProducerRecord<String, String>) cycleObj;
        if (this.seqTracking) {
            final long nextSequenceNumber = this.getMessageSequenceNumberSendingHandler(message.topic())
                .getNextSequenceNumber(this.errSimuTypeSet);
            message.headers().add(KafkaAdapterUtil.MSG_SEQUENCE_NUMBER, String.valueOf(nextSequenceNumber).getBytes(StandardCharsets.UTF_8));
        }
        try {
            if (TxnProcResult.SUCCESS == result) {
                final Future<RecordMetadata> responseFuture = this.producer.send(message, new Callback() {
                    @Override
                    public void onCompletion(final RecordMetadata recordMetadata, final Exception e) {
                        if (OpTimeTrackKafkaProducer.this.asyncMsgAck)
                            if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                                OpTimeTrackKafkaProducer.logger.debug("Message sending with async ack. is successful ({}) - {}, {}",
                                    cycle, OpTimeTrackKafkaProducer.this.producer, recordMetadata);
                    }
                });

                if (!this.asyncMsgAck) try {
                    final RecordMetadata recordMetadata = responseFuture.get();
                    if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                        OpTimeTrackKafkaProducer.logger.debug("Message sending with sync ack. is successful ({}) - {}, {}",
                            cycle, this.producer, recordMetadata);
                } catch (final InterruptedException | ExecutionException e) {
                    KafkaAdapterUtil.messageErrorHandling(
                        e,
                        this.kafkaSpace.isStrictMsgErrorHandling(),
                        "Unexpected error when waiting to receive message-send ack from the Kafka cluster." +
                            "\n-----\n" + e);
                }

                OpTimeTrackKafkaProducer.incTxnBatchTrackingCnt();
            }

        }
        catch ( final ProducerFencedException | OutOfOrderSequenceException |
                      UnsupportedOperationException | AuthorizationException | IllegalStateException e) {
            if (OpTimeTrackKafkaProducer.logger.isDebugEnabled())
                OpTimeTrackKafkaProducer.logger.debug("Fatal error when sending a message ({}) - {}, {}",
                    cycle, this.producer, message);
            throw new KafkaAdapterUnexpectedException(e);
        }
        catch (final Exception e) {
            throw new KafkaAdapterUnexpectedException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (null != producer) {
                if (this.transactionEnabled) {
                    this.producer.commitTransaction();
                }
                this.producer.close();
            }

            txnBatchTrackingCntTL.remove();
        }
        catch (final IllegalStateException ise) {
            // If a producer is already closed, that's fine.
        }
        catch (final Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }

    private MessageSequenceNumberSendingHandler getMessageSequenceNumberSendingHandler(final String topicName) {
        return this.MessageSequenceNumberSendingHandlersThreadLocal.get()
            .computeIfAbsent(topicName, k -> new MessageSequenceNumberSendingHandler());
    }
}
