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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.S4JSpace;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

public class S4JMsgReadOp extends S4JTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(S4JMsgReadOp.class);

    private S4JSpace s4JSpace;
    private final S4JActivity s4JActivity;
    private final JMSContext jmsContext;
    private final int jmsSessionMode;
    private final Destination destination;
    private final boolean asyncApi;
    private final JMSConsumer jmsConsumer;
    private final float msgAckRatio;
    private final long msgReadTimeout;
    private final boolean recvNoWait;
    private final boolean commitTransact;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;

    public S4JMsgReadOp(S4JSpace s4JSpace,
                        S4JActivity s4JActivity,
                        JMSContext jmsContext,
                        Destination destination,
                        boolean asyncApi,
                        JMSConsumer consumer,
                        float msgAckRatio,
                        long readTimeout,
                        boolean recvNoWait,
                        boolean commitTransact) {
        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JActivity;
        this.jmsContext = jmsContext;
        this.jmsSessionMode = jmsContext.getSessionMode();
        this.destination = destination;
        this.asyncApi = asyncApi;
        this.jmsConsumer = consumer;

        this.msgAckRatio = msgAckRatio;
        if (msgAckRatio < 0)
            msgAckRatio = 0.0f;
        else if (msgAckRatio > 1)
            msgAckRatio = 1.0f;

        this.msgReadTimeout = readTimeout;
        this.recvNoWait = recvNoWait;

        this.commitTransact = commitTransact;

        this.bytesCounter = s4JActivity.getBytesCounter();
        this.messageSizeHistogram = s4JActivity.getMessagesizeHistogram();
    }

    @Override
    public void run() {
        Message recvdMsg;

        try {
            // By default, if message read time out value is 0, it will block forever
            // Simulate it as the case for recvNoWait
            if (recvNoWait || (msgReadTimeout == 0)) {
                recvdMsg = jmsConsumer.receiveNoWait();
            } else {
                recvdMsg = jmsConsumer.receive(msgReadTimeout);
            }
            if (this.commitTransact) jmsContext.commit();

            // Please see S4JActivity::getOrCreateJmsConsumer() for async processing
            if (!asyncApi) {
                if (recvdMsg != null) {
                    s4JActivity.processMsgAck(jmsSessionMode, recvdMsg, msgAckRatio);
                }

                byte[] recvdMsgBody = recvdMsg.getBody(byte[].class);
                int messageSize = recvdMsgBody.length;
                bytesCounter.inc(messageSize);
                messageSizeHistogram.update(messageSize);

                if (logger.isDebugEnabled()) {
                    // for testing purpose
                    String myMsgSeq = recvdMsg.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);
                    logger.debug("Sync message receive successful - message ID {} ({}) "
                        , recvdMsg.getJMSMessageID(), myMsgSeq);
                }

                s4JSpace.incTotalOpResponseCnt();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected errors when receiving a JMS message.");
        }
    }
}
