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

package io.nosqlbench.adapter.kafka.dispensers;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterInvalidParamException;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaClient;
import io.nosqlbench.adapter.kafka.ops.OpTimeTrackKafkaConsumer;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.LongFunction;

public class MessageConsumerOpDispenser extends KafkaBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageConsumerOpDispenser");

    private final Map<String, String> consumerClientConfMap = new HashMap<>();

    // The timeout value as message Poll interval (in seconds)
    protected final int msgPollIntervalInSec;

    // Manual commit frequency
    // - # of received messages / sec.
    // - This is only relevant when the effective setting (global level and statement level)
    //   of "enable.auto.commit" is false
    protected final int maxMsgCntPerCommit;

    protected boolean autoCommitEnabled;

    public MessageConsumerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      KafkaSpace kafkaSpace) {
        super(adapter, op, tgtNameFunc, kafkaSpace);

        this.consumerClientConfMap.putAll(kafkaSpace.getKafkaClientConf().getConsumerConfMap());
        consumerClientConfMap.put("bootstrap.servers", kafkaSpace.getBootstrapSvr());

        this.msgPollIntervalInSec =
            NumberUtils.toInt(parsedOp.getStaticConfigOr("msg_poll_interval", "0"));

        this.maxMsgCntPerCommit =
            NumberUtils.toInt(parsedOp.getStaticConfig("manual_commit_batch_num", String.class));

        this.autoCommitEnabled = true;
        if (maxMsgCntPerCommit > 0) {
            this.autoCommitEnabled = false;
            consumerClientConfMap.put("enable.auto.commit", "false");
        } else {
            if (consumerClientConfMap.containsKey("enable.auto.commit")) {
                this.autoCommitEnabled = BooleanUtils.toBoolean(consumerClientConfMap.get("enable.auto.commit"));
            }
        }
    }

    private String getEffectiveGroupId(long cycle) {
        int grpIdx = (int) (cycle % consumerGrpCnt);
        String defaultGrpNamePrefix = "nb-grp";
        if (consumerClientConfMap.containsKey("group.id")) {
            defaultGrpNamePrefix = consumerClientConfMap.get("group.id");
        }

        return defaultGrpNamePrefix + "-" + grpIdx;
    }

    private OpTimeTrackKafkaClient getOrCreateOpTimeTrackKafkaConsumer(
        String cacheKey,
        String groupId,
        String topicName)
    {
        OpTimeTrackKafkaClient opTimeTrackKafkaClient = kafkaSpace.getOpTimeTrackKafkaClient(cacheKey);
        if (opTimeTrackKafkaClient == null) {
            Properties consumerConfProps = new Properties();
            consumerConfProps.putAll(consumerClientConfMap);
            consumerConfProps.put("group.id", groupId);

            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerConfProps);
            synchronized (this) {
                consumer.subscribe(Arrays.asList(topicName));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Kafka consumer created: {} -- {}", cacheKey, consumer);
            }

            opTimeTrackKafkaClient = new OpTimeTrackKafkaConsumer(
                kafkaSpace, asyncAPI, msgPollIntervalInSec, autoCommitEnabled, maxMsgCntPerCommit, consumer);
            kafkaSpace.addOpTimeTrackKafkaClient(cacheKey, opTimeTrackKafkaClient);
        }

        return opTimeTrackKafkaClient;
    }

    @Override
    public KafkaOp apply(long cycle) {
        String topicName = topicNameStrFunc.apply(cycle);
        String groupId = getEffectiveGroupId(cycle);
        String cacheKey = KafkaAdapterUtil.buildCacheKey(
            "consumer", topicName, groupId, String.valueOf(cycle % kafkaClntCnt));

        if (StringUtils.isBlank(groupId)) {
            throw new KafkaAdapterInvalidParamException("An effective \"group.id\" is needed for a consumer!");
        }

        OpTimeTrackKafkaClient opTimeTrackKafkaConsumer =
            getOrCreateOpTimeTrackKafkaConsumer(cacheKey, groupId, topicName);

        return new KafkaOp(
            kafkaAdapterMetrics,
            kafkaSpace,
            opTimeTrackKafkaConsumer,
            null);
    }
}
