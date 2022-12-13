package io.nosqlbench.adapter.kafka.dispensers;

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


import io.nosqlbench.adapter.kafka.KafkaSpace;
import io.nosqlbench.adapter.kafka.exception.KafkaAdapterInvalidParamException;
import io.nosqlbench.adapter.kafka.ops.KafkaOp;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterMetrics;
import io.nosqlbench.adapter.kafka.util.KafkaAdapterUtil;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract  class KafkaBaseOpDispenser extends BaseOpDispenser<KafkaOp, KafkaSpace> {

    private final static Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

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

    public KafkaBaseOpDispenser(DriverAdapter adapter,
                                ParsedOp op,
                                LongFunction<String> topicNameStrFunc,
                                KafkaSpace kafkaSpace) {

        super(adapter, op);

        this.parsedOp = op;
        this.kafkaSpace = kafkaSpace;

        String defaultMetricsPrefix = getDefaultMetricsPrefix(this.parsedOp);
        this.kafkaAdapterMetrics = new KafkaAdapterMetrics(defaultMetricsPrefix);
        kafkaAdapterMetrics.initS4JAdapterInstrumentation();

        this.asyncAPI =
            parsedOp.getStaticConfigOr(KafkaAdapterUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, Boolean.TRUE);

        this.topicNameStrFunc = topicNameStrFunc;
        this.topicConfMap.putAll(kafkaSpace.getKafkaClientConf().getTopicConfMap());

        this.totalCycleNum = NumberUtils.toLong(parsedOp.getStaticConfig("cycles", String.class));
        kafkaSpace.setTotalCycleNum(totalCycleNum);

        this.kafkaClntCnt = kafkaSpace.getClntNum();
        this.consumerGrpCnt = kafkaSpace.getConsumerGrpNum();
        this.totalThreadNum = NumberUtils.toInt(parsedOp.getStaticConfig("threads", String.class));

        assert (kafkaClntCnt > 0);
        assert (consumerGrpCnt > 0);

        boolean validThreadNum =
            ( ((this instanceof MessageProducerOpDispenser) && (totalThreadNum == kafkaClntCnt)) ||
              ((this instanceof MessageConsumerOpDispenser) && (totalThreadNum == kafkaClntCnt*consumerGrpCnt)) );
        if (!validThreadNum) {
            throw new KafkaAdapterInvalidParamException(
                "Incorrect settings of 'threads', 'num_clnt', or 'num_cons_grp' -- "  +
                totalThreadNum + ", " + kafkaClntCnt + ", " + consumerGrpCnt);
        }
    }

    public KafkaSpace getKafkaSpace() { return kafkaSpace; }
    public KafkaAdapterMetrics getKafkaAdapterMetrics() { return kafkaAdapterMetrics; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(String paramName, boolean defaultValue) {
        LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = (l) -> parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    protected LongFunction<Set<String>> lookupStaticStrSetOpValueFunc(String paramName) {
        LongFunction<Set<String>> setStringLongFunction;
        setStringLongFunction = (l) -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<String > set = new HashSet<>();

                if (StringUtils.contains(value,',')) {
                    set = Arrays.stream(value.split(","))
                        .map(String::trim)
                        .filter(Predicate.not(String::isEmpty))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                }

                return set;
            }).orElse(Collections.emptySet());
        logger.info("{}: {}", paramName, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<Integer> lookupStaticIntOpValueFunc(String paramName, int defaultValue) {
        LongFunction<Integer> integerLongFunction;
        integerLongFunction = (l) -> parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> NumberUtils.toInt(value))
            .map(value -> {
                if (value < 0) return 0;
                else return value;
            }).orElse(defaultValue);
        logger.info("{}: {}", paramName, integerLongFunction.apply(0));
        return integerLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName, String defaultValue) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse((l) -> defaultValue);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(String paramName) {
        return lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(String paramName) {
        LongFunction<String> stringLongFunction;
        stringLongFunction = parsedOp.getAsRequiredFunction(paramName, String.class);
        logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
}
