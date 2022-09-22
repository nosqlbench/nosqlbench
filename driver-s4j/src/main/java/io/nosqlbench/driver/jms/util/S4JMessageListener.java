package io.nosqlbench.driver.jms.util;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *  Used for async message consumption
 */
public class S4JMessageListener implements MessageListener {

    private final static Logger logger = LogManager.getLogger(S4JSpace.class);

    private final float msgAckRatio;
    private final int slowAckInSec;
    private final JMSContext jmsContext;
    private final S4JSpace s4JSpace;
    private final S4JActivity s4JActivity;

    public S4JMessageListener(JMSContext jmsContext, S4JSpace s4JSpace, float msgAckRatio, int slowAckInSec) {
        assert (jmsContext != null);
        assert (s4JSpace != null);

        this.jmsContext = jmsContext;
        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JSpace.getS4JActivity();
        this.msgAckRatio = msgAckRatio;
        this.slowAckInSec = slowAckInSec;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message != null) {
                s4JActivity.processMsgAck(jmsContext, message, msgAckRatio, slowAckInSec);

                int msgSize = message.getIntProperty(S4JActivityUtil.NB_MSG_SIZE_PROP);
                Counter bytesCounter = this.s4JActivity.getBytesCounter();
                bytesCounter.inc(msgSize);
                Histogram messageSizeHistogram = this.s4JActivity.getMessagesizeHistogram();
                messageSizeHistogram.update(msgSize);

                if (logger.isTraceEnabled()) {
                    // for testing purpose
                    String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);
                    logger.trace("onMessage::Async message receive successful - message ID {} ({}) "
                        , message.getJMSMessageID(), myMsgSeq);
                }

                if (s4JActivity.isTrackingMsgRecvCnt()) {
                    s4JSpace.incTotalOpResponseCnt();
                }
            }
            else {
                if (s4JActivity.isTrackingMsgRecvCnt()) {
                    s4JSpace.incTotalNullMsgRecvdCnt();
                }
            }
        }
        catch (JMSException e) {
            S4JActivityUtil.processMsgErrorHandling(
                e,
                s4JActivity.isStrictMsgErrorHandling(),
                "Unexpected errors when async receiving a JMS message.");
        }
    }
}
