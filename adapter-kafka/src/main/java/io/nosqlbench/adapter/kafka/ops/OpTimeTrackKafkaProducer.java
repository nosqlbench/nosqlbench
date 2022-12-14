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

package io.nosqlbench.adapter.kafka.ops;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterUnexpectedException;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.InterruptException;

public class OpTimeTrackKafkaProducer extends OpTimeTrackKafkaClient {

    private final static Logger logger = LogManager.getLogger("OpTimeTrackKafkaProducer");

    private final boolean transactionEnabled;

    private final boolean asyncMsgAck;
    private final boolean transactEnabledConfig;
    private final int txnBatchNum;

    enum TxnProcResult {
        SUCCESS,
        RECOVERABLE_ERROR,
        FATAL_ERROR,
        UNKNOWN_ERROR
    }


    // Keep track the transaction count per thread
    private static ThreadLocal<Integer>
        txnBatchTrackingCntTL = ThreadLocal.withInitial(() -> 0);

    private static ThreadLocal<TxnProcResult>
        txnProcResultTL = ThreadLocal.withInitial(() -> TxnProcResult.SUCCESS);

    private final KafkaProducer<String, String> producer;

    public OpTimeTrackKafkaProducer(KafkaSpace kafkaSpace,
                                    boolean asyncMsgAck,
                                    boolean transactEnabledConfig,
                                    int txnBatchNum,
                                    KafkaProducer<String, String> producer) {
        super(kafkaSpace);
        this.asyncMsgAck = asyncMsgAck;
        this.transactEnabledConfig = transactEnabledConfig;
        this.txnBatchNum = txnBatchNum;
        this.transactionEnabled = transactEnabledConfig && (txnBatchNum > 2);
        this.producer = producer;
    }

    public static int getTxnBatchTrackingCntTL() {
        return txnBatchTrackingCntTL.get();
    }
    public static void incTxnBatchTrackingCnt() {
        txnBatchTrackingCntTL.set(getTxnBatchTrackingCntTL() + 1);
    }
    public static void resetTxnBatchTrackingCnt() {
        txnBatchTrackingCntTL.set(0);
    }

    public static TxnProcResult getTxnProcResultTL() {
        return txnProcResultTL.get();
    }
    public static void setTxnProcResultTL(TxnProcResult result) {
        txnProcResultTL.set(result);
    }
    public static void resetTxnProcResultTL(TxnProcResult result) {
        txnProcResultTL.set(TxnProcResult.SUCCESS);
    }

    private void processMsgTransaction(long cycle, KafkaProducer<String, String> producer) {
        TxnProcResult result = TxnProcResult.SUCCESS;

        if (transactionEnabled) {
            int txnBatchTackingCnt = getTxnBatchTrackingCntTL();

            try {
                if (txnBatchTackingCnt == 0) {
                    // Start a new transaction when first starting the processing
                    producer.beginTransaction();
                    if (logger.isDebugEnabled()) {
                        logger.debug("New transaction started ( {}, {}, {}, {}, {} )",
                            cycle, producer, transactEnabledConfig, txnBatchNum, getTxnBatchTrackingCntTL());
                    }
                } else if ( (txnBatchTackingCnt % (txnBatchNum - 1) == 0) ||
                            (cycle == (kafkaSpace.getTotalCycleNum() - 1)) ) {

                    synchronized (this) {
                        // Commit the current transaction
                        if (logger.isDebugEnabled()) {
                            logger.debug("Start committing transaction ... ( {}, {}, {}, {}, {} )",
                                cycle, producer, transactEnabledConfig, txnBatchNum, getTxnBatchTrackingCntTL());
                        }
                        producer.commitTransaction();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Transaction committed ( {}, {}, {}, {}, {} )",
                                cycle, producer, transactEnabledConfig, txnBatchNum, getTxnBatchTrackingCntTL());
                        }

                        // Start a new transaction
                        producer.beginTransaction();
                        if (logger.isDebugEnabled()) {
                            logger.debug("New transaction started ( {}, {}, {}, {}, {} )",
                                cycle, producer, transactEnabledConfig, txnBatchNum, getTxnBatchTrackingCntTL());
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                if ( (e instanceof IllegalStateException) ||
                     (e instanceof ProducerFencedException) ||
                     (e instanceof UnsupportedOperationException) ||
                     (e instanceof AuthorizationException) ) {
                    result = TxnProcResult.FATAL_ERROR;
                }
                else if ( (e instanceof TimeoutException ) ||
                          (e instanceof  InterruptException)) {
                    result = TxnProcResult.RECOVERABLE_ERROR;
                }
                else {
                    result = TxnProcResult.UNKNOWN_ERROR;
                }
            }
        }

        setTxnProcResultTL(result);
    }

    @Override
    void cycleMsgProcess(long cycle, Object cycleObj) {
        // For producer, cycleObj represents a "message" (ProducerRecord)
        assert (cycleObj != null);

        if (kafkaSpace.isShuttigDown()) {
            if (transactionEnabled) {
                try {
                    producer.abortTransaction();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Abort open transaction while shutting down ( {}, {}, {}, {}, {} )",
                            cycle, producer, transactEnabledConfig, txnBatchNum, getTxnBatchTrackingCntTL());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return;
        }

        processMsgTransaction(cycle, producer);
        TxnProcResult result = getTxnProcResultTL();

        if (result == TxnProcResult.RECOVERABLE_ERROR) {
            try {
                producer.abortTransaction();
            }
            catch (Exception e) {
                throw new KafkaAdapterUnexpectedException("Aborting transaction failed!");
            }
        } else if (result == TxnProcResult.FATAL_ERROR) {
            throw new KafkaAdapterUnexpectedException("Fatal error when initializing or committing transactions!");
        } else if (result == TxnProcResult.UNKNOWN_ERROR) {
            logger.debug("Unexpected error when initializing or committing transactions!");
        }

        ProducerRecord<String, String> message = (ProducerRecord<String, String>) cycleObj;
        try {
            if (result == TxnProcResult.SUCCESS) {
                Future<RecordMetadata> responseFuture = producer.send(message, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (asyncMsgAck) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Message sending with async ack. is successful ({}) - {}, {}",
                                    cycle, producer, recordMetadata);
                            }
                        }
                    }
                });

                if (!asyncMsgAck) {
                    try {
                        RecordMetadata recordMetadata = responseFuture.get();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Message sending with sync ack. is successful ({}) - {}, {}",
                                cycle, producer, recordMetadata);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        KafkaAdapterUtil.messageErrorHandling(
                            e,
                            kafkaSpace.isStrictMsgErrorHandling(),
                            "Unexpected error when waiting to receive message-send ack from the Kafka cluster." +
                                "\n-----\n" + e);
                    }
                }

                incTxnBatchTrackingCnt();
            }

        }
        catch ( ProducerFencedException | OutOfOrderSequenceException |
                UnsupportedOperationException | AuthorizationException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Fatal error when sending a message ({}) - {}, {}",
                    cycle, producer, message);
            }
            throw new KafkaAdapterUnexpectedException(e);
        }
        catch (IllegalStateException | KafkaException e) {
            if (transactionEnabled) {

            }
        }
        catch (Exception e) {
            throw new KafkaAdapterUnexpectedException(e);
        }
    }

    public void close() {
        try {
            if (producer != null) {
                if (transactionEnabled) producer.commitTransaction();
                producer.close();
            }

            this.txnBatchTrackingCntTL.remove();
        }
        catch (IllegalStateException ise) {
            // If a producer is already closed, that's fine.
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
