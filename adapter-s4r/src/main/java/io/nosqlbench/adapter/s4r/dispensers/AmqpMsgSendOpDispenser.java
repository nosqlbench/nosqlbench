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

package io.nosqlbench.adapter.s4r.dispensers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterInvalidParamException;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackAmqpMsgSendOp;
import io.nosqlbench.adapter.s4r.ops.S4RTimeTrackOp;
import io.nosqlbench.adapter.s4r.util.S4RAdapterUtil;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public class AmqpMsgSendOpDispenser extends AmqpBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("AmqpMsgSendOpDispenser");

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
                                  S4RSpace s4rSpace) {
        super(adapter, op, s4rSpace);

        publisherConfirm = parsedOp
            .getOptionalStaticConfig("publisher_confirm", String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(BooleanUtils::toBoolean)
            .orElse(false);

        confirmMode = parsedOp
            .getOptionalStaticValue("confirm_mode", String.class)
            .orElse(S4RAdapterUtil.AMQP_PUB_CONFIRM_MODE.INDIVIDUAL.label);
        if (! S4RAdapterUtil.AMQP_PUB_CONFIRM_MODE.isValidLabel(confirmMode)) {
            throw new S4RAdapterInvalidParamException("confirm_mode",
                "Must be one following valid values: '" + S4RAdapterUtil.getValidAmqpPublisherConfirmModeList() + "'");
        }

        confirmBatchNum = parsedOp
            .getOptionalStaticConfig("confirm_batch_num", String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(NumberUtils::toInt)
            .orElse(S4RAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_BATCH_NUM);
        if (confirmBatchNum < S4RAdapterUtil.AMQP_PUBLISH_CONFIRM_BATCH_NUM_MIN) {
            confirmBatchNum = S4RAdapterUtil.DFT_AMQP_PUBLISH_CONFIRM_BATCH_NUM;
        }

        routingKeyFunc = lookupOptionalStrOpValueFunc("routing_key", null);

        msgPayloadFunc = lookupMandtoryStrOpValueFunc("message");
    }

    private long getExchangeSenderSeqNum(long cycle) {
        return (cycle / ((long) s4rSpace.getAmqpConnNum() * s4rSpace.getAmqpConnChannelNum()))
            % s4rSpace.getAmqpMsgClntNum();
    }

    private Channel getAmqpChannelForSender(long cycle,
                                            String exchangeName) {
        long connSeqNum = getConnSeqNum(cycle);
        long channelSeqNum = getConnChannelSeqNum(cycle);
        long senderSeqNum = getExchangeSenderSeqNum(cycle);

        Connection amqpConnection = s4rSpace.getAmqpConnection(cycle % connSeqNum);

        S4RSpace.AmqpSenderChannelKey amqpConnChannelKey =
            new S4RSpace.AmqpSenderChannelKey(connSeqNum, channelSeqNum, senderSeqNum);

        return s4rSpace.getAmqpSenderChannel(amqpConnChannelKey, () -> {
            Channel channel;

            try {
                channel = getChannelWithExchange(
                    amqpConnection,
                    connSeqNum,
                    channelSeqNum,
                    exchangeName);

                if (publisherConfirm) {
                    channel.confirmSelect();

                    boolean asyncConfirm = false;
                    if (StringUtils.equalsIgnoreCase(confirmMode, S4RAdapterUtil.AMQP_PUB_CONFIRM_MODE.ASYNC.label)) {
                        asyncConfirm = true;

                        channel.addConfirmListener((sequenceNumber, multiple) -> {
                            // code when message is confirmed
                            if (logger.isTraceEnabled()) {
                                logger.debug("Async ack of message publish received: {}, {}",
                                    sequenceNumber, multiple);
                            }
                        }, (sequenceNumber, multiple) -> {
                            // code when message is nack-ed
                            if (logger.isTraceEnabled()) {
                                logger.debug("Async n-ack of message publish received: {}, {}",
                                    sequenceNumber, multiple);
                            }
                        });
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Publisher Confirms enabled on AMQP channel (sync: {}) -- {}",
                            !asyncConfirm,
                            channel);
                    }
                }

            } catch (IOException ex) {
                throw new S4RAdapterUnexpectedException("Unexpected error when creating the AMQP channel!");
            }

            return channel;
        });
    }

    @Override
    public S4RTimeTrackOp apply(long cycle) {
        String msgPayload = msgPayloadFunc.apply(cycle);
        if (StringUtils.isBlank(msgPayload)) {
            throw new S4RAdapterInvalidParamException("Message payload must be specified and can't be empty!");
        }

        Channel channel;
        String exchangeName = getEffectiveExchangeName(cycle);

        try {
            channel = getAmqpChannelForSender(cycle, exchangeName);
        }
        catch (Exception ex) {
            throw new S4RAdapterUnexpectedException("Unable to create the AMQP channel for sending messages!");
        }

        return new OpTimeTrackAmqpMsgSendOp(
            s4rAdapterMetrics,
            s4rSpace,
            channel,
            exchangeName,
            msgPayload,
            routingKeyFunc.apply(cycle),
            publisherConfirm,
            confirmMode,
            confirmBatchNum);
    }
}
