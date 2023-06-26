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


import com.rabbitmq.client.*;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class OpTimeTrackAmqpMsgRecvOp extends S4RTimeTrackOp {

    private final static Logger logger = LogManager.getLogger("OpTimeTrackAmqpMsgRecvOp");
    private final String queueName;


    public OpTimeTrackAmqpMsgRecvOp(S4RAdapterMetrics s4rAdapterMetrics,
                                    S4RSpace s4rSpace,
                                    Channel channel,
                                    String exchangeName,
                                    String queueName) {
        super(s4rAdapterMetrics, s4rSpace, channel, exchangeName);
        this.queueName = queueName;
    }

    @Override
    void cycleMsgProcess(long cycle, Object cycleObj)  {
        try {
            Consumer receiver = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(
                    String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                {
                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    String msgPayload = new String(body, StandardCharsets.UTF_8);

                    if (logger.isTraceEnabled()) {
                        logger.trace(
                            "Successfully received message ({}) via consumer ({}/{}/{}) in the current channel: {}",
                            msgPayload,
                            consumerTag,
                            routingKey,
                            contentType,
                            channel);
                    }
                }
            };

            channel.basicConsume(queueName, true, receiver);
        }
        catch (IOException e) {
            throw  new S4RAdapterUnexpectedException(
                "Failed to receive message via the current channel: " + channel);
        }
    }
}
