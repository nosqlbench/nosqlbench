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
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class OpTimeTrackKafkaProducer extends OpTimeTrackKafkaClient {

    private final static Logger logger = LogManager.getLogger("OpTimeTrackKafkaProducer");

    private final boolean transactionEnabled;

    private final boolean asyncMsgAck;
    private final boolean transactEnabledConfig;
    private final int txnBatchNum;

    // Keep track the transaction count per thread
    private final ThreadLocal<Integer> txnBatchTrackingCnt = ThreadLocal.withInitial(() -> 0);

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

    public int getTxnBatchTrackingCnt() { return txnBatchTrackingCnt.get(); }
    public void incTxnBatchTrackingCnt() {
        int curVal = getTxnBatchTrackingCnt();
        txnBatchTrackingCnt.set(curVal + 1);
    }

    private boolean commitCurTransactionNeeded(long cycle) {
        // Whether to commit the transaction which happens when:
        // - "txn_batch_num" has been reached since last reset
        boolean commitCurTrans = transactionEnabled;

        if (commitCurTrans) {
            int txnBatchTackingCnt = getTxnBatchTrackingCnt();

            if ( ( (txnBatchTackingCnt > 0) && ((txnBatchTackingCnt % txnBatchNum) == 0) ) ||
                ( cycle >= (kafkaSpace.getTotalCycleNum() - 1) ) ) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Commit transaction ({}, {})",
                        txnBatchTackingCnt, cycle);
                }
            }
            else {
                commitCurTrans = false;
            }
        }

        return commitCurTrans;
    }

    private boolean startNewTransactionNeeded(long cycle) {
        boolean startNewTransact = transactionEnabled;

        if (startNewTransact) {
            if ( (cycle > 0) && (cycle < (kafkaSpace.getTotalCycleNum() - 1)) ) {
                startNewTransact = commitCurTransactionNeeded(cycle);
            } else {
                startNewTransact = false;
            }
        }

        return startNewTransact;
    }

    @Override
    void cycleMsgProcess(long cycle, Object cycleObj) {
        // For producer, cycleObj represents a "message" (ProducerRecord)
        assert (cycleObj != null);

        try {
            ProducerRecord<String, String> message = (ProducerRecord<String, String>) cycleObj;
            boolean startNewTransNeeded = startNewTransactionNeeded(cycle);
            boolean commitCurTransNeeded = commitCurTransactionNeeded(cycle);

            if (commitCurTransNeeded) {
                producer.commitTransaction();
                if (logger.isDebugEnabled()) {
                    logger.debug("Transaction committed ( {}, {}, {}, {} )",
                        cycle, transactEnabledConfig, txnBatchNum, getTxnBatchTrackingCnt());
                }

                incTxnBatchTrackingCnt();
            }

            if (startNewTransNeeded) {
                producer.beginTransaction();
            }

            Future<RecordMetadata> responseFuture = producer.send(message, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (asyncMsgAck) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Message sending with async ack. is successful ( {} ) - {}",
                                cycle, recordMetadata);
                        }
                    }
                }
            });

            if (!asyncMsgAck) {
                try {
                    RecordMetadata recordMetadata = responseFuture.get();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Message sending with sync ack. is successful ( {} ) - {}",
                            cycle, recordMetadata);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    KafkaAdapterUtil.messageErrorHandling(
                        e,
                        kafkaSpace.isStrictMsgErrorHandling(),
                        "Unexpected error when waiting to receive message-send ack from the Kafka cluster." +
                            "\n-----\n" + e);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (producer != null) {
                if (transactionEnabled) producer.commitTransaction();
                producer.close();
            }

            this.txnBatchTrackingCnt.remove();
        }
        catch (IllegalStateException ise) {
            // If a producer is already closed, that's fine.
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
