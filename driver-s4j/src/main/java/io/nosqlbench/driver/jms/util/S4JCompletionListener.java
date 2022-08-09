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

import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.S4JSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

/**
 *  Used for async message production
 */
public class S4JCompletionListener implements CompletionListener {

    private final static Logger logger = LogManager.getLogger(S4JSpace.class);

    private final S4JSpace s4JSpace;
    private final S4JActivity s4JActivity;

    public S4JCompletionListener(S4JSpace s4JSpace) {
        assert (s4JSpace != null);
        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JSpace.getS4JActivity();
    }

    @Override
    public void onCompletion(Message message) {
        try {
            if (logger.isTraceEnabled()) {
                // for testing purpose
                String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);
                logger.trace("onCompletion::Async message send successful - message ID {} ({}) "
                    , message.getJMSMessageID(), myMsgSeq);
            }

            if (s4JActivity.isTrackingMsgRecvCnt() ) {
                long totalResponseCnt = s4JSpace.incTotalOpResponseCnt();
                if (logger.isTraceEnabled()) {
                    logger.trace("... async op response received so far: {}", totalResponseCnt);
                }
            }
        }
        catch (JMSException jmsException) {
            logger.warn("onCompletion::Error retrieving message property - {}", jmsException.getMessage());
        }
    }

    @Override
    public void onException(Message message, Exception e) {
        try {
            if (logger.isDebugEnabled()) {
                // for testing purpose
                String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);

                logger.debug("onException::Async message send failed - message ID {} ({}) "
                    , message.getJMSMessageID(), myMsgSeq);
            }
        }
        catch (JMSException jmsException) {
            logger.warn("onException::Unexpected error: " + jmsException.getMessage());
        }
    }
}
