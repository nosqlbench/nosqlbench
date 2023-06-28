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

package io.nosqlbench.adapter.pulsar.dispensers;

import io.nosqlbench.adapter.pulsar.PulsarSpace;
import io.nosqlbench.adapter.pulsar.PulsarSpace.ConsumerCacheKey;
import io.nosqlbench.adapter.pulsar.PulsarSpace.ProducerCacheKey;
import io.nosqlbench.adapter.pulsar.PulsarSpace.ReaderCacheKey;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterInvalidParamException;
import io.nosqlbench.adapter.pulsar.exception.PulsarAdapterUnexpectedException;
import io.nosqlbench.adapter.pulsar.ops.PulsarOp;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterMetrics;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.CONF_GATEGORY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.CONSUMER_CONF_STD_KEY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.DOC_LEVEL_PARAMS;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.PRODUCER_CONF_STD_KEY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.PULSAR_API_TYPE;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.READER_CONF_CUSTOM_KEY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.READER_CONF_STD_KEY;
import io.nosqlbench.adapter.pulsar.util.PulsarAdapterUtil.READER_MSG_POSITION_TYPE;
import io.nosqlbench.api.config.NBLabels;
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

public abstract class PulsarBaseOpDispenser extends BaseOpDispenser<PulsarOp, PulsarSpace> {

    private static final Logger logger = LogManager.getLogger("PulsarBaseOpDispenser");

    protected final ParsedOp parsedOp;
    protected final PulsarSpace pulsarSpace;
    protected final PulsarAdapterMetrics pulsarAdapterMetrics;
    protected final LongFunction<Boolean> asyncApiFunc;
    protected final LongFunction<String> tgtNameFunc;

    protected final int totalThreadNum;

    protected final long totalCycleNum;

    protected PulsarBaseOpDispenser(final DriverAdapter adapter,
                                    final ParsedOp op,
                                    final LongFunction<String> tgtNameFunc,
                                    final PulsarSpace pulsarSpace) {

        super(adapter, op);

        parsedOp = op;
        this.tgtNameFunc = tgtNameFunc;
        this.pulsarSpace = pulsarSpace;

        // Doc-level parameter: async_api
        asyncApiFunc = this.lookupStaticBoolConfigValueFunc(
            DOC_LEVEL_PARAMS.ASYNC_API.label, true);

        pulsarAdapterMetrics = new PulsarAdapterMetrics(this);
        this.pulsarAdapterMetrics.initPulsarAdapterInstrumentation();

        this.totalThreadNum = NumberUtils.toInt(this.parsedOp.getStaticValue("threads"));
        this.totalCycleNum = NumberUtils.toLong(this.parsedOp.getStaticValue("cycles"));
    }

    public PulsarSpace getPulsarSpace() { return this.pulsarSpace; }

    protected LongFunction<Boolean> lookupStaticBoolConfigValueFunc(final String paramName, final boolean defaultValue) {
        final LongFunction<Boolean> booleanLongFunction;
        booleanLongFunction = l -> this.parsedOp.getOptionalStaticConfig(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> BooleanUtils.toBoolean(value))
            .orElse(defaultValue);
        PulsarBaseOpDispenser.logger.info("{}: {}", paramName, booleanLongFunction.apply(0));
        return  booleanLongFunction;
    }

    protected LongFunction<Set<String>> lookupStaticStrSetOpValueFunc(final String paramName) {
        final LongFunction<Set<String>> setStringLongFunction;
        setStringLongFunction = l -> this.parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> {
                Set<String > set = new HashSet<>();

                if (StringUtils.contains(value,',')) set = Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(Predicate.not(String::isEmpty))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

                return set;
            }).orElse(Collections.emptySet());
        PulsarBaseOpDispenser.logger.info("{}: {}", paramName, setStringLongFunction.apply(0));
        return setStringLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<Integer> lookupStaticIntOpValueFunc(final String paramName, final int defaultValue) {
        final LongFunction<Integer> integerLongFunction;
        integerLongFunction = l -> this.parsedOp.getOptionalStaticValue(paramName, String.class)
            .filter(Predicate.not(String::isEmpty))
            .map(value -> NumberUtils.toInt(value))
            .map(value -> {
                if (0 > value) {
                    return 0;
                }
                return value;
            }).orElse(defaultValue);
        PulsarBaseOpDispenser.logger.info("{}: {}", paramName, integerLongFunction.apply(0));
        return integerLongFunction;
    }

