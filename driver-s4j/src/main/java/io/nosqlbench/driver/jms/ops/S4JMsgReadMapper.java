package io.nosqlbench.driver.jms.ops;

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

import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.S4JSpace;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JJMSContextWrapper;

import javax.jms.*;
import java.util.function.LongFunction;

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
public class S4JMsgReadMapper extends S4JOpMapper {

    private final boolean durable;
    private final boolean shared;
    private final LongFunction<String> subNameStrFunc;
    private final LongFunction<Float> msgAckRatioFunc;
    private final LongFunction<String> msgSelectorStrFunc;
    private final LongFunction<Boolean> noLocalBoolFunc;
    private final LongFunction<Long> readTimeoutFunc;
    private final LongFunction<Boolean> recvNoWaitBoolFunc;

    public S4JMsgReadMapper(S4JSpace s4JSpace,
                            S4JActivity s4JActivity,
                            boolean durable,
                            boolean shared,
                            LongFunction<Boolean> tempDestBoolFunc,
                            LongFunction<String> destTypeStrFunc,
                            LongFunction<String> destNameStrFunc,
                            LongFunction<Boolean> reuseClntBoolFunc,
                            LongFunction<Boolean> asyncAPIBoolFunc,
                            LongFunction<Integer> txnBatchNumFunc,
                            LongFunction<String> subNameStrFunc,
                            LongFunction<Float> msgAckRatioFunc,
                            LongFunction<String> msgSelectorStrFunc,
                            LongFunction<Boolean> noLocalBoolFunc,
                            LongFunction<Long> readTimeoutFunc,
                            LongFunction<Boolean> recvNoWaitBoolFunc) {
        super(s4JSpace,
            s4JActivity,
            S4JActivityUtil.getMsgReadOpType(durable,shared),
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            reuseClntBoolFunc,
            asyncAPIBoolFunc,
            txnBatchNumFunc);

        this.durable = durable;
        this.shared = shared;
        this.subNameStrFunc = subNameStrFunc;
        this.msgAckRatioFunc = msgAckRatioFunc;
        this.msgSelectorStrFunc = msgSelectorStrFunc;
        this.noLocalBoolFunc = noLocalBoolFunc;
        this.readTimeoutFunc = readTimeoutFunc;
        this.recvNoWaitBoolFunc = recvNoWaitBoolFunc;
    }

    @Override
    public S4JOp apply(long value) {
        boolean tempDest = tempDestBoolFunc.apply(value);
        String destType = destTypeStrFunc.apply(value);
        String destName = destNameStrFunc.apply(value);
        boolean asyncApi = asyncAPIBoolFunc.apply(value);
        int txnBatchNum = txnBatchNumFunc.apply(value);
        boolean reuseClnt = reuseClntBoolFunc.apply(value);
        String subName = subNameStrFunc.apply(value);
        float msgAckRatio = msgAckRatioFunc.apply(value);
        String msgSelector = msgSelectorStrFunc.apply(value);
        boolean noLocal = noLocalBoolFunc.apply(value);
        long readTimeout = readTimeoutFunc.apply(value);
        boolean recvNoWait = recvNoWaitBoolFunc.apply(value);

        int jmsSessionSeqNum = (int)(value % s4JActivity.getMaxNumSessionPerConn());
        S4JJMSContextWrapper s4JJMSContextWrapper = s4JSpace.getS4jJmsContextWrapper(jmsSessionSeqNum);
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean commitTransaction = super.commitTransaction(txnBatchNum, jmsContext.getSessionMode(), value);

        Destination destination;
        try {
            destination = s4JSpace.getOrCreateJmsDestination(s4JJMSContextWrapper, tempDest, destType, destName);
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new RuntimeException("Unable to create the JMS destination!");
        }

        JMSConsumer consumer;
        try {
            consumer = s4JSpace.getOrCreateJmsConsumer(
                s4JJMSContextWrapper,
                destination,
                destType,
                subName,
                msgSelector,
                noLocal,
                durable,
                shared,
                reuseClnt,
                asyncApi);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Unable to create the JMS consumer!");
        }

        // Default to NO read timeout
        if (readTimeout < 0) readTimeout = 0;

        return new S4JMsgReadOp(
            s4JSpace,
            s4JActivity,
            jmsContext,
            destination,
            asyncApi,
            consumer,
            msgAckRatio,
            readTimeout,
            recvNoWait,
            commitTransaction);
    }
}
