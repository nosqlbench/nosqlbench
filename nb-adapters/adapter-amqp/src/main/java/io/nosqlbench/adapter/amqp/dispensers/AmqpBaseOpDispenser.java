/*
 * Copyright (c) nosqlbench
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
import io.nosqlbench.adapter.amqp.AmqpDriverAdapter;
import io.nosqlbench.adapter.amqp.AmqpSpace;
import io.nosqlbench.adapter.amqp.exception.AmqpAdapterUnexpectedException;
import io.nosqlbench.adapter.amqp.ops.AmqpTimeTrackOp;
import io.nosqlbench.adapter.amqp.util.AmqpAdapterMetrics;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.templating.ParsedOp;
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
    protected final AmqpAdapterMetrics amqpAdapterMetrics;

    protected final Map<String, String> amqpConfMap = new HashMap<>();
    protected String exchangeType;

    private boolean configured= false;

    protected AmqpBaseOpDispenser(final AmqpDriverAdapter adapter,
                                  final ParsedOp op) {

        super(adapter, op, adapter.getSpaceFunc(op));
        parsedOp = op;

        amqpAdapterMetrics = new AmqpAdapterMetrics(this, this);
        amqpAdapterMetrics.initS4JAdapterInstrumentation();

    }

    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsFunctionOr(paramName, defaultValue);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    protected void declareExchange(long cycleNum, Channel channel, String exchangeName, String exchangeType) {
        configureDispenser(cycleNum);

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
        AmqpSpace amqpSpace = spaceF.apply(cycle);
        configureDispenser(cycle);
        return cycle % amqpSpace.getAmqpConnNum();
    }

    protected long getConnChannelSeqNum(long cycle) {
        AmqpSpace amqpSpace = spaceF.apply(cycle);
        configureDispenser(cycle);

        return (cycle / amqpSpace.getAmqpConnNum()) % amqpSpace.getAmqpConnChannelNum();
    }

    protected long getChannelExchangeSeqNum(long cycle) {
        AmqpSpace amqpSpace = spaceF.apply(cycle);
        configureDispenser(cycle);

        return (cycle / ((long) amqpSpace.getAmqpConnNum() *
                                amqpSpace.getAmqpConnChannelNum())
               ) % amqpSpace.getAmqpChannelExchangeNum();
    }

    protected String getEffectiveExchangeNameByCycle(long cycle) {
        configureDispenser(cycle);

        return getEffectiveExchangeName(
            cycle,
            getConnSeqNum(cycle),
            getConnChannelSeqNum(cycle),
            getChannelExchangeSeqNum(cycle));
    }
    protected String getEffectiveExchangeName(long cycleNum, long connSeqNum, long channelSeqNum,
                                              long exchangeSeqNum) {
        configureDispenser(cycleNum);

        return String.format(
            "exchange-%d-%d-%d",
            connSeqNum,
            channelSeqNum,
            exchangeSeqNum);
    }

    public String getName() {
        return "AmqpBaseOpDispenser";
    }

    synchronized void configureDispenser(long cycle) {
        if (!configured) {
            AmqpSpace amqpSpace = spaceF.apply(cycle);
            amqpConfMap.putAll(amqpSpace.getAmqpClientConf().getConfigMap());
            this.exchangeType = amqpSpace.getAmqpExchangeType();
            amqpSpace.setTotalCycleNum(NumberUtils.toLong(this.parsedOp.getStaticConfig("cycles", String.class)));
            amqpSpace.setTotalThreadNum(NumberUtils.toInt(this.parsedOp.getStaticConfig("threads", String.class)));

        }
        configured=true;
    }

}