    // If the corresponding Op parameter is not provided, use the specified default value
    protected LongFunction<String> lookupOptionalStrOpValueFunc(final String paramName, final String defaultValue) {
        final LongFunction<String> stringLongFunction;
        stringLongFunction = this.parsedOp.getAsOptionalFunction(paramName, String.class)
            .orElse(l -> defaultValue);
        PulsarBaseOpDispenser.logger.info("{}: {}", paramName, stringLongFunction.apply(0));

        return stringLongFunction;
    }
    protected LongFunction<String> lookupOptionalStrOpValueFunc(final String paramName) {
        return this.lookupOptionalStrOpValueFunc(paramName, "");
    }

    // Mandatory Op parameter. Throw an error if not specified or having empty value
    protected LongFunction<String> lookupMandtoryStrOpValueFunc(final String paramName) {
        final LongFunction<String> stringLongFunction;
        stringLongFunction = this.parsedOp.getAsRequiredFunction(paramName, String.class);
        PulsarBaseOpDispenser.logger.info("{}: {}", paramName, stringLongFunction.apply(0));

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
    private String getPulsarAPIMetricsPrefix(final String apiType, final String apiObjName, final String topicName) {
        String apiMetricsPrefix = "";

        if (PulsarAdapterUtil.isValidPulsarApiType(apiType)) {
            if (!StringUtils.isBlank(apiObjName)) apiMetricsPrefix = apiObjName + '_';
            else {
                // we want a meaningful name for the API object (producer, consumer, reader, etc.)
                // we are not appending the topic name
                apiMetricsPrefix = apiType;

                if (apiType.equalsIgnoreCase(PULSAR_API_TYPE.PRODUCER.label)) {
                    apiMetricsPrefix += this.pulsarSpace.getProducerSetCnt();
                } else if (apiType.equalsIgnoreCase(PULSAR_API_TYPE.CONSUMER.label)) {
                    apiMetricsPrefix += this.pulsarSpace.getConsumerSetCnt();
                } else if (apiType.equalsIgnoreCase(PULSAR_API_TYPE.READER.label)) {
                    apiMetricsPrefix += this.pulsarSpace.getReaderSetCnt();
                }

                apiMetricsPrefix += "_";
            }

            apiMetricsPrefix += topicName + '_';
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


    // A configuration parameter can be set either at the global level (pulsar_config.properties file),
    //   or at the cycle level (<nb_scenario>.yaml file).
    // If set at both levels, cycle level setting takes precedence
    private String getEffectiveConValue(final String confCategory, final String confParamName, final String cycleConfValue) {
        if (!StringUtils.isBlank(cycleConfValue)) return cycleConfValue;

        if (PulsarAdapterUtil.isValidConfCategory(confCategory)) {
            Map<String, String> catConfMap = new HashMap<>();

            if (StringUtils.equalsIgnoreCase(confCategory, CONF_GATEGORY.Schema.label)) {
                catConfMap = this.pulsarSpace.getPulsarNBClientConf().getSchemaConfMapRaw();
            } else if (StringUtils.equalsIgnoreCase(confCategory, CONF_GATEGORY.Client.label)) {
                catConfMap = this.pulsarSpace.getPulsarNBClientConf().getClientConfMapRaw();
            } else if (StringUtils.equalsIgnoreCase(confCategory, CONF_GATEGORY.Producer.label)) {
                catConfMap = this.pulsarSpace.getPulsarNBClientConf().getProducerConfMapRaw();
            } else if (StringUtils.equalsIgnoreCase(confCategory, CONF_GATEGORY.Consumer.label)) {
                catConfMap = this.pulsarSpace.getPulsarNBClientConf().getConsumerConfMapRaw();
            } else if (StringUtils.equalsIgnoreCase(confCategory, CONF_GATEGORY.Reader.label)) {
                catConfMap = this.pulsarSpace.getPulsarNBClientConf().getReaderConfMapRaw();
            }

            final String globalConfValue = catConfMap.get(confParamName);
            if (!StringUtils.isBlank(globalConfValue)) return globalConfValue;
        }

        return "";
    }


    public Producer<?> getProducer(final String cycleTopicName, final String cycleProducerName) {
        final String topicName = this.getEffectiveConValue(
            CONF_GATEGORY.Producer.label,
            PRODUCER_CONF_STD_KEY.topicName.label,
            cycleTopicName);

        final String producerName = this.getEffectiveConValue(
            CONF_GATEGORY.Producer.label,
            PRODUCER_CONF_STD_KEY.producerName.label,
            cycleProducerName);

        final ProducerCacheKey producerCacheKey = new ProducerCacheKey(producerName, topicName);

        return this.pulsarSpace.getProducer(producerCacheKey, () -> {
            final PulsarClient pulsarClient = this.pulsarSpace.getPulsarClient();

            // Get other possible producer settings that are set at global level
            final Map<String, Object> producerConf = this.pulsarSpace.getPulsarNBClientConf().getProducerConfMapTgt();

            // Remove global level settings
            producerConf.remove(PRODUCER_CONF_STD_KEY.topicName.label);
            producerConf.remove(PRODUCER_CONF_STD_KEY.producerName.label);

            try {
                ProducerBuilder<?> producerBuilder = pulsarClient.
                    newProducer(this.pulsarSpace.getPulsarSchema()).
                    loadConf(producerConf).
                    topic(topicName);

                if (!StringUtils.isAnyBlank(producerName)) producerBuilder = producerBuilder.producerName(producerName);

                final Producer<?> producer = producerBuilder.create();
                this.pulsarAdapterMetrics.registerProducerApiMetrics(producer);
                return producer;
            } catch (final PulsarClientException ple) {
                throw new PulsarAdapterUnexpectedException("Failed to create a Pulsar producer.");
            }
        });
    }

    private List<String> getEffectiveConsumerTopicNameList(final String cycleTopicNameListStr) {
        final String effectiveTopicNamesStr = this.getEffectiveConValue(
            CONF_GATEGORY.Consumer.label,
            CONSUMER_CONF_STD_KEY.topicNames.label,
            cycleTopicNameListStr);

        final String[] names = effectiveTopicNamesStr.split("[;,]");
        final ArrayList<String> effectiveTopicNameList = new ArrayList<>();

        for (final String name : names)
            if (!StringUtils.isBlank(name)) {
                effectiveTopicNameList.add(name.trim());
            }

        return effectiveTopicNameList;
    }

    private SubscriptionType getEffectiveSubscriptionType(final String cycleSubscriptionType) {
        final String subscriptionTypeStr = this.getEffectiveConValue(
            CONF_GATEGORY.Consumer.label,
            CONSUMER_CONF_STD_KEY.subscriptionType.label,
            cycleSubscriptionType);

        SubscriptionType subscriptionType = SubscriptionType.Exclusive; // default subscription type
        if (!StringUtils.isBlank(subscriptionTypeStr)) try {
            subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr);
        } catch (final Exception e) {
            throw new PulsarAdapterInvalidParamException(
                "Invalid effective subscription type for a consumer (\"" + subscriptionTypeStr + "\"). " +
                    "It must be one of the following values: " + PulsarAdapterUtil.getValidSubscriptionTypeList());
        }

        return subscriptionType;
    }

    public Consumer<?> getConsumer(final String cycleTopicNameListStr,
                                   final String cycleTopicPatternStr,
                                   final String cycleSubscriptionName,
                                   final String cycleSubscriptionType,
                                   final String cycleConsumerName,
                                   final String cycleKeySharedSubscriptionRanges) {

        final List<String> topicNameList = this.getEffectiveConsumerTopicNameList(cycleTopicNameListStr);

        final String topicPatternStr = StringUtils.trimToNull(this.getEffectiveConValue(
            CONF_GATEGORY.Consumer.label,
            CONSUMER_CONF_STD_KEY.topicsPattern.label,
            cycleTopicPatternStr));

        final String subscriptionName = this.getEffectiveConValue(
            CONF_GATEGORY.Consumer.label,
            CONSUMER_CONF_STD_KEY.subscriptionName.label,
            cycleSubscriptionName);

        final SubscriptionType subscriptionType = this.getEffectiveSubscriptionType(cycleSubscriptionType);

        final String consumerName = this.getEffectiveConValue(
            CONF_GATEGORY.Consumer.label,
            CONSUMER_CONF_STD_KEY.consumerName.label,
            cycleConsumerName);

        if (SubscriptionType.Exclusive == subscriptionType && 1 < totalThreadNum)
            throw new PulsarAdapterInvalidParamException(
                CONSUMER_CONF_STD_KEY.subscriptionType.label,
                "creating multiple consumers of \"Exclusive\" subscription type under the same subscription name");

        if (topicNameList.isEmpty() == (null == topicPatternStr)) throw new PulsarAdapterInvalidParamException(
            "Invalid combination of topic name(s) and topic patterns; only specify one parameter!");

        return this.pulsarSpace.getConsumer(
            new ConsumerCacheKey(consumerName, subscriptionName, topicNameList, topicPatternStr), () -> {
            final PulsarClient pulsarClient = this.pulsarSpace.getPulsarClient();

            // Get other possible consumer settings that are set at global level
            final Map<String, Object> consumerConf =
                new HashMap<>(this.pulsarSpace.getPulsarNBClientConf().getConsumerConfMapTgt());
            final Map<String, Object> consumerConfToLoad = new HashMap<>();
            consumerConfToLoad.putAll(consumerConf);

            try {
                final ConsumerBuilder<?> consumerBuilder;

                // Remove settings that will be handled outside "loadConf()"
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.topicNames.label);
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.topicsPattern.label);
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.subscriptionName.label);
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.subscriptionType.label);
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.consumerName.label);

                // TODO: It looks like loadConf() method can't handle the following settings properly.
                //       Do these settings manually for now
                //       - deadLetterPolicy
                //       - negativeAckRedeliveryBackoff
                //       - ackTimeoutRedeliveryBackoff
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.deadLetterPolicy.label);
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.negativeAckRedeliveryBackoff.label);
                consumerConfToLoad.remove(CONSUMER_CONF_STD_KEY.ackTimeoutRedeliveryBackoff.label);

                final boolean multiTopicConsumer = 1 < topicNameList.size() || null != topicPatternStr;
                if (!multiTopicConsumer) {
                    assert 1 == topicNameList.size();
                    consumerBuilder = pulsarClient.newConsumer(this.pulsarSpace.getPulsarSchema());
                    consumerBuilder.topic(topicNameList.get(0));
                }
                else {
                    consumerBuilder = pulsarClient.newConsumer();
                    if (!topicNameList.isEmpty()) {
                        assert 1 < topicNameList.size();
                        consumerBuilder.topics(topicNameList);
                    }
                    else {
                        final Pattern topicPattern = Pattern.compile(topicPatternStr);
                        consumerBuilder.topicsPattern(topicPattern);
                    }
                }

                consumerBuilder.loadConf(consumerConfToLoad);

                if (consumerConf.containsKey(CONSUMER_CONF_STD_KEY.deadLetterPolicy.label))
                    consumerBuilder.deadLetterPolicy((DeadLetterPolicy)
                        consumerConf.get(CONSUMER_CONF_STD_KEY.deadLetterPolicy.label));
                if (consumerConf.containsKey(CONSUMER_CONF_STD_KEY.negativeAckRedeliveryBackoff.label))
                    consumerBuilder.negativeAckRedeliveryBackoff((RedeliveryBackoff)
                        consumerConf.get(CONSUMER_CONF_STD_KEY.negativeAckRedeliveryBackoff.label));
                if (consumerConf.containsKey(CONSUMER_CONF_STD_KEY.ackTimeoutRedeliveryBackoff.label))
                    consumerBuilder.ackTimeoutRedeliveryBackoff((RedeliveryBackoff)
                        consumerConf.get(CONSUMER_CONF_STD_KEY.ackTimeoutRedeliveryBackoff.label));

                consumerBuilder
                    .subscriptionName(subscriptionName)
                    .subscriptionType(subscriptionType);

                if (!StringUtils.isBlank(consumerName)) {
                    consumerBuilder.consumerName(consumerName);
                }

                if (SubscriptionType.Key_Shared == subscriptionType) {
                    KeySharedPolicy keySharedPolicy = KeySharedPolicy.autoSplitHashRange();
                    if ((null != cycleKeySharedSubscriptionRanges) && !cycleKeySharedSubscriptionRanges.isEmpty()) {
                        final Range[] ranges = PulsarBaseOpDispenser.parseRanges(cycleKeySharedSubscriptionRanges);
                        PulsarBaseOpDispenser.logger.info("Configuring KeySharedPolicy#stickyHashRange with ranges {}", ranges);
                        keySharedPolicy = KeySharedPolicy.stickyHashRange().ranges(ranges);
                    }
                    consumerBuilder.keySharedPolicy(keySharedPolicy);
                }

                final Consumer<?> consumer = consumerBuilder.subscribe();

                final String consumerTopicListString = !topicNameList.isEmpty() ? String.join("|", topicNameList) : topicPatternStr;
                this.pulsarAdapterMetrics.registerConsumerApiMetrics(
                    consumer,
                    this.getPulsarAPIMetricsPrefix(
                        PULSAR_API_TYPE.CONSUMER.label,
                        consumerName,
                        consumerTopicListString));

                return consumer;
            }
            catch (final PulsarClientException ple) {
                throw new PulsarAdapterUnexpectedException("Failed to create a Pulsar consumer!");
            }
        });
    }

    private static Range[] parseRanges(final String ranges) {
        if ((null == ranges) || ranges.isEmpty()) return new Range[0];
        final String[] split = ranges.split(",");
        final Range[] result = new Range[split.length];
        for (int i = 0; i < split.length; i++) {
            final String range = split[i];
            final int pos = range.indexOf("..");
            if (0 >= pos) throw new IllegalArgumentException("Invalid range '" + range + '\'');
            try {
                final int start = Integer.parseInt(range.substring(0, pos));
                final int end = Integer.parseInt(range.substring(pos + 2));
                result[i] = Range.of(start, end);
            } catch (final NumberFormatException err) {
                throw new IllegalArgumentException("Invalid range '" + range + '\'');
            }
        }
        return result;
    }

    public Reader<?> getReader(final String cycleTopicName,
                               final String cycleReaderName,
                               final String cycleStartMsgPos) {

        final String topicName = this.getEffectiveConValue(
            CONF_GATEGORY.Reader.label,
            READER_CONF_STD_KEY.topicName.label,
            cycleTopicName);

        final String readerName = this.getEffectiveConValue(
            CONF_GATEGORY.Reader.label,
            READER_CONF_STD_KEY.readerName.label,
            cycleReaderName);

        final String startMsgPosStr = this.getEffectiveConValue(
            CONF_GATEGORY.Reader.label,
            READER_CONF_CUSTOM_KEY.startMessagePos.label,
            cycleStartMsgPos);
        if (!PulsarAdapterUtil.isValideReaderStartPosition(startMsgPosStr))
            throw new RuntimeException("Reader:: Invalid value for reader start message position!");

        return this.pulsarSpace.getReader(new ReaderCacheKey(readerName, topicName, startMsgPosStr), () -> {
            final PulsarClient pulsarClient = this.pulsarSpace.getPulsarClient();

            final Map<String, Object> readerConf = this.pulsarSpace.getPulsarNBClientConf().getReaderConfMapTgt();

            // Remove global level settings: "topicName" and "readerName"
            readerConf.remove(READER_CONF_STD_KEY.topicName.label);
            readerConf.remove(READER_CONF_STD_KEY.readerName.label);
            // Remove non-standard reader configuration properties
            readerConf.remove(READER_CONF_CUSTOM_KEY.startMessagePos.label);

            try {
                final ReaderBuilder<?> readerBuilder = pulsarClient.
                    newReader(this.pulsarSpace.getPulsarSchema()).
                    loadConf(readerConf).
                    topic(topicName).
                    readerName(readerName);

                MessageId startMsgId = MessageId.latest;
                if (startMsgPosStr.equalsIgnoreCase(READER_MSG_POSITION_TYPE.earliest.label))
                    startMsgId = MessageId.earliest;
                //TODO: custom start message position is NOT supported yet
                //else if (startMsgPosStr.startsWith(PulsarAdapterUtil.READER_MSG_POSITION_TYPE.custom.label)) {
                //    startMsgId = MessageId.latest;
                //}

                return readerBuilder.startMessageId(startMsgId).create();
            } catch (final PulsarClientException ple) {
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
