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

import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

public class S4JMsgBrowseOp extends S4JTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(S4JMsgBrowseOp.class);
    private final QueueBrowser jmsQueueBrowser;

    private final S4JActivity s4JActivity;

    public S4JMsgBrowseOp(long curNBCycleNum,
                          S4JActivity s4JActivity,
                          JMSContext jmsContext,
                          QueueBrowser browser) {
        super(curNBCycleNum, s4JActivity.getS4JActivityStartTimeMills(), s4JActivity.getMaxS4JOpTimeInSec());
        this.s4JActivity = s4JActivity;
        this.jmsQueueBrowser = browser;
    }

    @Override
    public void run() {
        long timeElapsedMills = System.currentTimeMillis() - s4jOpStartTimeMills;

        // If maximum S4J operation duration is specified, only browsing messages
        // before the maximum duration threshold is reached. Otherwise, this is
        // just no-op.
        if ( (maxS4jOpDurationInSec == 0) || (timeElapsedMills <= (maxS4jOpDurationInSec*1000)) ) {
            try {
                Enumeration<Message> enumeration = jmsQueueBrowser.getEnumeration();

                if (enumeration != null) {
                    for (Iterator<Message> it = enumeration.asIterator(); it.hasNext(); ) {
                        Message msg = it.next();
                        if (logger.isTraceEnabled()) {
                            if (msg != null) {
                                byte[] msgBody = msg.getBody(byte[].class);
                                logger.trace("browsing message with payload={}", Arrays.toString(msgBody));
                            }
                        }
                    }
                }
            } catch (JMSException | JMSRuntimeException e) {
                S4JActivityUtil.processMsgErrorHandling(
                    e,
                    s4JActivity.isStrictMsgErrorHandling(),
                    "Unexpected errors when browsing JMS messages.");
            }
        }
        else {
            if (logger.isTraceEnabled()) {
                logger.trace("NB cycle number {} is no-op (maxS4jOpDuration: {}, timeElapsedMills: {})",
                    curNBCycleNum, maxS4jOpDurationInSec, timeElapsedMills);
            }
        }
    }
}
