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

import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.dispensers.S4JBaseOpDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 *  Used for async message production
 */
public class S4JCompletionListener implements CompletionListener {

    private final static Logger logger = LogManager.getLogger(S4JCompletionListener.class);

    private final S4JSpace s4JSpace;
    private final S4JBaseOpDispenser s4jBaseOpDispenser;

    public S4JCompletionListener(S4JSpace s4JSpace, S4JBaseOpDispenser s4jBaseOpDispenser) {
        assert (s4JSpace != null);
        assert (s4jBaseOpDispenser != null);
        this.s4JSpace = s4JSpace;
        this.s4jBaseOpDispenser = s4jBaseOpDispenser;
    }

    @Override
    public void onCompletion(Message message) {
        try {
            if (logger.isTraceEnabled()) {
                // for testing purpose
                String myMsgSeq = message.getStringProperty(S4JAdapterUtil.NB_MSG_SEQ_PROP);
                logger.trace("onCompletion::Async message send successful - message ID {} ({}) "
                    , message.getJMSMessageID(), myMsgSeq);
            }

            if (s4JSpace.isTrackingMsgRecvCnt() ) {
                long totalResponseCnt = s4JSpace.incTotalOpResponseCnt();
                if (logger.isTraceEnabled()) {
                    logger.trace("... async op response received so far: {}", totalResponseCnt);
                }
            }
        }
        catch (JMSException e) {
            S4JAdapterUtil.processMsgErrorHandling(
                e,
                s4JSpace.isStrictMsgErrorHandling(),
                "Unexpected errors when async sending a JMS message.");
        }
    }

    @Override
    public void onException(Message message, Exception e) {
        try {
            if (logger.isDebugEnabled()) {
                // for testing purpose
                String myMsgSeq = message.getStringProperty(S4JAdapterUtil.NB_MSG_SEQ_PROP);

                logger.debug("onException::Async message send failed - message ID {} ({}) "
                    , message.getJMSMessageID(), myMsgSeq);
            }
        }
        catch (JMSException jmsException) {
            logger.warn("onException::Unexpected error: " + jmsException.getMessage());
        }
    }
}
