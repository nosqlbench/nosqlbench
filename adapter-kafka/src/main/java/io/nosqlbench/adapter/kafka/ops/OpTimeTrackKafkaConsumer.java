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

import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.util.EndToEndStartingTimeSource;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterMetrics;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.ReceivedMessageSequenceTracker;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Function;

public class OpTimeTrackKafkaConsumer extends OpTimeTrackKafkaClient {
    private final static Logger logger = LogManager.getLogger("OpTimeTrackKafkaConsumer");
    private final EndToEndStartingTimeSource e2eStartingTimeSrc;
    private final int msgPoolIntervalInMs;
    private final boolean asyncMsgCommit;
    private final boolean autoCommitEnabled;
    private final int maxMsgCntPerCommit;

    // Keep track the manual commit count
    private final ThreadLocal<Integer> manualCommitTrackingCnt = ThreadLocal.withInitial(() -> 0);

    private final KafkaConsumer<String, String> consumer;
    private Histogram e2eMsgProcLatencyHistogram;
    private final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic;
    private final boolean seqTracking;

    public OpTimeTrackKafkaConsumer(KafkaSpace kafkaSpace,
                                    boolean asyncMsgCommit,
                                    int msgPoolIntervalInMs,
                                    boolean autoCommitEnabled,
                                    int maxMsgCntPerCommit,
                                    KafkaConsumer<String, String> consumer,
                                    KafkaAdapterMetrics kafkaAdapterMetrics,
                                    EndToEndStartingTimeSource e2eStartingTimeSrc,
                                    Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic,
                                    boolean seqTracking) {
        super(kafkaSpace);
        this.msgPoolIntervalInMs = msgPoolIntervalInMs;
        this.asyncMsgCommit = asyncMsgCommit;
        this.autoCommitEnabled = autoCommitEnabled;
        this.maxMsgCntPerCommit = maxMsgCntPerCommit;
        this.consumer = consumer;
        this.e2eStartingTimeSrc = e2eStartingTimeSrc;
        this.e2eMsgProcLatencyHistogram = kafkaAdapterMetrics.getE2eMsgProcLatencyHistogram();
        this.receivedMessageSequenceTrackerForTopic = receivedMessageSequenceTrackerForTopic;
        this.seqTracking = seqTracking;
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
        if (kafkaSpace.isShuttigDown()) {
            return;
        }

        synchronized (this) {
            ConsumerRecords<String, String> records = consumer.poll(msgPoolIntervalInMs);
            for (ConsumerRecord<String, String> record : records) {
                if (record != null) {
                    if (logger.isDebugEnabled()) {
                        Header msg_seq_header = record.headers().lastHeader(PulsarAdapterUtil.MSG_SEQUENCE_NUMBER);
                        logger.debug(
                            "Receiving message is successful: [{}] - offset({}), cycle ({}), e2e_latency_ms({}), e2e_seq_number({})",
                            printRecvedMsg(record),
                            record.offset(),
                            cycle,
                            System.currentTimeMillis() - record.timestamp(),
                            (msg_seq_header != null ? new String(msg_seq_header.value())  : "null"));
                    }

                    if (!autoCommitEnabled) {
                        boolean bCommitMsg = msgCommitNeeded(cycle);
                        if (bCommitMsg) {
                            if (!asyncMsgCommit) {
                                consumer.commitSync();
                                checkAndUpdateMessageE2EMetrics(record);
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
                                                checkAndUpdateMessageE2EMetrics(record);
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
                        } else  {
                            checkAndUpdateMessageE2EMetrics(record);
                            incManualCommitTrackingCnt();
                        }
                    }
                    checkAndUpdateMessageE2EMetrics(record);
                }
            }
        }
    }

    private void checkAndUpdateMessageE2EMetrics(ConsumerRecord<String, String> record) {
        // keep track of message errors and update error counters
        if(seqTracking) checkAndUpdateMessageErrorCounter(record);
        updateE2ELatencyMetric(record);
    }

    private void updateE2ELatencyMetric(ConsumerRecord<String, String> record) {
        long startTimeStamp = 0L;
        switch (e2eStartingTimeSrc) {
            case MESSAGE_PUBLISH_TIME:
                startTimeStamp = record.timestamp();
                break;
        }
        if (startTimeStamp != 0L) {
            long e2eMsgLatency = System.currentTimeMillis() - startTimeStamp;
            e2eMsgProcLatencyHistogram.update(e2eMsgLatency);
        }
    }

    private void checkAndUpdateMessageErrorCounter(ConsumerRecord<String, String> record) {
        String msgSeqIdStr = new String(record.headers().lastHeader(PulsarAdapterUtil.MSG_SEQUENCE_NUMBER).value());
        if ( !StringUtils.isBlank(msgSeqIdStr) ) {
            long sequenceNumber = Long.parseLong(msgSeqIdStr);
            ReceivedMessageSequenceTracker receivedMessageSequenceTracker =
                receivedMessageSequenceTrackerForTopic.apply(record.topic());
            receivedMessageSequenceTracker.sequenceNumberReceived(sequenceNumber);
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
