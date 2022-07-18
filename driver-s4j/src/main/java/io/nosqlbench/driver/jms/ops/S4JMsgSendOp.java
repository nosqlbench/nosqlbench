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
import io.nosqlbench.driver.jms.excption.S4JDriverUnexpectedException;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

public class S4JMsgSendOp extends S4JTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(S4JMsgSendOp.class);

    private final S4JSpace s4JSpace;
    private final S4JActivity s4JActivity;
    private final JMSContext jmsContext;
    private final Destination destination;
    private final boolean asyncApi;
    private final JMSProducer jmsProducer;
    private final Message message;
    private final boolean commitTransact;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;
    public S4JMsgSendOp(S4JSpace s4JSpace,
                        S4JActivity s4JActivity,
                        JMSContext jmsContext,
                        Destination destination,
                        boolean asyncApi,
                        JMSProducer producer,
                        Message message,
                        boolean commitTransact) {
        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JActivity;
        this.jmsContext = jmsContext;
        this.destination = destination;
        this.asyncApi = asyncApi;
        this.jmsProducer = producer;
        this.message = message;
        this.commitTransact = commitTransact;

        this.bytesCounter = s4JActivity.getBytesCounter();
        this.messageSizeHistogram = s4JActivity.getMessagesizeHistogram();
    }

    @Override
    public void run() {
        try {
            jmsProducer.send(destination, message);
            if (this.commitTransact) {
                jmsContext.commit();
            }

            int msgSize = message.getIntProperty(S4JActivityUtil.NB_MSG_SIZE_PROP);
            this.bytesCounter.inc(msgSize);
            this.messageSizeHistogram.update(msgSize);

            // Please see S4JActivity::getOrCreateJmsProducer() for async processing
            if (!asyncApi) {
                if (logger.isDebugEnabled()) {
                    // for testing purpose
                    String myMsgSeq = message.getStringProperty(S4JActivityUtil.NB_MSG_SEQ_PROP);
                    logger.debug("Sync message send successful - message ID {} ({}) "
                        , message.getJMSMessageID(), myMsgSeq);
                }

                s4JSpace.incTotalOpResponseCnt();
            }
        } catch (JMSException e) {
            e.printStackTrace();
            throw new S4JDriverUnexpectedException("Unexpected errors when sync receiving a JMS message.");
        }
    }
}
