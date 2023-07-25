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
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterUnexpectedException;
import io.nosqlbench.adapter.amqp.ops.OpTimeTrackAmqpMsgRecvOp;
import io.nosqlbench.adapter.amqp.ops.AmqpTimeTrackOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.LongFunction;

public class AmqpMsgRecvOpDispenser extends AmqpBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger(AmqpMsgRecvOpDispenser.class);

    private final LongFunction<String> bindingKeyFunc;
    public AmqpMsgRecvOpDispenser(DriverAdapter adapter,
                                  ParsedOp op,
                                  AmqpSpace amqpSpace) {
        super(adapter, op, amqpSpace);
        bindingKeyFunc = lookupOptionalStrOpValueFunc("binding_key", null);
    }

    private long getExchangeQueueSeqNum(long cycle) {
        return (cycle / ((long) amqpSpace.getAmqpConnNum() *
                                amqpSpace.getAmqpConnChannelNum() *
                                amqpSpace.getAmqpChannelExchangeNum())
               ) % amqpSpace.getAmqpExchangeQueueNum();
    }

    private long getQueueReceiverSeqNum(long cycle) {
        return (cycle / ((long) amqpSpace.getAmqpConnNum() *
                                amqpSpace.getAmqpConnChannelNum() *
                                amqpSpace.getAmqpChannelExchangeNum() *
                                amqpSpace.getAmqpExchangeQueueNum())
               ) % amqpSpace.getAmqpMsgClntNum();
    }

    private String getEffectiveQueueNameByCycle(long cycle) {
        return getEffectiveQueueName(
            getConnSeqNum(cycle),
            getConnChannelSeqNum(cycle),
            getChannelExchangeSeqNum(cycle),
            getExchangeQueueSeqNum(cycle));
    }
    private String getEffectiveQueueName(long connSeqNum, long channelSeqNum, long exchangeSeqNum, long queueSeqNum) {
        return "queue-" + connSeqNum + "-" + channelSeqNum + "-" + exchangeSeqNum + "-" + queueSeqNum;
    }

    private String getEffectiveReceiverName(long cycle) {
        return getEffectiveReceiverName(
            getConnSeqNum(cycle),
            getConnChannelSeqNum(cycle),
            getChannelExchangeSeqNum(cycle),
            getExchangeQueueSeqNum(cycle),
            getQueueReceiverSeqNum(cycle));
    }
    private String getEffectiveReceiverName(long connSeqNum,
                                            long channelSeqNum,
                                            long exchangeSeqNum,
                                            long queueSeqNum,
                                            long receiverSeqNum) {
        return String.format(
            "receiver-%d-%d-%d-%d-%d",
            connSeqNum,
            channelSeqNum,
            exchangeSeqNum,
            queueSeqNum,
            receiverSeqNum);
    }

    private Channel getAmqpChannelForReceiver(long cycle) {
        long connSeqNum = getConnSeqNum(cycle);
        long channelSeqNum = getConnChannelSeqNum(cycle);

        Connection amqpConnection = amqpSpace.getAmqpConnection(connSeqNum);
        AmqpSpace.AmqpChannelKey amqpConnChannelKey = new AmqpSpace.AmqpChannelKey(connSeqNum, channelSeqNum);

        return amqpSpace.getAmqpChannels(amqpConnChannelKey, () -> {
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

            return channel;
        });
    }


    @Override
    public AmqpTimeTrackOp apply(long cycle) {
        Channel channel = getAmqpChannelForReceiver(cycle);
        if (channel == null) {
            throw new AmqpAdapterUnexpectedException(
                String.format(
                    "Failed to get AMQP channel for receiver %s [%d]!",
                    getEffectiveReceiverName(cycle),
                    cycle));
        }

        String exchangeName = getEffectiveExchangeNameByCycle(cycle);
        declareExchange(channel, exchangeName, amqpSpace.getAmqpExchangeType());

        boolean durable = true;
        boolean exclusive = true;
        boolean autoDelete = false;
        String queueName = getEffectiveQueueNameByCycle(cycle);
        String bindingKey = bindingKeyFunc.apply(cycle);
        try {
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, null);
            if (logger.isTraceEnabled()) {
                logger.debug("AMQP queue is declared - \"{} ({}/{}/{})\" on exchange \"{}\" for a receiver!",
                    queueName,
                    durable,
                    exclusive,
                    autoDelete,
                    exchangeName);
            }
        }
        catch (IOException ex) {
            throw new AmqpAdapterUnexpectedException(
                String.format(
                    "Unable to declare the AMQP queue - \"%s (%b/%b/%b)\" on exchange \"%s\" for a receiver!",
                    queueName, durable, exclusive, autoDelete, exchangeName)
            );
        }

        try {
            // Binding the same queue multiple times on one exchange is considered as a no-op
            channel.queueBind(queueName, exchangeName, bindingKey);
            if (logger.isTraceEnabled()) {
                logger.debug("AMQP queue is bound - \"{} ({}/{}/{})\" on exchange \"{}\" with binding key \"{}\"!",
                    queueName,
                    durable,
                    exclusive,
                    autoDelete,
                    exchangeName,
                    bindingKey);
            }
        }
        catch (IOException ex) {
            throw new AmqpAdapterUnexpectedException(
                String.format(
                    "Unable to bind the AMQP queue - \"%s (%b/%b/%b)\" on exchange \"%s\" with binding key \"%s\"!",
                    queueName, durable, exclusive, autoDelete, exchangeName, bindingKey)
            );
        }

        return new OpTimeTrackAmqpMsgRecvOp(
            amqpAdapterMetrics,
            amqpSpace,
            channel,
            exchangeName,
            queueName);
    }
}
