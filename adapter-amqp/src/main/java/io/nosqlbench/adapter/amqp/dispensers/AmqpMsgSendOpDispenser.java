/*
 * Copyright (c) 2022-2023 nosqlbench
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

package io.nosqlbench.adapter.amqp.dispensers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.nosqlbench.adapter.amqp.AmqpSpace;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterInvalidParamException;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterUnexpectedException;
import io.nosqlbench.adapter.amqp.ops.OpTimeTrackAmqpMsgSendOp;
import io.nosqlbench.adapter.amqp.ops.AmqpTimeTrackOp;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterUtil;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public class AmqpMsgSendOpDispenser extends AmqpBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger(AmqpMsgSendOpDispenser.class);

    private final boolean publisherConfirm ;
    // Only relevant when 'publisherConfirm' is true
    // - default to "individual" confirm
    private final String confirmMode;

    // Only relevant when 'publisherConfirm' is true and 'confirmMode' is 'batch'
    // - default to 100
    private int confirmBatchNum;

    private final LongFunction<String> routingKeyFunc;
    private final LongFunction<String> msgPayloadFunc;

    public AmqpMsgSendOpDispenser(DriverAdapter adapter,
                                  ParsedOp op,
                                  AmqpSpace amqpSpace) {
        super(adapter, op, amqpSpace);

        publisherConfirm = parsedOp
            .getOptionalStaticConfig("publisher_confirm", String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(BooleanUtils::toBoolean)
            .orElse(false);

        confirmMode = parsedOp
            .getOptionalStaticValue("confirm_mode", String.class)
            .orElse(AmqpAdapterUtil.AMQP_PUB_CONFIRM_MODE.INDIVIDUAL.label);
        if (! AmqpAdapterUtil.AMQP_PUB_CONFIRM_MODE.isValidLabel(confirmMode)) {
            throw new AmqpAdapterInvalidParamException("confirm_mode",
                "The provided value \"" + confirmMode + "\" is not one of following valid values: '" +
                    AmqpAdapterUtil.getValidAmqpPublisherConfirmModeList() + "'");
        }

        confirmBatchNum = parsedOp
            .getOptionalStaticConfig("confirm_batch_num", String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(NumberUtils::toInt)
            .orElse(AmqpAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_BATCH_NUM);
        if (confirmBatchNum < AmqpAdapterUtil.AMQP_PUBLISH_CONFIRM_BATCH_NUM_MIN) {
            confirmBatchNum = AmqpAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_BATCH_NUM;
        }

        routingKeyFunc = lookupOptionalStrOpValueFunc("routing_key", null);

        msgPayloadFunc = lookupMandtoryStrOpValueFunc("message");
    }

    private long getExchangeSenderSeqNum(long cycle) {
        return (cycle / ((long) amqpSpace.getAmqpConnNum() *
                                amqpSpace.getAmqpConnChannelNum() *
                                amqpSpace.getAmqpChannelExchangeNum())
               ) % amqpSpace.getAmqpMsgClntNum();
    }

    private String getEffectiveSenderNameByCycle(long cycle) {
        return getEffectiveSenderNameByCycle(
            getConnSeqNum(cycle),
            getConnChannelSeqNum(cycle),
            getChannelExchangeSeqNum(cycle),
            getExchangeSenderSeqNum(cycle));
    }
    private String getEffectiveSenderNameByCycle(long connSeqNum,
                                                 long channelSeqNum,
                                                 long exchangeSeqNum,
                                                 long senderSeqNum) {
        return String.format(
            "sender-%d-%d-%d-%d",
            connSeqNum,
            channelSeqNum,
            exchangeSeqNum,
            senderSeqNum);
    }

    private Channel getAmqpChannelForSender(long cycle) {
        long connSeqNum = getConnSeqNum(cycle);
        long channelSeqNum = getConnChannelSeqNum(cycle);

        Connection amqpConnection = amqpSpace.getAmqpConnection(connSeqNum);
        AmqpSpace.AmqpChannelKey senderKey = new AmqpSpace.AmqpChannelKey(connSeqNum, channelSeqNum);

        return amqpSpace.getAmqpChannels(senderKey, () -> {
            Channel channel = null;

            try {
                channel = amqpConnection.createChannel();
                if (logger.isDebugEnabled()) {
                    logger.debug("Created channel for amqp connection: {}, channel: {}",
                        amqpConnection, channel);
                }
            }
            catch (IOException ex) {
                // Do not throw exception here, just log it and return null
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to create channel for amqp connection: " + amqpConnection, ex);
                }
            }

            try {
                if ((channel != null) && publisherConfirm) {
                    channel.confirmSelect();

                    if (StringUtils.equalsIgnoreCase(confirmMode, AmqpAdapterUtil.AMQP_PUB_CONFIRM_MODE.ASYNC.label)) {
                        channel.addConfirmListener((sequenceNumber, multiple) -> {
                            // code when message is confirmed
                            if (logger.isTraceEnabled()) {
                                logger.debug("Async ack received for a published message: {}, {}",
                                    sequenceNumber, multiple);
                            }
                        }, (sequenceNumber, multiple) -> {
                            // code when message is nack-ed
                            if (logger.isTraceEnabled()) {
                                logger.debug("Async n-ack received of a published message: {}, {}",
                                    sequenceNumber, multiple);
                            }
                        });
                    }

                    if (logger.isTraceEnabled()) {
                        logger.debug("Publisher Confirms is enabled on AMQP channel: {}({}), {}",
                            confirmMode,
                            confirmBatchNum,
                            channel);
                    }
                }

            } catch (IOException ex) {
                throw new AmqpAdapterUnexpectedException(
                    "Failed to enable publisher acknowledgement on the AMQP channel (" +
                        channel + ")!");
            }

            return channel;
        });
    }

    @Override
    public AmqpTimeTrackOp apply(long cycle) {
        String msgPayload = msgPayloadFunc.apply(cycle);
        if (StringUtils.isBlank(msgPayload)) {
            throw new AmqpAdapterInvalidParamException("Message payload must be specified and can't be empty!");
        }

        Channel channel = getAmqpChannelForSender(cycle);
        if (channel == null) {
            throw new AmqpAdapterUnexpectedException(
                String.format(
                    "Failed to get AMQP channel for sender %s [%d]!",
                    getEffectiveSenderNameByCycle(cycle),
                    cycle));
        }

        String exchangeName = getEffectiveExchangeNameByCycle(cycle);
        declareExchange(channel, exchangeName, amqpSpace.getAmqpExchangeType());

        return new OpTimeTrackAmqpMsgSendOp(
            amqpAdapterMetrics,
            amqpSpace,
            channel,
            exchangeName,
            msgPayload,
            routingKeyFunc.apply(cycle),
            publisherConfirm,
            confirmMode,
            confirmBatchNum);
    }
}
