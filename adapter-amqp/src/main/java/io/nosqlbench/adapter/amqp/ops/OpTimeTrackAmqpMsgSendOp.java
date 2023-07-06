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

package io.nosqlbench.adapter.amqp.ops;


import com.rabbitmq.client.Channel;
import io.nosqlbench.adapter.amqp.AmqpSpace;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterUnexpectedException;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterMetrics;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;


public class OpTimeTrackAmqpMsgSendOp extends AmqpTimeTrackOp {

    private final static Logger logger = LogManager.getLogger(OpTimeTrackAmqpMsgSendOp.class);

    private final String routingKey;
    private final boolean publishConfirm;
    private final String confirmMode;
    private final int confirmBatchNum;

    private static final ConcurrentHashMap<Channel, Integer>
        channelPublishConfirmBathTracking = new ConcurrentHashMap<>();

    public OpTimeTrackAmqpMsgSendOp(AmqpAdapterMetrics amqpAdapterMetrics,
                                    AmqpSpace amqpSpace,
                                    Channel channel,
                                    String exchangeName,
                                    String message,
                                    String routingKey,
                                    boolean publishConfirm,
                                    String confirmMode,
                                    int confirmBatchNum) {
        super(amqpAdapterMetrics, amqpSpace, channel, exchangeName);
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
            if (logger.isTraceEnabled()) {
                logger.trace("Successfully published message (({}) {}) via the current channel: {}",
                    cycle, msgPayload, channel);
            }

            if (publishConfirm) {
                // Individual publish confirm
                if (StringUtils.containsIgnoreCase(confirmMode, AmqpAdapterUtil.AMQP_PUB_CONFIRM_MODE.INDIVIDUAL.label)) {
                    channel.waitForConfirms(AmqpAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_TIMEOUT_MS);
                    if (logger.isTraceEnabled()) {
                        logger.debug("Sync ack received for an individual published message: {}", cycle);
                    }
                }
                // Batch publish confirm
                else if (StringUtils.containsIgnoreCase(confirmMode, AmqpAdapterUtil.AMQP_PUB_CONFIRM_MODE.BATCH.label)) {
                    int publishConfirmTrackingCnt =
                        channelPublishConfirmBathTracking.getOrDefault(channel, 0);

                    if ( (publishConfirmTrackingCnt > 0) &&
                         ( (publishConfirmTrackingCnt % (confirmBatchNum - 1) == 0)  ||
                           (publishConfirmTrackingCnt == (amqpSpace.getTotalCycleNum() - 1)) ) )  {
                        channel.waitForConfirms(AmqpAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_TIMEOUT_MS);
                        if (logger.isTraceEnabled()) {
                            logger.debug("Sync ack received for a batch of published message: {}, {}",
                                cycle, publishConfirmTrackingCnt);
                        }
                    }
                    else {
                        channelPublishConfirmBathTracking.put(channel, publishConfirmTrackingCnt+1);
                    }
                }
                // Async publish confirm
                // - Do nothing here. See "channel.addConfirmListener" code in 'AmqpMsgSendOpDispenser'
            }
        }
        catch (IllegalStateException ex) {
            throw  new AmqpAdapterUnexpectedException(
                "Wait for confirm on a wrong non-confirm channel: " + channel);
        }
        catch (InterruptedException | TimeoutException ex) {
            throw  new AmqpAdapterUnexpectedException(
                "Failed to wait for the ack of the published message (" + msgPayload
                    + ") via the current channel: " + channel);
        }
        catch (IOException ex) {
            throw  new AmqpAdapterUnexpectedException(
                "Failed to publish message (" + msgPayload
                    + ") via the current channel: " + channel);
        }
    }
}
