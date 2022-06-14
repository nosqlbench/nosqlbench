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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;

public class S4JMsgReadOp extends S4JTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(S4JMsgReadOp.class);

    private final S4JActivity s4JActivity;
    private final JMSContext jmsContext;
    private final int jmsSessionMode;
    private final Destination destination;
    private final boolean asyncApi;
    private final JMSConsumer jmsConsumer;
    private final long msgReadTimeout;
    private final boolean recvNoWait;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;

    public S4JMsgReadOp(S4JActivity s4JActivity,
                        Destination destination,
                        boolean asyncApi,
                        JMSConsumer consumer,
                        long readTimeout,
                        boolean recvNoWait) {
        this.s4JActivity = s4JActivity;
        this.jmsContext = s4JActivity.getJmsContext();
        this.jmsSessionMode = jmsContext.getSessionMode();
        this.destination = destination;
        this.asyncApi = asyncApi;
        this.jmsConsumer = consumer;
        this.msgReadTimeout = readTimeout;
        this.recvNoWait = recvNoWait;

        this.bytesCounter = s4JActivity.getBytesCounter();
        this.messageSizeHistogram = s4JActivity.getMessagesizeHistogram();
    }

    @Override
    public void run() {

        // For async API, message will be handled by the message listener
        if (!asyncApi) {
            Message recvdMsg;

            // By default, if message read time out value is 0, it will block forever
            // Simulate it as the case for recvNoWait
            if (recvNoWait || (msgReadTimeout == 0)) {
                recvdMsg = jmsConsumer.receiveNoWait();
            } else {
                recvdMsg = jmsConsumer.receive(msgReadTimeout);
            }

            try {
                if (recvdMsg != null) {
                    if (jmsSessionMode == Session.CLIENT_ACKNOWLEDGE) {
                        recvdMsg.acknowledge();
                    }

                    byte[] recvdMsgBody = recvdMsg.getBody(byte[].class);
                    int messageSize = recvdMsgBody.length;
                    bytesCounter.inc(messageSize);
                    messageSizeHistogram.update(messageSize);

                    if (logger.isDebugEnabled()) {
                        // for testing purpose
                        String myMsgSeq = recvdMsg.getStringProperty("MyMsgSeq");

                        logger.debug("Sync message receive successful - message ID {} ({}) "
                            , recvdMsg.getJMSMessageID(), myMsgSeq);
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to acknowledge the received JMS message.");
            }
        }
    }
}
