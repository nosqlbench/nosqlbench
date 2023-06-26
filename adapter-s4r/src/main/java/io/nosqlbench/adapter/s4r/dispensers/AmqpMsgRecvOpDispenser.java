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
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackAmqpMsgRecvOp;
import io.nosqlbench.adapter.s4r.ops.S4RTimeTrackOp;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.function.LongFunction;

public class AmqpMsgRecvOpDispenser extends AmqpBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("AmqpMsgRecvOpDispenser");

    private final LongFunction<String> bindingKeyFunc;
    public AmqpMsgRecvOpDispenser(DriverAdapter adapter,
                                  ParsedOp op,
                                  S4RSpace s4rSpace) {
        super(adapter, op, s4rSpace);
        bindingKeyFunc = lookupOptionalStrOpValueFunc("binding_key", null);
    }

    private long getExchangeQueueSeqNum(long cycle) {
        return (cycle / ((long) s4rSpace.getAmqpConnNum() *
                                s4rSpace.getAmqpConnChannelNum() *
                                s4rSpace.getAmqpChannelExchangeNum())
               ) % s4rSpace.getAmqpExchangeQueueNum();
    }

    private long getQueueReceiverSeqNum(long cycle) {
        return (cycle / ((long) s4rSpace.getAmqpConnNum() *
                                s4rSpace.getAmqpConnChannelNum() *
                                s4rSpace.getAmqpChannelExchangeNum() *
                                s4rSpace.getAmqpExchangeQueueNum())
               ) % s4rSpace.getAmqpMsgClntNum();
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

        Connection amqpConnection = s4rSpace.getAmqpConnection(connSeqNum);
        S4RSpace.AmqpChannelKey amqpConnChannelKey = new S4RSpace.AmqpChannelKey(connSeqNum, channelSeqNum);

        return s4rSpace.getAmqpChannels(amqpConnChannelKey, () -> {
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
    public S4RTimeTrackOp apply(long cycle) {
        Channel channel = getAmqpChannelForReceiver(cycle);
        if (channel == null) {
            throw new S4RAdapterUnexpectedException(
                String.format(
                    "Failed to get AMQP channel for receiver %s [%d]!",
                    getEffectiveReceiverName(cycle),
                    cycle));
        }

        String exchangeName = getEffectiveExchangeNameByCycle(cycle);
        declareExchange(channel, exchangeName, s4rSpace.getAmqpExchangeType());

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
            throw new S4RAdapterUnexpectedException(
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
            throw new S4RAdapterUnexpectedException(
                String.format(
                    "Unable to bind the AMQP queue - \"%s (%b/%b/%b)\" on exchange \"%s\" with binding key \"%s\"!",
                    queueName, durable, exclusive, autoDelete, exchangeName, bindingKey)
            );
        }

        return new OpTimeTrackAmqpMsgRecvOp(
            s4rAdapterMetrics,
            s4rSpace,
            channel,
            exchangeName,
            queueName);
    }
}
