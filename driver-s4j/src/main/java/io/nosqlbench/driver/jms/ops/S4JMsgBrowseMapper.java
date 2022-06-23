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
import org.apache.commons.lang3.StringUtils;

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
public class S4JMsgBrowseMapper extends S4JOpMapper {

    private final LongFunction<String> msgSelectorStrFunc;

    public S4JMsgBrowseMapper(S4JSpace s4JSpace,
                              S4JActivity s4JActivity,
                              LongFunction<Boolean> tempDestBoolFunc,
                              LongFunction<String> destTypeStrFunc,
                              LongFunction<String> destNameStrFunc,
                              LongFunction<Boolean> reuseClntBoolFunc,
                              LongFunction<Boolean> asyncAPIBoolFunc,
                              LongFunction<Integer> txnBatchNumFunc,
                              LongFunction<String> msgSelectorStrFunc) {
        super(s4JSpace,
            s4JActivity,
            S4JActivityUtil.MSG_OP_TYPES.MSG_BROWSE.label,
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            reuseClntBoolFunc,
            asyncAPIBoolFunc,
            txnBatchNumFunc);

        this.msgSelectorStrFunc = msgSelectorStrFunc;
    }

    @Override
    public S4JOp apply(long value) {
        boolean tempDest = tempDestBoolFunc.apply(value);
        String destType = destTypeStrFunc.apply(value);
        String destName = destNameStrFunc.apply(value);
        String msgSelector = msgSelectorStrFunc.apply(value);

        int jmsSessionSeqNum = (int)(value % s4JActivity.getMaxNumSessionPerConn());
        S4JJMSContextWrapper s4JJMSContextWrapper = s4JSpace.getS4jJmsContextWrapper(jmsSessionSeqNum);
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        if (tempDest) {
            throw new RuntimeException("Can't use temporary destination for a QueueBrowser !");
        }

        if (StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label)) {
            throw new RuntimeException("Can't use a topic destination for a QueueBrowser !");
        }

        Queue queue;
        try {
            queue = (Queue)s4JSpace.getOrCreateJmsDestination(s4JJMSContextWrapper, tempDest, destType, destName);
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new RuntimeException("Unable to create the JMS destination!");
        }

        QueueBrowser browser;
        try {
            browser = s4JSpace.getOrCreateJmsQueueBrowser(
                s4JJMSContextWrapper,
                queue,
                msgSelector);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Unable to create the JMS consumer!");
        }

        return new S4JMsgBrowseOp(
            s4JActivity,
            jmsContext,
            queue,
            browser);
    }
}
