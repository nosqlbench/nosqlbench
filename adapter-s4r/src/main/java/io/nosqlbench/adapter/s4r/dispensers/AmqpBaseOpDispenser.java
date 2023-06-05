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

package io.nosqlbench.adapter.s4r.dispensers;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.nosqlbench.adapter.s4r.S4RSpace;
import io.nosqlbench.adapter.s4r.exception.S4RAdapterUnexpectedException;
import io.nosqlbench.adapter.s4r.ops.S4RTimeTrackOp;
import io.nosqlbench.adapter.s4r.util.S4RAdapterMetrics;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public abstract  class AmqpBaseOpDispenser extends BaseOpDispenser<S4RTimeTrackOp, S4RSpace> {

    private static final Logger logger = LogManager.getLogger("AmqpBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final S4RAdapterMetrics s4rAdapterMetrics;
    protected final S4RSpace s4rSpace;

    protected final Map<String, String> s4rConfMap = new HashMap<>();
    protected final String exchangeType;
    protected final LongFunction<String> exchangeNameFunc;

    protected AmqpBaseOpDispenser(final DriverAdapter adapter,
                                  final ParsedOp op,
                                  final S4RSpace s4RSpace) {

        super(adapter, op);

        parsedOp = op;
        this.s4rSpace = s4RSpace;

        s4rAdapterMetrics = new S4RAdapterMetrics(this, this);
        s4rAdapterMetrics.initS4JAdapterInstrumentation();

        s4rConfMap.putAll(s4RSpace.getS4rClientConf().getS4rConfMap());

        this.exchangeType = s4RSpace.getAmqpExchangeType();
        this.exchangeNameFunc = lookupMandtoryStrOpValueFunc("exchange_name");

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

    protected Channel getChannelWithExchange(Connection amqpConnection,
                                             long connSeqNum,
                                             long channelSeqNum,
                                             String exchangeName)
    throws IOException {
        Channel channel = amqpConnection.createChannel();
        if (channel == null) {
            throw new S4RAdapterUnexpectedException("No AMQP channel is available!");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("AMQP channel created -- {} [{},{}] ",
                channel,
                connSeqNum,
                channelSeqNum);
        }

        AMQP.Exchange.DeclareOk declareOk =
            channel.exchangeDeclare(exchangeName, s4rSpace.getAmqpExchangeType());
        if (logger.isDebugEnabled()) {
            logger.debug("AMQP exchange declared -- [name: {}, type: {}] {}",
                exchangeName,
                exchangeType,
                declareOk);
        }

        return channel;
    }

    protected long getConnSeqNum(long cycle) {
        return cycle % s4rSpace.getAmqpConnNum();
    }

    protected long getConnChannelSeqNum(long cycle) {
        return (cycle / s4rSpace.getAmqpConnNum()) % s4rSpace.getAmqpConnChannelNum();
    }

    protected String getEffectiveExchangeName(long cycle) {
        String exchangeNameInput = exchangeNameFunc.apply(cycle);
        return (StringUtils.isBlank(exchangeNameInput) ? "exchange-" + getConnChannelSeqNum(cycle) : exchangeNameInput);
    }

    public String getName() {
        return "AmqpBaseOpDispenser";
    }
}
