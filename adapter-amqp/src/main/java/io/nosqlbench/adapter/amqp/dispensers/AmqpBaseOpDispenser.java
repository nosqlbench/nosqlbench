/*
 * Copyright (c) 2023 nosqlbench
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
import io.nosqlbench.adapter.amqp.AmqpSpace;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterUnexpectedException;
import io.nosqlbench.adapter.amqp.ops.AmqpTimeTrackOp;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterMetrics;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public abstract  class AmqpBaseOpDispenser extends BaseOpDispenser<AmqpTimeTrackOp, AmqpSpace> {

    private static final Logger logger = LogManager.getLogger(AmqpBaseOpDispenser.class);

    protected final ParsedOp parsedOp;
    protected final AmqpAdapterMetrics s4rAdapterMetrics;
    protected final AmqpSpace s4rSpace;

    protected final Map<String, String> s4rConfMap = new HashMap<>();
    protected final String exchangeType;
    protected AmqpBaseOpDispenser(final DriverAdapter adapter,
                                  final ParsedOp op,
                                  final AmqpSpace s4RSpace) {

        super(adapter, op);

        parsedOp = op;
        this.s4rSpace = s4RSpace;

        s4rAdapterMetrics = new AmqpAdapterMetrics(this, this);
        s4rAdapterMetrics.initS4JAdapterInstrumentation();

        s4rConfMap.putAll(s4RSpace.getS4rClientConf().getS4rConfMap());

        this.exchangeType = s4RSpace.getAmqpExchangeType();

        s4rSpace.setTotalCycleNum(NumberUtils.toLong(this.parsedOp.getStaticConfig("cycles", String.class)));
        s4rSpace.setTotalThreadNum(NumberUtils.toInt(this.parsedOp.getStaticConfig("threads", String.class)));
    }

    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse(l -> defaultValue);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    protected void declareExchange(Channel channel, String exchangeName, String exchangeType) {
        try {
            // Declaring the same exchange multiple times on one channel is considered as a no-op
            channel.exchangeDeclare(exchangeName, exchangeType);
            if (logger.isTraceEnabled()) {
                logger.debug("Declared the AMQP exchange \"{}\" on channel \"{}\".",
                    exchangeName, channel);
            }
        } catch (IOException e) {
            String errMsg = String.format("Failed to declare the AMQP exchange \"%s\" on channel \"%s\"!",
                exchangeName, channel);
            throw new AmqpAdapterUnexpectedException(errMsg);
        }
    }

    protected long getConnSeqNum(long cycle) {
        return cycle % s4rSpace.getAmqpConnNum();
    }

    protected long getConnChannelSeqNum(long cycle) {
        return (cycle / s4rSpace.getAmqpConnNum()) % s4rSpace.getAmqpConnChannelNum();
    }

    protected long getChannelExchangeSeqNum(long cycle) {
        return (cycle / ((long) s4rSpace.getAmqpConnNum() *
                                s4rSpace.getAmqpConnChannelNum())
               ) % s4rSpace.getAmqpChannelExchangeNum();
    }

    protected String getEffectiveExchangeNameByCycle(long cycle) {
        return getEffectiveExchangeName(
            getConnSeqNum(cycle),
            getConnChannelSeqNum(cycle),
            getChannelExchangeSeqNum(cycle));
    }
    protected String getEffectiveExchangeName(long connSeqNum, long channelSeqNum,  long exchangeSeqNum) {
        return String.format(
            "exchange-%d-%d-%d",
            connSeqNum,
            channelSeqNum,
            exchangeSeqNum);
    }

    public String getName() {
        return "AmqpBaseOpDispenser";
    }
}
