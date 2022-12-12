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

package io.nosqlbench.adapter.s4j.ops;


import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.util.S4JAdapterMetrics;
import io.nosqlbench.adapter.s4j.util.S4JAdapterUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import javax.jms.*;
import java.util.HashMap;
import java.util.Map;


public class MessageProducerOp extends S4JOp {

    private final static Logger logger = LogManager.getLogger("MessageProducerOp");

    private final JMSProducer jmsProducer;
    private final Message message;

    public MessageProducerOp(S4JAdapterMetrics s4jAdapterMetrics,
                             S4JSpace s4jSpace,
                             JMSContext jmsContext,
                             Destination destination,
                             boolean asyncApi,
                             boolean commitTransact,
                             JMSProducer jmsProducer,
                             Message message) {
        super(s4jAdapterMetrics, s4jSpace, jmsContext, destination, asyncApi, commitTransact);

        this.jmsProducer = jmsProducer;
        this.message = message;
    }

    @Override
    public Object apply(long value) {

        long timeElapsedMills = System.currentTimeMillis() - s4jOpStartTimeMills;

        // If maximum S4J operation duration is specified, only publish messages
        // before the maximum duration threshold is reached. Otherwise, this is
        // just no-op.
        if ( (maxS4jOpDurationInSec == 0) || (timeElapsedMills <= (maxS4jOpDurationInSec*1000)) ) {
            try {
                jmsProducer.send(destination, message);
                if (this.commitTransact) {
                    jmsContext.commit();
                }

                int msgSize = message.getIntProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP);
                messageSizeHistogram.update(msgSize);

                // Please see S4JBaseOpDispenser::getOrCreateJmsProducer() for async processing
                if (!asyncApi) {
                    if (logger.isDebugEnabled()) {
                        // for testing purpose
                        String myMsgSeq = message.getStringProperty(S4JAdapterUtil.NB_MSG_SEQ_PROP);
                        logger.debug("Sync message sending is successful - message ID {} ({}) "
                            , message.getJMSMessageID(), myMsgSeq);
                    }

                    if (s4jSpace.isTrackingMsgRecvCnt()) {
                        s4jSpace.incTotalOpResponseCnt();
                    }
                }
            } catch (JMSException | JMSRuntimeException e) {
                S4JAdapterUtil.processMsgErrorHandling(
                    e,
                    s4jSpace.isStrictMsgErrorHandling(),
                    "Unexpected errors when sync sending a JMS message.");
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("NB cycle number {} is no-op (maxS4jOpDurationInSec: {}, timeElapsedMills: {})",
                    value, maxS4jOpDurationInSec, timeElapsedMills);
            }
        }

        return null;
    }
}
