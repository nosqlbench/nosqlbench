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

package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Supplier;

/**
 * This maps a set of specifier functions to a pulsar operation. The pulsar operation contains
 * enough state to define a pulsar operation such that it can be executed, measured, and possibly
 * retried if needed.
 *
 * This function doesn't act *as* the operation. It merely maps the construction logic into
 * a simple functional type, given the component functions.
 *
 * For additional parameterization, the command template is also provided.
 */
public class PulsarConsumerMapper extends PulsarTransactOpMapper {

    private final static Logger logger = LogManager.getLogger(PulsarProducerMapper.class);

    private final LongFunction<Consumer<?>> consumerFunc;
    private final EndToEndStartingTimeSource endToEndStartingTimeSource;
    private final LongFunction<String> payloadRttFieldFunc;

    public PulsarConsumerMapper(CommandTemplate cmdTpl,
                                PulsarSpace clientSpace,
                                PulsarActivity pulsarActivity,
                                LongFunction<Boolean> asyncApiFunc,
                                LongFunction<Boolean> useTransactionFunc,
                                LongFunction<Boolean> seqTrackingFunc,
                                LongFunction<Supplier<Transaction>> transactionSupplierFunc,
                                LongFunction<Consumer<?>> consumerFunc,
                                EndToEndStartingTimeSource endToEndStartingTimeSource,
                                LongFunction<String> payloadRttFieldFunc) {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc, useTransactionFunc, seqTrackingFunc, transactionSupplierFunc);
        this.consumerFunc = consumerFunc;
        this.endToEndStartingTimeSource = endToEndStartingTimeSource;
        this.payloadRttFieldFunc = payloadRttFieldFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        boolean seqTracking = seqTrackingFunc.apply(value);
        Consumer<?> consumer = consumerFunc.apply(value);
        boolean asyncApi = asyncApiFunc.apply(value);
        boolean useTransaction = useTransactionFunc.apply(value);
        Supplier<Transaction> transactionSupplier = transactionSupplierFunc.apply(value);
        String payloadRttFieldFunc = this.payloadRttFieldFunc.apply(value);

        return new PulsarConsumerOp(
            pulsarActivity,
            asyncApi,
            useTransaction,
            seqTracking,
            transactionSupplier,
            consumer,
            clientSpace.getPulsarSchema(),
            clientSpace.getPulsarClientConf().getConsumerTimeoutSeconds(),
            endToEndStartingTimeSource,
            this::getReceivedMessageSequenceTracker,
            payloadRttFieldFunc);
    }


    private ReceivedMessageSequenceTracker getReceivedMessageSequenceTracker(String topicName) {
        return receivedMessageSequenceTrackersForTopicThreadLocal.get()
            .computeIfAbsent(topicName, k -> createReceivedMessageSequenceTracker());
    }

    private ReceivedMessageSequenceTracker createReceivedMessageSequenceTracker() {
        return new ReceivedMessageSequenceTracker(pulsarActivity.getMsgErrOutOfSeqCounter(),
            pulsarActivity.getMsgErrDuplicateCounter(),
            pulsarActivity.getMsgErrLossCounter());
    }

    private final ThreadLocal<Map<String, ReceivedMessageSequenceTracker>> receivedMessageSequenceTrackersForTopicThreadLocal =
        ThreadLocal.withInitial(HashMap::new);

}
