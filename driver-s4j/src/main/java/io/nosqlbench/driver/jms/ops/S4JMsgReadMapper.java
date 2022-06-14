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
import io.nosqlbench.driver.jms.util.S4JActivityUtil;

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
    private final LongFunction<String> msgSelectorStrFunc;
    private final LongFunction<Boolean> noLocalBoolFunc;
    private final LongFunction<Long> readTimeoutFunc;
    private final LongFunction<Boolean> recvNoWaitBoolFunc;

    public S4JMsgReadMapper(S4JActivity s4JActivity,
                            boolean durable,
                            boolean shared,
                            LongFunction<Boolean> tempDestBoolFunc,
                            LongFunction<String> destTypeStrFunc,
                            LongFunction<String> destNameStrFunc,
                            LongFunction<Boolean> asyncAPIBoolFunc,
                            LongFunction<String> subNameStrFunc,
                            LongFunction<String> msgSelectorStrFunc,
                            LongFunction<Boolean> noLocalBoolFunc,
                            LongFunction<Long> readTimeoutFunc,
                            LongFunction<Boolean> recvNoWaitBoolFunc) {
        super(s4JActivity,
            S4JActivityUtil.getMsgReadOpType(durable,shared),
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBoolFunc);

        this.durable = durable;
        this.shared = shared;
        this.subNameStrFunc = subNameStrFunc;
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
        String subName = subNameStrFunc.apply(value);
        String msgSelector = msgSelectorStrFunc.apply(value);
        boolean noLocal = noLocalBoolFunc.apply(value);
        long readTimeout = readTimeoutFunc.apply(value);
        boolean recvNoWait = recvNoWaitBoolFunc.apply(value);

        Destination destination;
        try {
            destination = s4JActivity.getOrCreateJmsDestination(tempDest, destType, destName);
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new RuntimeException("Unable to create the JMS destination!");
        }

        JMSConsumer consumer;
        try {
            consumer = s4JActivity.getOrCreateJmsConsumer(
                destination,
                destType,
                subName,
                msgSelector,
                noLocal,
                durable,
                shared,
                asyncApi);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Unable to create the JMS consumer!");
        }

        // Default to NO read timeout
        if (readTimeout < 0) readTimeout = 0;

        return new S4JMsgReadOp(
            s4JActivity,
            destination,
            asyncApi,
            consumer,
            readTimeout,
            recvNoWait);
    }
}
