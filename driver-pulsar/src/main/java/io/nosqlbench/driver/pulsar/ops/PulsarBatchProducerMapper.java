package io.nosqlbench.driver.pulsar.ops;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.driver.pulsar.util.PulsarActivityUtil;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;

public class PulsarBatchProducerMapper extends PulsarOpMapper {

    private final static Logger logger = LogManager.getLogger(PulsarBatchProducerMapper.class);

    private final LongFunction<String> keyFunc;
    private final LongFunction<String> propFunc;
    private final LongFunction<String> payloadFunc;

    public PulsarBatchProducerMapper(CommandTemplate cmdTpl,
                                     PulsarSpace clientSpace,
                                     PulsarActivity pulsarActivity,
                                     LongFunction<Boolean> asyncApiFunc,
                                     LongFunction<String> keyFunc,
                                     LongFunction<String> propFunc,
                                     LongFunction<String> payloadFunc) {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc);
        this.keyFunc = keyFunc;
        this.propFunc = propFunc;
        this.payloadFunc = payloadFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        String msgKey = keyFunc.apply(value);
        String msgPayload = payloadFunc.apply(value);

        // Check if msgPropJonStr is valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message properties without throwing a runtime exception
        Map<String, String> msgProperties = new HashMap<>();
        String msgPropJsonStr = propFunc.apply(value);
        try {
            msgProperties = PulsarActivityUtil.convertJsonToMap(msgPropJsonStr);
        }
        catch (Exception e) {
            logger.error(
                "PulsarProducerMapper:: Error parsing message property JSON string {}, ignore message properties!",
                msgPropJsonStr);
        }

        return new PulsarBatchProducerOp(
            clientSpace.getPulsarSchema(),
            msgKey,
            msgProperties,
            msgPayload
        );
    }
}
