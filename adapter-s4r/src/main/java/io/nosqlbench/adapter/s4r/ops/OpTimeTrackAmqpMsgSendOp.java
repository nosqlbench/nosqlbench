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

package io.nosqlbench.adapter.s4r.ops;


import com.rabbitmq.client.Channel;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;


public class OpTimeTrackAmqpMsgSendOp extends S4RTimeTrackOp {

    private final static Logger logger = LogManager.getLogger("OpTimeTrackAmqpMsgSendOp");

    private final String routingKey;
    private final boolean publishConfirm;
    private final String confirmMode;
    private final int confirmBatchNum;

    private static final ThreadLocal<Integer>
        publishConfirmBatchTrackingCnt = ThreadLocal.withInitial(() -> 0);

    public OpTimeTrackAmqpMsgSendOp(S4RAdapterMetrics s4rAdapterMetrics,
                                    S4RSpace s4rSpace,
                                    Channel channel,
                                    String exchangeName,
                                    String message,
                                    String routingKey,
                                    boolean publishConfirm,
                                    String confirmMode,
                                    int confirmBatchNum) {
        super(s4rAdapterMetrics, s4rSpace, channel, exchangeName);
        this.cycleObj = message;
        this.routingKey = routingKey;
        this.publishConfirm = publishConfirm;
        this.confirmMode = confirmMode;
        this.confirmBatchNum = confirmBatchNum;
    }

    @Override
    void cycleMsgProcess(long cycle, Object cycleObj) {
        assert (cycleObj != null);
        assert (cycleObj.getClass().equals(String.class));

        String msgPayload = (String) cycleObj;

        try {
            channel.basicPublish(
                exchangeName,
                routingKey,
                null,
                msgPayload.getBytes(StandardCharsets.UTF_8));

            if (publishConfirm) {
                // Individual publish confirm
                if (StringUtils.containsIgnoreCase(confirmMode, S4RAdapterUtil.AMQP_PUB_CONFIRM_MODE.INDIVIDUAL.label)) {
                    channel.waitForConfirms(S4RAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_TIMEOUT_MS);
                }
                // Batch publish confirm
                else if (StringUtils.containsIgnoreCase(confirmMode, S4RAdapterUtil.AMQP_PUB_CONFIRM_MODE.BATCH.label)) {
                    int publishConfirmTrackingCnt = publishConfirmBatchTrackingCnt.get();
                    if ( (publishConfirmTrackingCnt > 0) &&
                         ( (publishConfirmTrackingCnt % (confirmBatchNum - 1) == 0)  ||
                           (publishConfirmTrackingCnt == (s4RSpace.getTotalCycleNum() - 1)) ) )  {
                        synchronized (this) {
                            channel.waitForConfirms(S4RAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_TIMEOUT_MS);
                        }
                    }
                    else {
                        publishConfirmBatchTrackingCnt.set(publishConfirmTrackingCnt+1);
                    }
                }
                // Async publish confirm
                // - Do nothing here. See "channel.addConfirmListener" code in 'AmqpMsgSendOpDispenser'
            }

            if (logger.isTraceEnabled()) {
                logger.trace("Successfully published message ({}) via the current channel: {}",
                    msgPayload, channel);
            }
        }
        catch (IllegalStateException ex) {
            throw  new S4RAdapterUnexpectedException(
                "Wait for confirm on a wrong non-confirm channel: " + channel);
        }
        catch (InterruptedException | TimeoutException ex) {
            throw  new S4RAdapterUnexpectedException(
                "Failed to wait for the ack of the published message (" + msgPayload
                    + ") via the current channel: " + channel);
        }
        catch (IOException ex) {
            throw  new S4RAdapterUnexpectedException(
                "Failed to publish message (" + msgPayload
                    + ") via the current channel: " + channel);
        }
    }
}
