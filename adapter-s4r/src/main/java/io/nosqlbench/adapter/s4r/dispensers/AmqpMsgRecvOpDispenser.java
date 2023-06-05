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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.ops.OpTimeTrackAmqpMsgRecvOp;
import io.nosqlbench.adapter.s4r.ops.S4RTimeTrackOp;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.function.LongFunction;

public class AmqpMsgRecvOpDispenser extends AmqpBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("AmqpMsgRecvOpDispenser");

    private final LongFunction<String> bindingKeyFunc;
    private final LongFunction<String> queueNameFunc;
    public AmqpMsgRecvOpDispenser(DriverAdapter adapter,
                                  ParsedOp op,
                                  S4RSpace s4rSpace) {
        super(adapter, op, s4rSpace);

        queueNameFunc = lookupOptionalStrOpValueFunc("queue_name", null);
        bindingKeyFunc = lookupOptionalStrOpValueFunc("binding_key", null);
    }

    private long getExchangeQueueSeqNum(long cycle) {
        return (cycle / ((long) s4rSpace.getAmqpConnNum() * s4rSpace.getAmqpConnChannelNum()))
            % s4rSpace.getAmqpExchangeQueueNum();
    }

    private long getQueueReceiverSeqNum(long cycle) {
        return (cycle / ((long) s4rSpace.getAmqpConnNum() * s4rSpace.getAmqpConnChannelNum() * s4rSpace.getAmqpExchangeQueueNum()))
            % s4rSpace.getAmqpMsgClntNum();
    }

    private String getEffectiveQueueName(long cycle) {
        String queueNameInput = queueNameFunc.apply(cycle);
        return (StringUtils.isBlank(queueNameInput) ? "queue-" + getExchangeQueueSeqNum(cycle) : queueNameInput);
    }

    private Channel getAmqpChannelQueueForReceiver(long cycle,
                                                   String exchangeName,
                                                   String queueName) {
        long connSeqNum = getConnSeqNum(cycle);
        long channelSeqNum = getConnChannelSeqNum(cycle);
        long queueSeqNum = getExchangeQueueSeqNum(cycle);
        long receiverSeqNum = getQueueReceiverSeqNum(cycle);

        Connection amqpConnection = s4rSpace.getAmqpConnection(cycle % connSeqNum);

        S4RSpace.AmqpReceiverChannelKey amqpConnChannelKey =
            new S4RSpace.AmqpReceiverChannelKey(connSeqNum, channelSeqNum, queueSeqNum, receiverSeqNum);

        return s4rSpace.getAmqpReceiverChannel(amqpConnChannelKey, () -> {
            Channel channel = null;

            try {
                channel = getChannelWithExchange(
                    amqpConnection,
                    connSeqNum,
                    channelSeqNum,
                    exchangeName);

                AMQP.Queue.DeclareOk declareOk =
                    channel.queueDeclare(queueName, true, true, true, null);
                if (logger.isDebugEnabled()) {
                    logger.debug("AMQP queue declared -- [exchange name: {}, queue name: {}] {}",
                        exchangeName,
                        queueName,
                        declareOk);
                }
            } catch (IOException ex) {
                throw new S4RAdapterUnexpectedException("Unexpected error when creating the AMQP channel!");
            }

            return channel;
        });
    }

    @Override
    public S4RTimeTrackOp apply(long cycle) {
        Channel channel = null;

        String exchangeName = getEffectiveExchangeName(cycle);
        String queueName = getEffectiveQueueName(cycle);

        try {
            channel = getAmqpChannelQueueForReceiver(cycle, exchangeName, queueName);
        }
        catch (Exception ex) {
            throw new S4RAdapterUnexpectedException("Unable to create the AMQP channel!");
        }

        return new OpTimeTrackAmqpMsgRecvOp(
            s4rAdapterMetrics,
            s4rSpace,
            channel,
            exchangeName,
            queueName,
            bindingKeyFunc.apply(cycle));
    }
}
