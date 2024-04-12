package io.nosqlbench.adapter.s4j.util;

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

import com.codahale.metrics.Histogram;
import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.dispensers.S4JBaseOpDispenser;
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

    private final static Logger logger = LogManager.getLogger(S4JMessageListener.class);

    private final float msgAckRatio;
    private final int slowAckInSec;
    private final JMSContext jmsContext;
    private final S4JSpace s4jSpace;
    private final S4JBaseOpDispenser s4jBaseOpDispenser;

    public S4JMessageListener(
        JMSContext jmsContext,
        S4JSpace s4jSpace,
        S4JBaseOpDispenser s4jBaseOpDispenser,
        float msgAckRatio,
        int slowAckInSec)
    {
        assert (jmsContext != null);
        assert (s4jSpace != null);
        assert (s4jBaseOpDispenser != null);

        this.jmsContext = jmsContext;
        this.s4jSpace = s4jSpace;
        this.s4jBaseOpDispenser = s4jBaseOpDispenser;
        this.msgAckRatio = msgAckRatio;
        this.slowAckInSec = slowAckInSec;
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message != null) {
                s4jSpace.processMsgAck(jmsContext, message, msgAckRatio, slowAckInSec);

                int msgSize = message.getIntProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP);
                S4JAdapterMetrics s4JAdapterMetrics = s4jBaseOpDispenser.getS4jAdapterMetrics();
                Histogram messageSizeHistogram = s4JAdapterMetrics.getMessagesizeHistogram();
                messageSizeHistogram.update(msgSize);

                if (logger.isTraceEnabled()) {
                    // for testing purpose
                    String myMsgSeq = message.getStringProperty(S4JAdapterUtil.NB_MSG_SEQ_PROP);
                    logger.trace("onMessage::Async message receive successful - message ID {} ({}) "
                        , message.getJMSMessageID(), myMsgSeq);
                }

                if (s4jSpace.isTrackingMsgRecvCnt()) {
                    s4jSpace.incTotalOpResponseCnt();
                }
            }
            else {
                if (s4jSpace.isTrackingMsgRecvCnt()) {
                    s4jSpace.incTotalNullMsgRecvdCnt();
                }
            }
        }
        catch (JMSException e) {
            S4JAdapterUtil.processMsgErrorHandling(
                e,
                s4jSpace.isStrictMsgErrorHandling(),
                "Unexpected errors when async receiving a JMS message.");
        }
    }
}
