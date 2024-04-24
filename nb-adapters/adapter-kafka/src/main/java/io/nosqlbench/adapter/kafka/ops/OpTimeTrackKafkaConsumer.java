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

import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.util.EndToEndStartingTimeSource;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterMetrics;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapters.api.metrics.ReceivedMessageSequenceTracker;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class OpTimeTrackKafkaConsumer extends OpTimeTrackKafkaClient {
    private static final Logger logger = LogManager.getLogger("OpTimeTrackKafkaConsumer");
    private final EndToEndStartingTimeSource e2eStartingTimeSrc;
    private final int msgPoolIntervalInMs;
    private final boolean asyncMsgCommit;
    private final boolean autoCommitEnabled;
    private final int maxMsgCntPerCommit;

    // Keep track the manual commit count
    private final ThreadLocal<Integer> manualCommitTrackingCnt = ThreadLocal.withInitial(() -> 0);

    private final KafkaConsumer<String, String> consumer;
    private final Histogram e2eMsgProcLatencyHistogram;
    private final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic;
    private final boolean seqTracking;

    public OpTimeTrackKafkaConsumer(final KafkaSpace kafkaSpace,
                                    final boolean asyncMsgCommit,
                                    final int msgPoolIntervalInMs,
                                    final boolean autoCommitEnabled,
                                    final int maxMsgCntPerCommit,
                                    final KafkaConsumer<String, String> consumer,
                                    final KafkaAdapterMetrics kafkaAdapterMetrics,
                                    final EndToEndStartingTimeSource e2eStartingTimeSrc,
                                    final Function<String, ReceivedMessageSequenceTracker> receivedMessageSequenceTrackerForTopic,
                                    final boolean seqTracking) {
        super(kafkaSpace);
        this.msgPoolIntervalInMs = msgPoolIntervalInMs;
        this.asyncMsgCommit = asyncMsgCommit;
        this.autoCommitEnabled = autoCommitEnabled;
        this.maxMsgCntPerCommit = maxMsgCntPerCommit;
        this.consumer = consumer;
        this.e2eStartingTimeSrc = e2eStartingTimeSrc;
        e2eMsgProcLatencyHistogram = kafkaAdapterMetrics.getE2eMsgProcLatencyHistogram();
        this.receivedMessageSequenceTrackerForTopic = receivedMessageSequenceTrackerForTopic;
        this.seqTracking = seqTracking;
    }

    public int getManualCommitTrackingCnt() { return this.manualCommitTrackingCnt.get(); }
    public void incManualCommitTrackingCnt() {
        final int curVal = this.getManualCommitTrackingCnt();
        this.manualCommitTrackingCnt.set(curVal + 1);
    }
    public void resetManualCommitTrackingCnt() {
        this.manualCommitTrackingCnt.set(0);
    }

    private boolean msgCommitNeeded(final long cycle) {
        // Whether to commit the transaction which happens when:
        // - "txn_batch_num" has been reached since last reset
        boolean commitNeeded = !this.autoCommitEnabled;

        if (commitNeeded) {
            final int msgCommitTackingCnt = this.manualCommitTrackingCnt.get();

            if ( 0 < msgCommitTackingCnt && 0 == msgCommitTackingCnt % maxMsgCntPerCommit ||
                cycle >= this.kafkaSpace.getTotalCycleNum() - 1) {
                commitNeeded = true;

                if (OpTimeTrackKafkaConsumer.logger.isDebugEnabled())
                    OpTimeTrackKafkaConsumer.logger.debug("Manually commit message ({}, {}, {})",
                        this.manualCommitTrackingCnt, msgCommitTackingCnt, cycle);
            }
            else commitNeeded = false;
        }

        return commitNeeded;
    }

    private String printRecvedMsg(final ConsumerRecord<String, String> record) {
        final Headers headers = record.headers();
        final Header nbMsgSeqHeader = headers.lastHeader(KafkaAdapterUtil.NB_MSG_SEQ_PROP);

        final StringBuilder sb = new StringBuilder();
        if (null != nbMsgSeqHeader)
            sb.append("Header (MsgSeq): " + new String(nbMsgSeqHeader.value(), StandardCharsets.UTF_8) + "; ");
        sb.append("Key: " + record.key() + "; ");
        sb.append("Value: " + record.value() + "; ");


        return sb.toString();
    }

    @Override
    void cycleMsgProcess(final long cycle, final Object cycleObj) {
        if (this.kafkaSpace.isShuttingDown()) return;

        synchronized (this) {
            final ConsumerRecords<String, String> records = this.consumer.poll(this.msgPoolIntervalInMs);
            for (final ConsumerRecord<String, String> record : records)
                if (null != record) {
                    if (OpTimeTrackKafkaConsumer.logger.isDebugEnabled()) {
                        final Header msg_seq_header = record.headers().lastHeader(KafkaAdapterUtil.MSG_SEQUENCE_NUMBER);
                        OpTimeTrackKafkaConsumer.logger.debug(
                            "Receiving message is successful: [{}] - offset({}), cycle ({}), e2e_latency_ms({}), e2e_seq_number({})",
                            this.printRecvedMsg(record),
                            record.offset(),
                            cycle,
                            System.currentTimeMillis() - record.timestamp(),
                            null != msg_seq_header ? new String(msg_seq_header.value(), StandardCharsets.UTF_8) : "null");
                    }

                    if (!this.autoCommitEnabled) {
                        final boolean bCommitMsg = this.msgCommitNeeded(cycle);
                        if (bCommitMsg) {
                            if (!this.asyncMsgCommit) {
                                this.consumer.commitSync();
                                this.checkAndUpdateMessageE2EMetrics(record);
                                if (OpTimeTrackKafkaConsumer.logger.isDebugEnabled())
                                    OpTimeTrackKafkaConsumer.logger.debug(
                                        "Sync message commit is successful: cycle ({}), maxMsgCntPerCommit ({})",
                                        cycle,
                                        this.maxMsgCntPerCommit);
                            } else this.consumer.commitAsync(new OffsetCommitCallback() {
                                @Override
                                public void onComplete(final Map<TopicPartition, OffsetAndMetadata> map, final Exception e) {
                                    if (OpTimeTrackKafkaConsumer.logger.isDebugEnabled()) if (null == e) {
                                        OpTimeTrackKafkaConsumer.logger.debug(
                                            "Async message commit succeeded: cycle({}), maxMsgCntPerCommit ({})",
                                            cycle,
                                            OpTimeTrackKafkaConsumer.this.maxMsgCntPerCommit);
                                        OpTimeTrackKafkaConsumer.this.checkAndUpdateMessageE2EMetrics(record);
                                    } else OpTimeTrackKafkaConsumer.logger.debug(
                                        "Async message commit failed: cycle ({}), maxMsgCntPerCommit ({}), error ({})",
                                        cycle,
                                        OpTimeTrackKafkaConsumer.this.maxMsgCntPerCommit,
                                        e.getMessage());
                                }
                            });

                            this.resetManualCommitTrackingCnt();
                        } else {
                            this.checkAndUpdateMessageE2EMetrics(record);
                            this.incManualCommitTrackingCnt();
                        }
                    }
                    this.checkAndUpdateMessageE2EMetrics(record);
                }
        }
    }

    private void checkAndUpdateMessageE2EMetrics(final ConsumerRecord<String, String> record) {
        // keep track of message errors and update error counters
        if(this.seqTracking) {
            this.checkAndUpdateMessageErrorCounter(record);
        }
        this.updateE2ELatencyMetric(record);
    }

    private void updateE2ELatencyMetric(final ConsumerRecord<String, String> record) {
        long startTimeStamp = 0L;
        if (Objects.requireNonNull(this.e2eStartingTimeSrc) == EndToEndStartingTimeSource.MESSAGE_PUBLISH_TIME) {
            startTimeStamp = record.timestamp();
        }
        if (0L != startTimeStamp) {
            final long e2eMsgLatency = System.currentTimeMillis() - startTimeStamp;
            this.e2eMsgProcLatencyHistogram.update(e2eMsgLatency);
        }
    }

    private void checkAndUpdateMessageErrorCounter(final ConsumerRecord<String, String> record) {
        final Header msg_seq_number_header = record.headers().lastHeader(KafkaAdapterUtil.MSG_SEQUENCE_NUMBER);
        final String msgSeqIdStr = (null != msg_seq_number_header) ? new String(msg_seq_number_header.value(), StandardCharsets.UTF_8) : StringUtils.EMPTY;
        if (!StringUtils.isBlank(msgSeqIdStr)) {
            final long sequenceNumber = Long.parseLong(msgSeqIdStr);
            final ReceivedMessageSequenceTracker receivedMessageSequenceTracker =
                this.receivedMessageSequenceTrackerForTopic.apply(record.topic());
            receivedMessageSequenceTracker.sequenceNumberReceived(sequenceNumber);
        } else
            OpTimeTrackKafkaConsumer.logger.warn("Message sequence number header is null, skipping e2e message error metrics generation.");
    }

    @Override
    public void close() {
        try {
            if (null != consumer) {
                if (!this.asyncMsgCommit) {
                    this.consumer.commitSync();
                } else {
                    this.consumer.commitAsync();
                }

                this.consumer.close();
            }

            manualCommitTrackingCnt.remove();
        }
        catch (final IllegalStateException ise) {
            // If a consumer is already closed, that's fine.
        }
        catch (final Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
    }
}
