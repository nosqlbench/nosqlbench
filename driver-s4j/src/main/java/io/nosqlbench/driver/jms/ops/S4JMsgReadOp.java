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

public class S4JMsgReadOp extends S4JTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(S4JMsgReadOp.class);

    private final S4JSpace s4JSpace;
    private final S4JActivity s4JActivity;
    private final JMSContext jmsContext;
    private final boolean asyncApi;
    private final boolean blockingMsgRecv;
    private final JMSConsumer jmsConsumer;
    private final float msgAckRatio;
    private final long msgReadTimeout;
    private final boolean recvNoWait;
    private final boolean commitTransact;

    private final Counter bytesCounter;
    private final Histogram messageSizeHistogram;

    public S4JMsgReadOp(long curNBCycleNum,
                        S4JSpace s4JSpace,
                        S4JActivity s4JActivity,
                        JMSContext jmsContext,
                        boolean asyncApi,
                        boolean blockingMsgRecv,
                        JMSConsumer consumer,
                        float msgAckRatio,
                        long readTimeout,
                        boolean recvNoWait,
                        boolean commitTransact) {
        super(curNBCycleNum, s4JActivity.getS4JActivityStartTimeMills(), s4JActivity.getMaxS4JOpTimeInSec());

        this.s4JSpace = s4JSpace;
        this.s4JActivity = s4JActivity;
        this.jmsContext = jmsContext;
        this.asyncApi = asyncApi;
        this.blockingMsgRecv = blockingMsgRecv;
        this.jmsConsumer = consumer;
        this.msgAckRatio = msgAckRatio;
        this.msgReadTimeout = readTimeout;
        this.recvNoWait = recvNoWait;
        this.commitTransact = commitTransact;

        this.bytesCounter = s4JActivity.getBytesCounter();
        this.messageSizeHistogram = s4JActivity.getMessagesizeHistogram();
    }

    @Override
    public void run() {
        long timeElapsedMills = System.currentTimeMillis() - s4jOpStartTimeMills;

        // If maximum S4J operation duration is specified, only receive messages
        // before the maximum duration threshold is reached. Otherwise, this is
        // just no-op.
        if ( (maxS4jOpDurationInSec == 0) || (timeElapsedMills <= (maxS4jOpDurationInSec*1000)) ) {

            // Please see S4JSpace::getOrCreateJmsConsumer() for async processing
            if (!asyncApi) {
                Message recvdMsg;

                try {
                    // blocking message receiving only applies to synchronous API
                    if (blockingMsgRecv) {
                        recvdMsg = jmsConsumer.receive();
                    } else if (recvNoWait) {
                        recvdMsg = jmsConsumer.receiveNoWait();
                    } else {
                        // timeout value 0 means to wait forever
                        recvdMsg = jmsConsumer.receive(msgReadTimeout);
                    }
                    if (this.commitTransact) jmsContext.commit();

                    if (recvdMsg != null) {
                        s4JActivity.processMsgAck(jmsContext, recvdMsg, msgAckRatio);

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

                        if (s4JActivity.isTrackingMsgRecvCnt()) {
                            s4JSpace.incTotalOpResponseCnt();
                        }
                    } else {
                        if (s4JActivity.isTrackingMsgRecvCnt()) {
                            s4JSpace.incTotalNullMsgRecvdCnt();
                        }
                    }
                } catch (JMSException e) {
                    e.printStackTrace();
                    throw new S4JDriverUnexpectedException("Unexpected errors when sync receiving a JMS message.");
                }
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("NB cycle number {} is no-op (maxS4jOpDurationInSec: {}, timeElapsedMills: {})",
                    curNBCycleNum, maxS4jOpDurationInSec, timeElapsedMills);
            }
        }
    }
}
