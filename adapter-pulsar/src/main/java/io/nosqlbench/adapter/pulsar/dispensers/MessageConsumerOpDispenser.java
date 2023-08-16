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

package io.nosqlbench.adapter.pulsar.dispensers;

import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.ops.MessageConsumerOp;
import io.nosqlbench.adapter.pulsar.util.EndToEndStartingTimeSource;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.CONSUMER_CONF_CUSTOM_KEY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.CONSUMER_CONF_STD_KEY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapters.api.metrics.ReceivedMessageSequenceTracker;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class MessageConsumerOpDispenser extends PulsarClientOpDispenser {

    private static final Logger logger = LogManager.getLogger("MessageConsumerOpDispenser");

    private final LongFunction<String> topicPatternFunc;
    private final LongFunction<String> subscriptionNameFunc;
    private final LongFunction<String> subscriptionTypeFunc;
    private final LongFunction<String> cycleConsumerNameFunc;
    private final LongFunction<String> rangesFunc;
    private final LongFunction<String> e2eStartTimeSrcParamStrFunc;
    private final LongFunction<Consumer> consumerFunction;

    private final ThreadLocal<Map<String, ReceivedMessageSequenceTracker>>
        receivedMessageSequenceTrackersForTopicThreadLocal = ThreadLocal.withInitial(HashMap::new);

    public MessageConsumerOpDispenser(final DriverAdapter adapter,
                                      final ParsedOp op,
                                      final LongFunction<String> tgtNameFunc,
                                      final PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);

        topicPatternFunc =
            this.lookupOptionalStrOpValueFunc(CONSUMER_CONF_STD_KEY.topicsPattern.label);
        subscriptionNameFunc =
            this.lookupMandtoryStrOpValueFunc(CONSUMER_CONF_STD_KEY.subscriptionName.label);
        subscriptionTypeFunc =
            this.lookupOptionalStrOpValueFunc(CONSUMER_CONF_STD_KEY.subscriptionType.label);
        cycleConsumerNameFunc =
            this.lookupOptionalStrOpValueFunc(CONSUMER_CONF_STD_KEY.consumerName.label);
        rangesFunc =
            this.lookupOptionalStrOpValueFunc(CONSUMER_CONF_CUSTOM_KEY.ranges.label);
        e2eStartTimeSrcParamStrFunc = this.lookupOptionalStrOpValueFunc(
            DOC_LEVEL_PARAMS.E2E_STARTING_TIME_SOURCE.label, "none");
        consumerFunction = l -> this.getConsumer(
            tgtNameFunc.apply(l),
            this.topicPatternFunc.apply(l),
            this.subscriptionNameFunc.apply(l),
            this.subscriptionTypeFunc.apply(l),
            this.cycleConsumerNameFunc.apply(l),
            this.rangesFunc.apply(l));
    }

    @Override
    public MessageConsumerOp apply(final long cycle) {
        return new MessageConsumerOp(
            this.pulsarAdapterMetrics,
            this.pulsarClient,
            this.pulsarSchema,
            this.asyncApiFunc.apply(cycle),
            this.useTransactFunc.apply(cycle),
            this.seqTrackingFunc.apply(cycle),
            this.transactSupplierFunc.apply(cycle),
            this.payloadRttFieldFunc.apply(cycle),
            EndToEndStartingTimeSource.valueOf(this.e2eStartTimeSrcParamStrFunc.apply(cycle).toUpperCase()),
            this::getReceivedMessageSequenceTracker,
            this.consumerFunction.apply(cycle),
            this.pulsarSpace.getPulsarNBClientConf().getConsumerTimeoutSeconds()
        );
    }

    private ReceivedMessageSequenceTracker getReceivedMessageSequenceTracker(final String topicName) {
        return this.receivedMessageSequenceTrackersForTopicThreadLocal.get()
            .computeIfAbsent(topicName, k -> this.createReceivedMessageSequenceTracker());
    }

    private ReceivedMessageSequenceTracker createReceivedMessageSequenceTracker() {
        return new ReceivedMessageSequenceTracker(this.pulsarAdapterMetrics.getMsgErrOutOfSeqCounter(),
            this.pulsarAdapterMetrics.getMsgErrDuplicateCounter(),
            this.pulsarAdapterMetrics.getMsgErrLossCounter());
    }
}
