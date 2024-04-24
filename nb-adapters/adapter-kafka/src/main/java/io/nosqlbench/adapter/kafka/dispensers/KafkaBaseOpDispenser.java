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

package io.nosqlbench.adapter.kafka.dispensers;

import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterInvalidParamException;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterMetrics;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapters.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public abstract  class KafkaBaseOpDispenser extends BaseOpDispenser<KafkaOp, KafkaSpace> {

    private static final Logger logger = LogManager.getLogger("KafkaBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final KafkaAdapterMetrics kafkaAdapterMetrics;
    protected final KafkaSpace kafkaSpace;

    protected final int kafkaClntCnt;
    protected final int consumerGrpCnt;

    // Doc-level parameter: async_api (default: true)
    // - For Producer workload, this means waiting for message send ack. synchronously or asynchronously
    // - For Consumer workload, this means doing manual message commit synchronously or asynchronously
    //   Only relevant when auto.commit is disabled
    protected final boolean asyncAPI;

    protected final LongFunction<String> topicNameStrFunc;
    protected final Map<String, String> topicConfMap = new HashMap<>();

    protected final int totalThreadNum;
    protected final long totalCycleNum;

    protected KafkaBaseOpDispenser(final DriverAdapter adapter,
                                   final ParsedOp op,
                                   final LongFunction<String> topicNameStrFunc,
                                   final KafkaSpace kafkaSpace) {

        super(adapter, op);

        parsedOp = op;
        this.kafkaSpace = kafkaSpace;

        kafkaAdapterMetrics = new KafkaAdapterMetrics(this, this);
        this.kafkaAdapterMetrics.initS4JAdapterInstrumentation();

        asyncAPI =
            this.parsedOp.getStaticConfigOr(DOC_LEVEL_PARAMS.ASYNC_API.label, Boolean.TRUE);

        this.topicNameStrFunc = topicNameStrFunc;
        topicConfMap.putAll(kafkaSpace.getKafkaClientConf().getTopicConfMap());

        totalCycleNum = NumberUtils.toLong(this.parsedOp.getStaticConfig("cycles", String.class));
        kafkaSpace.setTotalCycleNum(this.totalCycleNum);

        kafkaClntCnt = kafkaSpace.getKafkaClntNum();
        consumerGrpCnt = kafkaSpace.getConsumerGrpNum();
        totalThreadNum = NumberUtils.toInt(this.parsedOp.getStaticConfig("threads", String.class));

        assert 0 < kafkaClntCnt;
        assert 0 < consumerGrpCnt;

        final boolean validThreadNum =
            this instanceof MessageProducerOpDispenser && this.totalThreadNum == this.kafkaClntCnt ||
                this instanceof MessageConsumerOpDispenser && this.totalThreadNum == this.kafkaClntCnt * this.consumerGrpCnt;
        if (!validThreadNum) throw new KafkaAdapterInvalidParamException(
            "Incorrect settings of 'threads', 'num_clnt', or 'num_cons_grp' -- " +
                this.totalThreadNum + ", " + this.kafkaClntCnt + ", " + this.consumerGrpCnt);
    }

    public KafkaSpace getKafkaSpace() { return this.kafkaSpace; }
    public KafkaAdapterMetrics getKafkaAdapterMetrics() { return this.kafkaAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(final String paramName, final boolean defaultValue) {
        final LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = l -> this.parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        KafkaBaseOpDispenser.logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(final String paramName, final String defaultValue) {
        final LongFunction<String> stringLongFunction;
        stringLongFunction = this.parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse(l -> defaultValue);
        KafkaBaseOpDispenser.logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(final String paramName) {
        return this.lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(final String paramName) {
        final LongFunction<String> stringLongFunction;
        stringLongFunction = this.parsedOp.getAsRequiredFunction(paramName, String.class);
        KafkaBaseOpDispenser.logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }

    public String getName() {
        return "KafkaBaseOpDispenser";
    }
}
