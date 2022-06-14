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

public class S4JMsgSendOp extends S4JTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(S4JMsgSendOp.class);

    private final S4JActivity s4JActivity;
    private final JMSContext jmsContext;
    private final Destination destination;
    private final boolean asyncApi;
    private final JMSProducer jmsProducer;
    private final Message message;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;
    public S4JMsgSendOp(S4JActivity s4JActivity,
                        Destination destination,
                        boolean asyncApi,
                        JMSProducer producer,
                        Message message) {
        this.s4JActivity = s4JActivity;
        this.jmsContext = s4JActivity.getJmsContext();
        this.destination = destination;
        this.asyncApi = asyncApi;
        this.jmsProducer = producer;
        this.message = message;

        this.bytesCounter = s4JActivity.getBytesCounter();
        this.messageSizeHistogram = s4JActivity.getMessagesizeHistogram();
    }

    @Override
    public void run() {
        try {
            jmsProducer.send(destination, message);

            byte[] msgPayload = message.getBody(byte[].class);
            long msgSize = msgPayload.length;
            this.bytesCounter.inc(msgSize);
            this.messageSizeHistogram.update(msgSize);

            if (!asyncApi) {
                if (logger.isDebugEnabled()) {
                    // for testing purpose
                    String myMsgSeq = message.getStringProperty("MyMsgSeq");

                    logger.debug("Sync message send successful - message ID {} ({}) "
                        , message.getJMSMessageID(), myMsgSeq);
                }
            }

        } catch (MessageFormatRuntimeException mfre) {
            logger.error("Invalid message is specified - " + mfre.getMessage());
        } catch (InvalidDestinationRuntimeException idre) {
            logger.error("Invalid destination is specified - " + idre.getMessage());
        } catch (MessageNotWriteableRuntimeException mnwre) {
            logger.error("Set value to a read-only message property - " + mnwre.getMessage());
        } catch (JMSRuntimeException jre) {
            logger.error("An internal error happened when sending the message - " + jre.getMessage());
        } catch (JMSException je) {
            logger.error("Unexpected error - " + je.getMessage());
        }
    }
}
