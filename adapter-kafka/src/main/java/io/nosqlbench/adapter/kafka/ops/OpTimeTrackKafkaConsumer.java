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
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class OpTimeTrackKafkaConsumer extends OpTimeTrackKafkaClient {
    private final static Logger logger = LogManager.getLogger("OpTimeTrackKafkaConsumer");

    private final int msgPoolIntervalInMs;
    private final boolean asyncMsgCommit;
    private final boolean autoCommitEnabled;
    private final int maxMsgCntPerCommit;

    // Keep track the manual commit count
    private final ThreadLocal<Integer> manualCommitTrackingCnt = ThreadLocal.withInitial(() -> 0);

    private final KafkaConsumer<String, String> consumer;

    public OpTimeTrackKafkaConsumer(KafkaSpace kafkaSpace,
                                    boolean asyncMsgCommit,
                                    int msgPoolIntervalInMs,
                                    boolean autoCommitEnabled,
                                    int maxMsgCntPerCommit,
                                    KafkaConsumer<String, String> consumer) {
        super(kafkaSpace);
        this.msgPoolIntervalInMs = msgPoolIntervalInMs;
        this.asyncMsgCommit = asyncMsgCommit;
        this.autoCommitEnabled = autoCommitEnabled;
        this.maxMsgCntPerCommit = maxMsgCntPerCommit;
        this.consumer = consumer;
    }

    public int getManualCommitTrackingCnt() { return manualCommitTrackingCnt.get(); }
    public void incManualCommitTrackingCnt() {
        int curVal = getManualCommitTrackingCnt();
        manualCommitTrackingCnt.set(curVal + 1);
    }
    public void resetManualCommitTrackingCnt() {
        manualCommitTrackingCnt.set(0);
    }

    private boolean msgCommitNeeded(long cycle) {
        // Whether to commit the transaction which happens when:
        // - "txn_batch_num" has been reached since last reset
        boolean commitNeeded = !autoCommitEnabled;

        if (commitNeeded) {
            int msgCommitTackingCnt = manualCommitTrackingCnt.get();

            if ( ( (msgCommitTackingCnt > 0) && ((msgCommitTackingCnt % maxMsgCntPerCommit) == 0) ) ||
                ( cycle >= (kafkaSpace.getTotalCycleNum() - 1) ) ) {
                commitNeeded = true;

                if (logger.isDebugEnabled()) {
                    logger.debug("Manually commit message ({}, {}, {})",
                        manualCommitTrackingCnt, msgCommitTackingCnt, cycle);
                }
            }
            else {
                commitNeeded = false;
            }
        }

        return commitNeeded;
    }

    private String printRecvedMsg(ConsumerRecord<String, String> record) {
        Headers headers = record.headers();
        Header nbMsgSeqHeader = headers.lastHeader(KafkaAdapterUtil.NB_MSG_SEQ_PROP);

        StringBuilder sb = new StringBuilder();
        if (nbMsgSeqHeader != null) {
            sb.append("Header (MsgSeq): " + new String(nbMsgSeqHeader.value()) + "; ");
        }
        sb.append("Key: " + record.key() + "; ");
        sb.append("Value: " + record.value() + "; ");


        return sb.toString();
    }

    @Override
    void cycleMsgProcess(long cycle, Object cycleObj) {
        synchronized (this) {
            ConsumerRecords<String, String> records = consumer.poll(msgPoolIntervalInMs);
            for (ConsumerRecord<String, String> record : records) {
                if (record != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "Receiving message is successful: [{}] - offset({}), cycle ({})",
                            printRecvedMsg(record),
                            record.offset(),
                            cycle);
                    }

                    if (!autoCommitEnabled) {
                        boolean bCommitMsg = msgCommitNeeded(cycle);
                        if (bCommitMsg) {
                            if (!asyncMsgCommit) {
                                consumer.commitSync();

                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                        "Sync message commit is successful: cycle ({}), maxMsgCntPerCommit ({})",
                                        cycle,
                                        maxMsgCntPerCommit);
                                }
                            } else {
                                consumer.commitAsync(new OffsetCommitCallback() {
                                    @Override
                                    public void onComplete(Map<TopicPartition, OffsetAndMetadata> map, Exception e) {
                                        if (logger.isDebugEnabled()) {
                                            if (e == null) {
                                                logger.debug(
                                                    "Async message commit succeeded: cycle({}), maxMsgCntPerCommit ({})",
                                                    cycle,
                                                    maxMsgCntPerCommit);
                                            } else {
                                                logger.debug(
                                                    "Async message commit failed: cycle ({}), maxMsgCntPerCommit ({}), error ({})",
                                                    cycle,
                                                    maxMsgCntPerCommit,
                                                    e.getMessage());
                                            }
                                        }
                                    }
                                });
                            }

                            resetManualCommitTrackingCnt();
                        } else {
                            incManualCommitTrackingCnt();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            if (consumer != null) {
                if (!asyncMsgCommit)
                    consumer.commitSync();
                else
                    consumer.commitAsync();

                consumer.close();
            }

            this.manualCommitTrackingCnt.remove();
        }
        catch (IllegalStateException ise) {
            // If a consumer is already closed, that's fine.
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
