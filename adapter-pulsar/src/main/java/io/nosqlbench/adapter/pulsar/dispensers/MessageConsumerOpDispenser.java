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

package io.nosqlbench.adapter.pulsar.dispensers;

import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.ops.MessageConsumerOp;
import io.nosqlbench.adapter.pulsar.util.EndToEndStartingTimeSource;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.ReceivedMessageSequenceTracker;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class MessageConsumerOpDispenser extends PulsarClientOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageConsumerOpDispenser");

    private final LongFunction<String> topicPatternFunc;
    private final LongFunction<String> subscriptionNameFunc;
    private final LongFunction<String> subscriptionTypeFunc;
    private final LongFunction<String> cycleConsumerNameFunc;
    private final LongFunction<String> rangesFunc;
    private final LongFunction<String> e2eStartTimeSrcParamStrFunc;
    private final LongFunction<Consumer> consumerFunction;

    private final ThreadLocal<Map<String, ReceivedMessageSequenceTracker>>
        receivedMessageSequenceTrackersForTopicThreadLocal = ThreadLocal.withInitial(HashMap::new);

    public MessageConsumerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      PulsarSpace pulsarSpace) {
        super(adapter, op, tgtNameFunc, pulsarSpace);

        this.topicPatternFunc =
            lookupOptionalStrOpValueFunc(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
        this.subscriptionNameFunc =
            lookupMandtoryStrOpValueFunc(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label);
        this.subscriptionTypeFunc =
            lookupOptionalStrOpValueFunc(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label);
        this.cycleConsumerNameFunc =
            lookupOptionalStrOpValueFunc(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.consumerName.label);
        this.rangesFunc =
            lookupOptionalStrOpValueFunc(PulsarAdapterUtil.CONSUMER_CONF_CUSTOM_KEY.ranges.label);
        this.e2eStartTimeSrcParamStrFunc = lookupOptionalStrOpValueFunc(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.E2E_STARTING_TIME_SOURCE.label, "none");
        this.consumerFunction = (l) -> getConsumer(
            tgtNameFunc.apply(l),
            topicPatternFunc.apply(l),
            subscriptionNameFunc.apply(l),
            subscriptionTypeFunc.apply(l),
            cycleConsumerNameFunc.apply(l),
            rangesFunc.apply(l));
    }

    @Override
    public MessageConsumerOp apply(long cycle) {
        return new MessageConsumerOp(
            pulsarAdapterMetrics,
            pulsarClient,
            pulsarSchema,
            asyncApiFunc.apply(cycle),
            useTransactFunc.apply(cycle),
            seqTrackingFunc.apply(cycle),
            transactSupplierFunc.apply(cycle),
            payloadRttFieldFunc.apply(cycle),
            EndToEndStartingTimeSource.valueOf(e2eStartTimeSrcParamStrFunc.apply(cycle).toUpperCase()),
            this::getReceivedMessageSequenceTracker,
            consumerFunction.apply(cycle),
            pulsarSpace.getPulsarNBClientConf().getConsumerTimeoutSeconds()
        );
    }

    private ReceivedMessageSequenceTracker getReceivedMessageSequenceTracker(String topicName) {
        return receivedMessageSequenceTrackersForTopicThreadLocal.get()
            .computeIfAbsent(topicName, k -> createReceivedMessageSequenceTracker());
    }

    private ReceivedMessageSequenceTracker createReceivedMessageSequenceTracker() {
        return new ReceivedMessageSequenceTracker(pulsarAdapterMetrics.getMsgErrOutOfSeqCounter(),
            pulsarAdapterMetrics.getMsgErrDuplicateCounter(),
            pulsarAdapterMetrics.getMsgErrLossCounter());
    }
}
