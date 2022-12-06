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

import com.codahale.metrics.Timer;
import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.exception.S4JAdapterAsyncOperationFailedException;
import io.nosqlbench.adapter.s4j.exception.S4JAdapterUnexpectedException;
import io.nosqlbench.adapter.s4j.util.S4JAdapterMetrics;
import io.nosqlbench.adapter.s4j.util.S4JAdapterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.shade.org.apache.avro.AvroRuntimeException;

import javax.jms.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageConsumerOp extends S4JOp {

    private final static Logger logger = LogManager.getLogger(MessageConsumerOp.class);

    private final JMSConsumer jmsConsumer;
    private final boolean blockingMsgRecv;
    private final float msgAckRatio;
    private final long msgReadTimeout;
    private final boolean recvNoWait;
    private final int slowInSec;

    public MessageConsumerOp(S4JAdapterMetrics s4jAdapterMetrics,
                             S4JSpace s4jSpace,
                             JMSContext jmsContext,
                             Destination destination,
                             boolean asyncApi,
                             boolean commitTransact,
                             JMSConsumer jmsConsumer,
                             boolean blockingMsgRecv,
                             float msgAckRatio,
                             long readTimeout,
                             boolean recvNoWait,
                             int slowInSec)
    {
        super(s4jAdapterMetrics, s4jSpace, jmsContext, destination, asyncApi, commitTransact);

        this.jmsConsumer = jmsConsumer;
        this.blockingMsgRecv = blockingMsgRecv;
        this.msgAckRatio = msgAckRatio;
        this.msgReadTimeout = readTimeout;
        this.recvNoWait = recvNoWait;
        this.slowInSec = slowInSec;
    }

    @Override
    public Object apply(long value) {
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
                        s4jSpace.processMsgAck(jmsContext, recvdMsg, msgAckRatio, slowInSec);

                        byte[] recvdMsgBody = recvdMsg.getBody(byte[].class);
                        int messageSize = recvdMsgBody.length;

                        messageSizeHistogram.update(messageSize);

                        if (logger.isDebugEnabled()) {
                            // for testing purpose
                            String myMsgSeq = recvdMsg.getStringProperty(S4JAdapterUtil.NB_MSG_SEQ_PROP);
                            logger.debug("Sync message receive successful - message ID {} ({}) "
                                , recvdMsg.getJMSMessageID(), myMsgSeq);
                        }

                        if (s4jSpace.isTrackingMsgRecvCnt()) {
                            s4jSpace.incTotalOpResponseCnt();
                        }
                    } else {
                        if (s4jSpace.isTrackingMsgRecvCnt()) {
                            s4jSpace.incTotalNullMsgRecvdCnt();
                        }
                    }
                } catch (JMSException | JMSRuntimeException e) {
                    S4JAdapterUtil.processMsgErrorHandling(
                        e,
                        s4jSpace.isStrictMsgErrorHandling(),
                        "Unexpected errors when sync receiving a JMS message.");
                }
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("NB cycle number {} is no-op (maxS4jOpDurationInSec: {}, timeElapsedMills: {})",
                    value, maxS4jOpDurationInSec, timeElapsedMills);
            }
        }


        return  null;
    }
}
