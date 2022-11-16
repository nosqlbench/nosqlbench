package io.nosqlbench.adapter.pulsar.dispensers;

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

import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterInvalidParamException;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.ops.PulsarOp;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiter;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateLimiters;
import io.nosqlbench.engine.api.activityapi.ratelimits.RateSpec;
import io.nosqlbench.engine.api.activityimpl.BaseOpDispenser;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public abstract  class PulsarBaseOpDispenser extends BaseOpDispenser<PulsarOp, PulsarSpace> implements NBNamedElement {

    private final static Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final PulsarSpace pulsarSpace;
    protected final PulsarAdapterMetrics pulsarAdapterMetrics;
    private final ConcurrentHashMap<String, Producer<?>> producers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Consumer<?>> consumers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Reader<?>> readers = new ConcurrentHashMap<>();

    protected final LongFunction<Boolean> asyncApiFunc;
    protected final LongFunction<String> tgtNameFunc;

    protected final int totalThreadNum;

    protected final long totalCycleNum;

    protected RateLimiter per_thread_cyclelimiter;

    public PulsarBaseOpDispenser(DriverAdapter adapter,
                                 ParsedOp op,
                                 LongFunction<String> tgtNameFunc,
                                 PulsarSpace pulsarSpace) {

        super(adapter, op);

        this.parsedOp = op;
        this.tgtNameFunc = tgtNameFunc;
        this.pulsarSpace = pulsarSpace;

        // Doc-level parameter: async_api
        this.asyncApiFunc = lookupStaticBoolConfigValueFunc(
            PulsarAdapterUtil.DOC_LEVEL_PARAMS.ASYNC_API.label, true);

        String defaultMetricsPrefix = getDefaultMetricsPrefix(this.parsedOp);
        this.pulsarAdapterMetrics = new PulsarAdapterMetrics(this, defaultMetricsPrefix);
        if (instrument) {
            pulsarAdapterMetrics.initPulsarAdapterInstrumentation();
        }

        totalThreadNum = NumberUtils.toInt(parsedOp.getStaticValue("threads"));
        totalCycleNum = NumberUtils.toLong(parsedOp.getStaticValue("cycles"));

        this.parsedOp.getOptionalStaticConfig("per_thread_cyclerate", String.class)
            .map(RateSpec::new)
            .ifPresent(spec -> per_thread_cyclelimiter =
                RateLimiters.createOrUpdate(this, "cycles", per_thread_cyclelimiter, spec));
    }

    @Override
    public String getName() {
        return "PulsarBaseOpDispenser";
    }

    public PulsarSpace getPulsarSpace() { return pulsarSpace; }

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

    /**
     * Get a proper Pulsar API metrics prefix depending on the API type
     *
     * @param apiType - Pulsar API type: producer, consumer, reader, etc.
     * @param apiObjName - actual name of a producer, a consumer, a reader, etc.
     * @param topicName - topic name
     * @return String
     */
    private String getPulsarAPIMetricsPrefix(String apiType, String apiObjName, String topicName) {
        String apiMetricsPrefix = "";

        if (PulsarAdapterUtil.isValidPulsarApiType(apiType)) {
            if (!StringUtils.isBlank(apiObjName)) {
                apiMetricsPrefix = apiObjName + "_";
            } else {
                // we want a meaningful name for the API object (producer, consumer, reader, etc.)
                // we are not appending the topic name
                apiMetricsPrefix = apiType;

                if (apiType.equalsIgnoreCase(PulsarAdapterUtil.PULSAR_API_TYPE.PRODUCER.label))
                    apiMetricsPrefix += producers.size();
                else if (apiType.equalsIgnoreCase(PulsarAdapterUtil.PULSAR_API_TYPE.CONSUMER.label))
                    apiMetricsPrefix += consumers.size();
                else if (apiType.equalsIgnoreCase(PulsarAdapterUtil.PULSAR_API_TYPE.READER.label))
                    apiMetricsPrefix += readers.size();

                apiMetricsPrefix += "_";
            }

            apiMetricsPrefix += topicName + "_";
            apiMetricsPrefix = apiMetricsPrefix
                // default name for tests/demos (in all Pulsar examples) is persistent://public/default/test -> use just the topic name test
                .replace("persistent://public/default/", "")
                // always remove topic type
                .replace("non-persistent://", "")
                .replace("persistent://", "")
                // persistent://tenant/namespace/topicname -> tenant_namespace_topicname
                .replace("/", "_");

            apiMetricsPrefix += "--";
        }

        return apiMetricsPrefix;
    }

    //////////////////////////////////////
    // Producer Processing --> start
    //////////////////////////////////////
    //

    // Topic name IS mandatory for a producer
    // - It must be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveProducerTopicName(String cycleTopicName) {
        if (!StringUtils.isBlank(cycleTopicName)) {
            return cycleTopicName;
        }

        String globalTopicName = pulsarSpace.getPulsarNBClientConf().getProducerTopicName();
        if (!StringUtils.isBlank(globalTopicName)) {
            return globalTopicName;
        }

        throw new PulsarAdapterInvalidParamException(
            "Effective topic name for a producer can't NOT be  empty, " +
                "it must be set either as a corresponding adapter Op parameter value or " +
                "set in the global Pulsar conf file.");
    }

    // Producer name is NOT mandatory
    // - It can be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveProducerName(String cycleProducerName) {
        if (!StringUtils.isBlank(cycleProducerName)) {
            return cycleProducerName;
        }

        String globalProducerName = pulsarSpace.getPulsarNBClientConf().getProducerName();
        if (!StringUtils.isBlank(globalProducerName)) {
            return globalProducerName;
        }

        return "";
    }

    public Producer<?> getProducer(String cycleTopicName, String cycleProducerName) {
        String topicName = getEffectiveProducerTopicName(cycleTopicName);
        String producerName = getEffectiveProducerName(cycleProducerName);

        String producerCacheKey = PulsarAdapterUtil.buildCacheKey(producerName, topicName);
        Producer<?> producer = producers.get(producerCacheKey);

        if (producer == null) {
            PulsarClient pulsarClient = pulsarSpace.getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> producerConf = pulsarSpace.getPulsarNBClientConf().getProducerConfMap();

            // Remove global level settings: "topicName" and "producerName"
            producerConf.remove(PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.topicName.label);
            producerConf.remove(PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.producerName.label);

            try {
                ProducerBuilder<?> producerBuilder = pulsarClient.
                    newProducer(pulsarSpace.getPulsarSchema()).
                    loadConf(producerConf).
                    topic(topicName);

                if (!StringUtils.isAnyBlank(producerName)) {
                    producerBuilder = producerBuilder.producerName(producerName);
                }

                producer = producerBuilder.create();
                producers.put(producerCacheKey, producer);

                if (instrument) {
                    pulsarAdapterMetrics.registerProducerApiMetrics(producer,
                        getPulsarAPIMetricsPrefix(
                            PulsarAdapterUtil.PULSAR_API_TYPE.PRODUCER.label,
                            producerName,
                            topicName));
                }
            }
            catch (PulsarClientException ple) {
                throw new PulsarAdapterUnexpectedException("Failed to create a Pulsar producer.");
            }
        }

        return producer;
    }

    //
    //////////////////////////////////////
    // Producer Processing <-- end
    //////////////////////////////////////



    //////////////////////////////////////
    // Consumer Processing --> start
    //////////////////////////////////////
    //

    private String getEffectiveConsumerTopicNameListStr(String cycleTopicNameListStr) {
        if (!StringUtils.isBlank(cycleTopicNameListStr)) {
            return cycleTopicNameListStr;
        }

        String globalTopicNames = pulsarSpace.getPulsarNBClientConf().getConsumerTopicNames();
        if (!StringUtils.isBlank(globalTopicNames)) {
            return globalTopicNames;
        }

        return "";
    }

    private List<String> getEffectiveConsumerTopicNameList(String cycleTopicNameListStr) {
        String effectiveTopicNamesStr = getEffectiveConsumerTopicNameListStr(cycleTopicNameListStr);

        String[] names = effectiveTopicNamesStr.split("[;,]");
        ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (String name : names) {
            if (!StringUtils.isBlank(name))
                effectiveTopicNameList.add(name.trim());
        }

        return effectiveTopicNameList;
    }

    private String getEffectiveConsumerTopicPatternStr(String cycleTopicPatternStr) {
        if (!StringUtils.isBlank(cycleTopicPatternStr)) {
            return cycleTopicPatternStr;
        }

        String globalTopicsPattern = pulsarSpace.getPulsarNBClientConf().getConsumerTopicPattern();
        if (!StringUtils.isBlank(globalTopicsPattern)) {
            return globalTopicsPattern;
        }

        return "";
    }

    private Pattern getEffectiveConsumerTopicPattern(String cycleTopicPatternStr) {
        String effectiveTopicPatternStr = getEffectiveConsumerTopicPatternStr(cycleTopicPatternStr);
        Pattern topicsPattern;
        try {
            if (!StringUtils.isBlank(effectiveTopicPatternStr))
                topicsPattern = Pattern.compile(effectiveTopicPatternStr);
            else
                topicsPattern = null;
        } catch (PatternSyntaxException pse) {
            topicsPattern = null;
        }
        return topicsPattern;
    }


    // Subscription name is NOT mandatory
    // - It can be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveSubscriptionName(String cycleSubscriptionName) {
        if (!StringUtils.isBlank(cycleSubscriptionName)) {
            return cycleSubscriptionName;
        }

        String globalSubscriptionName = pulsarSpace.getPulsarNBClientConf().getConsumerSubscriptionName();
        if (!StringUtils.isBlank(globalSubscriptionName)) {
            return globalSubscriptionName;
        }

        throw new PulsarAdapterInvalidParamException(
            "Effective subscription name for a consumer can't NOT be  empty, " +
                "it must be set either as a corresponding adapter Op parameter value or " +
                "set in the global Pulsar conf file.");
    }

    private String getEffectiveSubscriptionTypeStr(String cycleSubscriptionType) {
        String subscriptionTypeStr = "";

        if (!StringUtils.isBlank(cycleSubscriptionType)) {
            subscriptionTypeStr = cycleSubscriptionType;
        }
        else {
            String globalSubscriptionType = pulsarSpace.getPulsarNBClientConf().getConsumerSubscriptionType();
            if (!StringUtils.isBlank(globalSubscriptionType)) {
                subscriptionTypeStr = globalSubscriptionType;
            }
        }

        if (StringUtils.isNotBlank(subscriptionTypeStr) &&
            !PulsarAdapterUtil.isValidSubscriptionType(subscriptionTypeStr)) {
            throw new PulsarAdapterInvalidParamException(
                "Invalid effective subscription type for a consumer (\"" + subscriptionTypeStr + "\"). " +
                    "It must be one of the following values: " +  PulsarAdapterUtil.getValidSubscriptionTypeList());
        }

        return subscriptionTypeStr;
    }
    private SubscriptionType getEffectiveSubscriptionType(String cycleSubscriptionType) {
        String effectiveSubscriptionStr = getEffectiveSubscriptionTypeStr(cycleSubscriptionType);
        SubscriptionType subscriptionType = SubscriptionType.Exclusive; // default subscription type

        if (!StringUtils.isBlank(effectiveSubscriptionStr)) {
            subscriptionType = SubscriptionType.valueOf(effectiveSubscriptionStr);
        }

        return subscriptionType;
    }

    private String getEffectiveConsumerName(String cycleConsumerName) {
        if (!StringUtils.isBlank(cycleConsumerName)) {
            return cycleConsumerName;
        }

        String globalConsumerName = pulsarSpace.getPulsarNBClientConf().getConsumerName();
        if (!StringUtils.isBlank(globalConsumerName)) {
            return globalConsumerName;
        }

        return "";
    }

    public Consumer<?> getConsumer(String cycleTopicNameListStr,
                                   String cycleTopicPatternStr,
                                   String cycleSubscriptionName,
                                   String cycleSubscriptionType,
                                   String cycleConsumerName,
                                   String cycleKeySharedSubscriptionRanges) {

        List<String> topicNameList = getEffectiveConsumerTopicNameList(cycleTopicNameListStr);
        String topicPatternStr = getEffectiveConsumerTopicPatternStr(cycleTopicPatternStr);
        Pattern topicPattern = getEffectiveConsumerTopicPattern(cycleTopicPatternStr);
        String subscriptionName = getEffectiveSubscriptionName(cycleSubscriptionName);
        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);
        String consumerName = getEffectiveConsumerName(cycleConsumerName);

        if ( subscriptionType.equals(SubscriptionType.Exclusive) && (totalThreadNum > 1) ) {
            throw new PulsarAdapterInvalidParamException(
                MessageConsumerOpDispenser.SUBSCRIPTION_TYPE_OP_PARAM,
                "creating multiple consumers of \"Exclusive\" subscription type under the same subscription name");
        }

        if ( (topicNameList.isEmpty() && (topicPattern == null)) ||
             (!topicNameList.isEmpty() && (topicPattern != null)) ) {
            throw new PulsarAdapterInvalidParamException(
                "Invalid combination of topic name(s) and topic patterns; only specify one parameter!");
        }

        boolean multiTopicConsumer = (topicNameList.size() > 1 || (topicPattern != null));

        String consumerTopicListString;
        if (!topicNameList.isEmpty()) {
            consumerTopicListString = String.join("|", topicNameList);
        } else {
            consumerTopicListString = topicPatternStr;
        }

        String consumerCacheKey = PulsarAdapterUtil.buildCacheKey(
            consumerName,
            subscriptionName,
            consumerTopicListString);
        Consumer<?> consumer = consumers.get(consumerCacheKey);

        if (consumer == null) {
            PulsarClient pulsarClient = pulsarSpace.getPulsarClient();

            // Get other possible consumer settings that are set at global level
            Map<String, Object> consumerConf = new HashMap<>(pulsarSpace.getPulsarNBClientConf().getConsumerConfMap());

            // Remove global level settings:
            // - "topicNames", "topicsPattern", "subscriptionName", "subscriptionType", "consumerName"
            consumerConf.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
            consumerConf.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
            consumerConf.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label);
            consumerConf.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label);
            consumerConf.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.consumerName.label);
            // Remove non-standard consumer configuration properties
            consumerConf.remove(PulsarAdapterUtil.CONSUMER_CONF_CUSTOM_KEY.timeout.label);

            try {
                ConsumerBuilder<?> consumerBuilder;

                if (!multiTopicConsumer) {
                    assert (topicNameList.size() == 1);
                    consumerBuilder = pulsarClient.newConsumer(pulsarSpace.getPulsarSchema());
                    consumerBuilder.topic(topicNameList.get(0));
                }
                else {
                    consumerBuilder = pulsarClient.newConsumer();
                    if (!topicNameList.isEmpty()) {
                        assert (topicNameList.size() > 1);
                        consumerBuilder.topics(topicNameList);
                    }
                    else {
                        consumerBuilder.topicsPattern(topicPattern);
                    }
                }

                consumerBuilder.
                    loadConf(consumerConf).
                    subscriptionName(subscriptionName).
                    subscriptionType(subscriptionType);

                if (!StringUtils.isBlank(consumerName))
                    consumerBuilder.consumerName(consumerName);

                if (subscriptionType == SubscriptionType.Key_Shared) {
                    KeySharedPolicy keySharedPolicy = KeySharedPolicy.autoSplitHashRange();
                    if (cycleKeySharedSubscriptionRanges != null && !cycleKeySharedSubscriptionRanges.isEmpty()) {
                        Range[] ranges = parseRanges(cycleKeySharedSubscriptionRanges);
                        logger.info("Configuring KeySharedPolicy#stickyHashRange with ranges {}", ranges);
                        keySharedPolicy = KeySharedPolicy.stickyHashRange().ranges(ranges);
                    }
                    consumerBuilder.keySharedPolicy(keySharedPolicy);
                }

                consumer = consumerBuilder.subscribe();
                consumers.put(consumerCacheKey, consumer);

                if (instrument) {
                    pulsarAdapterMetrics.registerConsumerApiMetrics(
                        consumer,
                        getPulsarAPIMetricsPrefix(
                            PulsarAdapterUtil.PULSAR_API_TYPE.CONSUMER.label,
                            consumerName,
                            consumerTopicListString));
                }

            }
            catch (PulsarClientException ple) {
                throw new PulsarAdapterUnexpectedException("Failed to create a Pulsar consumer!");
            }
        }

        return consumer;
    }

    private static Range[] parseRanges(String ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return new Range[0];
        }
        String[] split = ranges.split(",");
        Range[] result = new Range[split.length];
        for (int i = 0; i < split.length; i++) {
            String range = split[i];
            int pos = range.indexOf("..");
            if (pos <= 0) {
                throw new IllegalArgumentException("Invalid range '" + range + "'");
            }
            try {
                int start = Integer.parseInt(range.substring(0, pos));
                int end = Integer.parseInt(range.substring(pos + 2));
                result[i] = Range.of(start, end);
            } catch (NumberFormatException err) {
                throw new IllegalArgumentException("Invalid range '" + range + "'");
            }
        }
        return result;
    }

    //
    //////////////////////////////////////
    // Consumer Processing <-- end
    //////////////////////////////////////



    //////////////////////////////////////
    // Reader Processing --> Start
    //////////////////////////////////////
    //

    // Topic name IS mandatory for a reader
    // - It must be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveReaderTopicName(String cycleReaderTopicName) {
        if (!StringUtils.isBlank(cycleReaderTopicName)) {
            return cycleReaderTopicName;
        }

        String globalReaderTopicName = pulsarSpace.getPulsarNBClientConf().getReaderTopicName();
        if (!StringUtils.isBlank(globalReaderTopicName)) {
            return globalReaderTopicName;
        }

        throw new PulsarAdapterInvalidParamException(
            "Effective topic name for a reader can't NOT be  empty, " +
                "it must be set either as a corresponding adapter Op parameter value or " +
                "set in the global Pulsar conf file.");
    }

    // Reader name is NOT mandatory
    // - It can be set at either global level or cycle level
    // - If set at both levels, cycle level setting takes precedence
    private String getEffectiveReaderName(String cycleReaderName) {
        if (!StringUtils.isBlank(cycleReaderName)) {
            return cycleReaderName;
        }

        String globalReaderName = pulsarSpace.getPulsarNBClientConf().getReaderName();
        if (!StringUtils.isBlank(globalReaderName)) {
            return globalReaderName;
        }

        return "";
    }

    private String getEffectiveStartMsgPosStr(String cycleStartMsgPosStr) {
        if (!StringUtils.isBlank(cycleStartMsgPosStr)) {
            return cycleStartMsgPosStr;
        }

        String globalStartMsgPosStr = pulsarSpace.getPulsarNBClientConf().getStartMsgPosStr();
        if (!StringUtils.isBlank(globalStartMsgPosStr)) {
            return globalStartMsgPosStr;
        }

        return PulsarAdapterUtil.READER_MSG_POSITION_TYPE.latest.label;
    }

    public Reader<?> getReader(String cycleTopicName,
                               String cycleReaderName,
                               String cycleStartMsgPos) {

        String topicName = getEffectiveReaderTopicName(cycleTopicName);
        String readerName = getEffectiveReaderName(cycleReaderName);
        String startMsgPosStr = getEffectiveStartMsgPosStr(cycleStartMsgPos);
        if (!PulsarAdapterUtil.isValideReaderStartPosition(startMsgPosStr)) {
            throw new RuntimeException("Reader:: Invalid value for reader start message position!");
        }

        String readerCacheKey = PulsarAdapterUtil.buildCacheKey(topicName, readerName, startMsgPosStr);
        Reader<?> reader = readers.get(readerCacheKey);

        if (reader == null) {
            PulsarClient pulsarClient = pulsarSpace.getPulsarClient();;

            Map<String, Object> readerConf = pulsarSpace.getPulsarNBClientConf().getReaderConfMap();

            // Remove global level settings: "topicName" and "readerName"
            readerConf.remove(PulsarAdapterUtil.READER_CONF_STD_KEY.topicName.label);
            readerConf.remove(PulsarAdapterUtil.READER_CONF_STD_KEY.readerName.label);
            // Remove non-standard reader configuration properties
            readerConf.remove(PulsarAdapterUtil.READER_CONF_CUSTOM_KEY.startMessagePos.label);

            try {
                ReaderBuilder<?> readerBuilder = pulsarClient.
                    newReader(pulsarSpace.getPulsarSchema()).
                    loadConf(readerConf).
                    topic(topicName).
                    readerName(readerName);

                MessageId startMsgId = MessageId.latest;
                if (startMsgPosStr.equalsIgnoreCase(PulsarAdapterUtil.READER_MSG_POSITION_TYPE.earliest.label)) {
                    startMsgId = MessageId.earliest;
                }
                //TODO: custom start message position is NOT supported yet
                //else if (startMsgPosStr.startsWith(PulsarAdapterUtil.READER_MSG_POSITION_TYPE.custom.label)) {
                //    startMsgId = MessageId.latest;
                //}

                reader = readerBuilder.startMessageId(startMsgId).create();

            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar reader!");
            }

            readers.put(readerCacheKey, reader);
        }

        return reader;
    }
    //
    //////////////////////////////////////
    // Reader Processing <-- end
    //////////////////////////////////////
}
