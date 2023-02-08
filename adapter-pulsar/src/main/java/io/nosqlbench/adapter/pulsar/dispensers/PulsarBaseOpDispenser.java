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
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract  class PulsarBaseOpDispenser extends BaseOpDispenser<PulsarOp, PulsarSpace> implements NBNamedElement {

    private final static Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final PulsarSpace pulsarSpace;
    protected final PulsarAdapterMetrics pulsarAdapterMetrics;
    protected final LongFunction<Boolean> asyncApiFunc;
    protected final LongFunction<String> tgtNameFunc;

    protected final int totalThreadNum;

    protected final long totalCycleNum;

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
        pulsarAdapterMetrics.initPulsarAdapterInstrumentation();

        totalThreadNum = NumberUtils.toInt(parsedOp.getStaticValue("threads"));
        totalCycleNum = NumberUtils.toLong(parsedOp.getStaticValue("cycles"));
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
                    apiMetricsPrefix += pulsarSpace.getProducerSetCnt();
                else if (apiType.equalsIgnoreCase(PulsarAdapterUtil.PULSAR_API_TYPE.CONSUMER.label))
                    apiMetricsPrefix += pulsarSpace.getConsumerSetCnt();
                else if (apiType.equalsIgnoreCase(PulsarAdapterUtil.PULSAR_API_TYPE.READER.label))
                    apiMetricsPrefix += pulsarSpace.getReaderSetCnt();

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


    // A configuration parameter can be set either at the global level (config.properties file),
    //   or at the cycle level (<nb_scenario>.yaml file).
    // If set at both levels, cycle level setting takes precedence
    private String getEffectiveConValue(String confCategory, String confParamName, String cycleConfValue) {
        if (!StringUtils.isBlank(cycleConfValue)) {
            return cycleConfValue;
        }

        if (PulsarAdapterUtil.isValidConfCategory(confCategory)) {
            Map<String, String> catConfMap = new HashMap<>();

            if (StringUtils.equalsIgnoreCase(confCategory, PulsarAdapterUtil.CONF_GATEGORY.Schema.label))
                catConfMap = pulsarSpace.getPulsarNBClientConf().getSchemaConfMapRaw();
            else if (StringUtils.equalsIgnoreCase(confCategory, PulsarAdapterUtil.CONF_GATEGORY.Client.label))
                catConfMap = pulsarSpace.getPulsarNBClientConf().getClientConfMapRaw();
            else if (StringUtils.equalsIgnoreCase(confCategory, PulsarAdapterUtil.CONF_GATEGORY.Producer.label))
                catConfMap = pulsarSpace.getPulsarNBClientConf().getProducerConfMapRaw();
            else if (StringUtils.equalsIgnoreCase(confCategory, PulsarAdapterUtil.CONF_GATEGORY.Consumer.label))
                catConfMap = pulsarSpace.getPulsarNBClientConf().getConsumerConfMapRaw();
            else if (StringUtils.equalsIgnoreCase(confCategory, PulsarAdapterUtil.CONF_GATEGORY.Reader.label))
                catConfMap = pulsarSpace.getPulsarNBClientConf().getReaderConfMapRaw();

            String globalConfValue = catConfMap.get(confParamName);
            if (!StringUtils.isBlank(globalConfValue)) {
                return globalConfValue;
            }
        }

        return "";
    }


    public Producer<?> getProducer(String cycleTopicName, String cycleProducerName) {
        String topicName = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Producer.label,
            PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.topicName.label,
            cycleTopicName);

        String producerName = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Producer.label,
            PulsarAdapterUtil.PRODUCER_CONF_STD_KEY.producerName.label,
            cycleProducerName);

        PulsarSpace.ProducerCacheKey producerCacheKey = new PulsarSpace.ProducerCacheKey(producerName, topicName);
        return pulsarSpace.getProducer(producerCacheKey, () -> {
            PulsarClient pulsarClient = pulsarSpace.getPulsarClient();

            // Get other possible producer settings that are set at global level
            Map<String, Object> producerConf = pulsarSpace.getPulsarNBClientConf().getProducerConfMapTgt();

            // Remove global level settings
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

                Producer<?> producer = producerBuilder.create();
                pulsarAdapterMetrics.registerProducerApiMetrics(producer,
                    getPulsarAPIMetricsPrefix(
                        PulsarAdapterUtil.PULSAR_API_TYPE.PRODUCER.label,
                        producerName,
                        topicName));
                return producer;
            } catch (PulsarClientException ple) {
                throw new PulsarAdapterUnexpectedException("Failed to create a Pulsar producer.");
            }
        });
    }

    private List<String> getEffectiveConsumerTopicNameList(String cycleTopicNameListStr) {
        String effectiveTopicNamesStr = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Consumer.label,
            PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicNames.label,
            cycleTopicNameListStr);

        String[] names = effectiveTopicNamesStr.split("[;,]");
        ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (String name : names) {
            if (!StringUtils.isBlank(name))
                effectiveTopicNameList.add(name.trim());
        }

        return effectiveTopicNameList;
    }

    private SubscriptionType getEffectiveSubscriptionType(String cycleSubscriptionType) {
        String subscriptionTypeStr = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Consumer.label,
            PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label,
            cycleSubscriptionType);

        SubscriptionType subscriptionType = SubscriptionType.Exclusive; // default subscription type
        if (!StringUtils.isBlank(subscriptionTypeStr)) {
            try {
                subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr);
            }
            catch (Exception e) {
                throw new PulsarAdapterInvalidParamException(
                    "Invalid effective subscription type for a consumer (\"" + subscriptionTypeStr + "\"). " +
                        "It must be one of the following values: " +  PulsarAdapterUtil.getValidSubscriptionTypeList());
            }
        }

        return subscriptionType;
    }

    public Consumer<?> getConsumer(String cycleTopicNameListStr,
                                   String cycleTopicPatternStr,
                                   String cycleSubscriptionName,
                                   String cycleSubscriptionType,
                                   String cycleConsumerName,
                                   String cycleKeySharedSubscriptionRanges) {

        List<String> topicNameList = getEffectiveConsumerTopicNameList(cycleTopicNameListStr);

        String topicPatternStr = StringUtils.trimToNull(getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Consumer.label,
            PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label,
            cycleTopicPatternStr));

        String subscriptionName = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Consumer.label,
            PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label,
            cycleSubscriptionName);

        SubscriptionType subscriptionType = getEffectiveSubscriptionType(cycleSubscriptionType);

        String consumerName = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Consumer.label,
            PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.consumerName.label,
            cycleConsumerName);

        if ( subscriptionType.equals(SubscriptionType.Exclusive) && (totalThreadNum > 1) ) {
            throw new PulsarAdapterInvalidParamException(
                PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label,
                "creating multiple consumers of \"Exclusive\" subscription type under the same subscription name");
        }

        if ( (topicNameList.isEmpty() && (topicPatternStr == null)) ||
             (!topicNameList.isEmpty() && (topicPatternStr != null)) ) {
            throw new PulsarAdapterInvalidParamException(
                "Invalid combination of topic name(s) and topic patterns; only specify one parameter!");
        }

        return pulsarSpace.getConsumer(
            new PulsarSpace.ConsumerCacheKey(consumerName, subscriptionName, topicNameList, topicPatternStr), () -> {
            PulsarClient pulsarClient = pulsarSpace.getPulsarClient();

            // Get other possible consumer settings that are set at global level
            Map<String, Object> consumerConf =
                new HashMap<>(pulsarSpace.getPulsarNBClientConf().getConsumerConfMapTgt());
            Map<String, Object> consumerConfToLoad = new HashMap<>();
            consumerConfToLoad.putAll(consumerConf);

            try {
                ConsumerBuilder<?> consumerBuilder;

                // Remove settings that will be handled outside "loadConf()"
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicNames.label);
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.topicsPattern.label);
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionName.label);
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.subscriptionType.label);
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.consumerName.label);

                // TODO: It looks like loadConf() method can't handle the following settings properly.
                //       Do these settings manually for now
                //       - deadLetterPolicy
                //       - negativeAckRedeliveryBackoff
                //       - ackTimeoutRedeliveryBackoff
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.deadLetterPolicy.label);
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.negativeAckRedeliveryBackoff.label);
                consumerConfToLoad.remove(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.ackTimeoutRedeliveryBackoff.label);

                boolean multiTopicConsumer = (topicNameList.size() > 1 || (topicPatternStr != null));
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
                        Pattern topicPattern = Pattern.compile(topicPatternStr);
                        consumerBuilder.topicsPattern(topicPattern);
                    }
                }

                consumerBuilder.loadConf(consumerConfToLoad);

                if (consumerConf.containsKey(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.deadLetterPolicy.label)) {
                    consumerBuilder.deadLetterPolicy((DeadLetterPolicy)
                        consumerConf.get(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.deadLetterPolicy.label));
                }
                if (consumerConf.containsKey(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.negativeAckRedeliveryBackoff.label)) {
                    consumerBuilder.negativeAckRedeliveryBackoff((RedeliveryBackoff)
                        consumerConf.get(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.negativeAckRedeliveryBackoff.label));
                }
                if (consumerConf.containsKey(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.ackTimeoutRedeliveryBackoff.label)) {
                    consumerBuilder.ackTimeoutRedeliveryBackoff((RedeliveryBackoff)
                        consumerConf.get(PulsarAdapterUtil.CONSUMER_CONF_STD_KEY.ackTimeoutRedeliveryBackoff.label));
                }

                consumerBuilder
                    .subscriptionName(subscriptionName)
                    .subscriptionType(subscriptionType);

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

                Consumer<?> consumer = consumerBuilder.subscribe();

                String consumerTopicListString = (!topicNameList.isEmpty()) ? String.join("|", topicNameList) : topicPatternStr;
                pulsarAdapterMetrics.registerConsumerApiMetrics(
                    consumer,
                    getPulsarAPIMetricsPrefix(
                        PulsarAdapterUtil.PULSAR_API_TYPE.CONSUMER.label,
                        consumerName,
                        consumerTopicListString));

                return consumer;
            }
            catch (PulsarClientException ple) {
                throw new PulsarAdapterUnexpectedException("Failed to create a Pulsar consumer!");
            }
        });
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

    public Reader<?> getReader(String cycleTopicName,
                               String cycleReaderName,
                               String cycleStartMsgPos) {

        String topicName = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Reader.label,
            PulsarAdapterUtil.READER_CONF_STD_KEY.topicName.label,
            cycleTopicName);

        String readerName = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Reader.label,
            PulsarAdapterUtil.READER_CONF_STD_KEY.readerName.label,
            cycleReaderName);

        String startMsgPosStr = getEffectiveConValue(
            PulsarAdapterUtil.CONF_GATEGORY.Reader.label,
            PulsarAdapterUtil.READER_CONF_CUSTOM_KEY.startMessagePos.label,
            cycleStartMsgPos);
        if (!PulsarAdapterUtil.isValideReaderStartPosition(startMsgPosStr)) {
            throw new RuntimeException("Reader:: Invalid value for reader start message position!");
        }

        return pulsarSpace.getReader(new PulsarSpace.ReaderCacheKey(readerName, topicName, startMsgPosStr), () -> {
            PulsarClient pulsarClient = pulsarSpace.getPulsarClient();;

            Map<String, Object> readerConf = pulsarSpace.getPulsarNBClientConf().getReaderConfMapTgt();

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

                return readerBuilder.startMessageId(startMsgId).create();
            } catch (PulsarClientException ple) {
                ple.printStackTrace();
                throw new RuntimeException("Unable to create a Pulsar reader!");
            }
        });
    }
    //
    //////////////////////////////////////
    // Reader Processing <-- end
    //////////////////////////////////////
}
