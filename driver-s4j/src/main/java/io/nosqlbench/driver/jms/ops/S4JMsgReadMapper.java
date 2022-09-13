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
import io.nosqlbench.driver.jms.conn.S4JConnInfoUtil;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JJMSContextWrapper;

import javax.jms.*;
import java.util.Map;
import java.util.function.LongFunction;

public class S4JMsgReadMapper extends S4JOpMapper {

    private final boolean durable;
    private final boolean shared;
    private final LongFunction<String> subNameStrFunc;
    private final float msgAckRatio;
    private final LongFunction<String> msgSelectorStrFunc;
    private final boolean noLocalBool;
    private final LongFunction<Long> readTimeoutFunc;
    private final boolean recvNoWaitBool;
    private final Map<String,String> extraConsumerConfigRaw;
    private final Map<String,Object> combinedConsumerConfigObjMap;

    public S4JMsgReadMapper(S4JSpace s4JSpace,
                            S4JActivity s4JActivity,
                            boolean durable,
                            boolean shared,
                            boolean tempDestBool,
                            LongFunction<String> destTypeStrFunc,
                            LongFunction<String> destNameStrFunc,
                            boolean asyncAPIBool,
                            int txnBatchNum,
                            boolean blockingMsgRecvBool,
                            LongFunction<String> subNameStrFunc,
                            float msgAckRatio,
                            LongFunction<String> msgSelectorStrFunc,
                            boolean noLocalBool,
                            LongFunction<Long> readTimeoutFunc,
                            boolean recvNoWaitBool,
                            Map<String,String> extraConsumerConfigRaw) {
        super(s4JSpace,
            s4JActivity,
            S4JActivityUtil.getMsgReadOpType(durable,shared),
            tempDestBool,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBool,
            txnBatchNum,
            blockingMsgRecvBool);

        this.durable = durable;
        this.shared = shared;
        this.subNameStrFunc = subNameStrFunc;
        this.msgAckRatio = msgAckRatio;
        this.msgSelectorStrFunc = msgSelectorStrFunc;
        this.noLocalBool = noLocalBool;
        this.readTimeoutFunc = readTimeoutFunc;
        this.recvNoWaitBool = recvNoWaitBool;
        this.extraConsumerConfigRaw = extraConsumerConfigRaw;
        this.combinedConsumerConfigObjMap = S4JConnInfoUtil.mergeExtraConsumerConfig(
            s4JActivity.getS4JConnInfo(), this.extraConsumerConfigRaw);
    }

    @Override
    public S4JOp apply(long value) {
        String destType = destTypeStrFunc.apply(value);
        String destName = destNameStrFunc.apply(value);
        String subName = subNameStrFunc.apply(value);
        String msgSelector = msgSelectorStrFunc.apply(value);
        long readTimeout = readTimeoutFunc.apply(value);

        S4JJMSContextWrapper s4JJMSContextWrapper =
            s4JSpace.getOrCreateS4jJmsContextWrapper(value, this.combinedConsumerConfigObjMap);
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean commitTransaction = !super.commitTransaction(txnBatchNum, jmsContext.getSessionMode(), value);

        Destination destination;
        try {
            destination = s4JSpace.getOrCreateJmsDestination(s4JJMSContextWrapper, tempDestBool, destType, destName);
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
                msgAckRatio,
                noLocalBool,
                durable,
                shared,
                asyncAPIBool);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Unable to create the JMS consumer!");
        }

        // Default to NO read timeout
        if (readTimeout < 0) readTimeout = 0;

        return new S4JMsgReadOp(
            value,
            s4JSpace,
            s4JActivity,
            jmsContext,
            asyncAPIBool,
            blockingMsgRecvBool,
            consumer,
            msgAckRatio,
            readTimeout,
            recvNoWaitBool,
            commitTransaction);
    }
}
