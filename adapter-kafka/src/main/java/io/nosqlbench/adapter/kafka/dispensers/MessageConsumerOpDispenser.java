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

package io.nosqlbench.adapter.kafka.dispensers;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterInvalidParamException;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaClient;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaConsumer;
import io.nosqlbench.adapter.kafka.util.EndToEndStartingTimeSource;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapters.api.metrics.ReceivedMessageSequenceTracker;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;

public class MessageConsumerOpDispenser extends KafkaBaseOpDispenser {

    private static final Logger logger = LogManager.getLogger("MessageConsumerOpDispenser");

    private final Map<String, String> consumerClientConfMap = new HashMap<>();

    // The timeout value as message Poll interval (in seconds)
    protected final int msgPollIntervalInSec;

    // Manual commit frequency
    // - # of received messages / sec.
    // - This is only relevant when the effective setting (global level and statement level)
    //   of "enable.auto.commit" is false
    protected final int maxMsgCntPerCommit;

    protected boolean autoCommitEnabled;

    private final LongFunction<String> e2eStartTimeSrcParamStrFunc;

    private final ThreadLocal<Map<String, ReceivedMessageSequenceTracker>>
        receivedMessageSequenceTrackersForTopicThreadLocal = ThreadLocal.withInitial(HashMap::new);
    protected final LongFunction<Boolean> seqTrackingFunc;

    public MessageConsumerOpDispenser(final DriverAdapter adapter,
                                      final ParsedOp op,
                                      final LongFunction<String> tgtNameFunc,
                                      final KafkaSpace kafkaSpace) {
        super(adapter, op, tgtNameFunc, kafkaSpace);

        consumerClientConfMap.putAll(kafkaSpace.getKafkaClientConf().getConsumerConfMap());
        this.consumerClientConfMap.put("bootstrap.servers", kafkaSpace.getBootstrapSvr());

        msgPollIntervalInSec =
            NumberUtils.toInt(this.parsedOp.getStaticConfigOr("msg_poll_interval", "0"));

        maxMsgCntPerCommit =
            NumberUtils.toInt(this.parsedOp.getStaticConfig("manual_commit_batch_num", String.class));

        autoCommitEnabled = true;
        if (0 < maxMsgCntPerCommit) {
            autoCommitEnabled = false;
            this.consumerClientConfMap.put("enable.auto.commit", "false");
        } else if (this.consumerClientConfMap.containsKey("enable.auto.commit"))
            autoCommitEnabled = BooleanUtils.toBoolean(this.consumerClientConfMap.get("enable.auto.commit"));
        e2eStartTimeSrcParamStrFunc = this.lookupOptionalStrOpValueFunc(
            DOC_LEVEL_PARAMS.E2E_STARTING_TIME_SOURCE.label, "none");
        seqTrackingFunc = this.lookupStaticBoolConfigValueFunc(
            DOC_LEVEL_PARAMS.SEQ_TRACKING.label, false);
    }

    private String getEffectiveGroupId(final long cycle) {
        final int grpIdx = (int) (cycle % this.consumerGrpCnt);
        String defaultGrpNamePrefix = KafkaAdapterUtil.DFT_CONSUMER_GROUP_NAME_PREFIX;
        if (this.consumerClientConfMap.containsKey("group.id"))
            defaultGrpNamePrefix = this.consumerClientConfMap.get("group.id");

        return defaultGrpNamePrefix + '-' + grpIdx;
    }

    private ReceivedMessageSequenceTracker getReceivedMessageSequenceTracker(final String topicName) {
        return this.receivedMessageSequenceTrackersForTopicThreadLocal.get()
            .computeIfAbsent(topicName, k -> this.createReceivedMessageSequenceTracker());
    }

    private ReceivedMessageSequenceTracker createReceivedMessageSequenceTracker() {
        return new ReceivedMessageSequenceTracker(this.kafkaAdapterMetrics.getMsgErrOutOfSeqCounter(),
            this.kafkaAdapterMetrics.getMsgErrDuplicateCounter(),
            this.kafkaAdapterMetrics.getMsgErrLossCounter());
    }

    protected List<String> getEffectiveTopicNameList(final long cycle) {
        final String explicitTopicListStr = this.topicNameStrFunc.apply(cycle);
        assert StringUtils.isNotBlank(explicitTopicListStr);

        return Arrays.stream(StringUtils.split(explicitTopicListStr, ','))
            .filter(StringUtils::isNotBlank)
            .toList();
    }

    private OpTimeTrackKafkaConsumer getTimeTrackKafkaConsumer(final long cycle,
                                                               final List<String> topicNameList,
                                                               final String groupId)
    {
        final String consumerName = "consumer-" + cycle % this.kafkaClntCnt;
        KafkaSpace.ConsumerCacheKey consumerCacheKey =
            new KafkaSpace.ConsumerCacheKey(consumerName, topicNameList, groupId);

        return kafkaSpace.getOpTimeTrackKafkaConsumer(consumerCacheKey, () -> {
            final Properties consumerConfProps = new Properties();
            consumerConfProps.putAll(this.consumerClientConfMap);
            consumerConfProps.put("group.id", groupId);

            final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfProps);
            synchronized (this) {
                consumer.subscribe(topicNameList);
            }
            if (MessageConsumerOpDispenser.logger.isDebugEnabled())
                MessageConsumerOpDispenser.logger.debug(
                    "Kafka consumer created: {} -- autoCommitEnabled: {}, maxMsgCntPerCommit: {}",
                    consumer,
                    this.autoCommitEnabled,
                    this.maxMsgCntPerCommit);

            return new OpTimeTrackKafkaConsumer(
                this.kafkaSpace,
                this.asyncAPI,
                this.msgPollIntervalInSec,
                this.autoCommitEnabled,
                this.maxMsgCntPerCommit,
                consumer,
                this.kafkaAdapterMetrics,
                EndToEndStartingTimeSource.valueOf(this.e2eStartTimeSrcParamStrFunc.apply(cycle).toUpperCase()),
                this::getReceivedMessageSequenceTracker,
                this.seqTrackingFunc.apply(cycle));
        });
    }

    @Override
    public KafkaOp apply(final long cycle) {
        final List<String> topicNameList = this.getEffectiveTopicNameList(cycle);
        final String groupId = this.getEffectiveGroupId(cycle);
        if ((0 == topicNameList.size()) || StringUtils.isBlank(groupId)) throw new KafkaAdapterInvalidParamException(
            "Effective consumer group name and/or topic names  are needed for creating a consumer!");

        final OpTimeTrackKafkaClient opTimeTrackKafkaConsumer =
            this.getTimeTrackKafkaConsumer(cycle, topicNameList, groupId);

        return new KafkaOp(
            this.kafkaAdapterMetrics,
            this.kafkaSpace,
            opTimeTrackKafkaConsumer,
            null);
    }
}
