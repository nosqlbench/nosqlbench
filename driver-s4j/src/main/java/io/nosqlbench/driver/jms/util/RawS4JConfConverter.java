package io.nosqlbench.driver.jms.util;

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

import com.datastax.oss.pulsar.jms.shaded.org.apache.pulsar.client.api.CompressionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nosqlbench.driver.jms.excption.S4JConfigParamException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to convert the configuration items in its raw
 * format (as provided in the property file) to the format needed by
 * the S4J driver
 */
public class RawS4JConfConverter {

    public static Map<String, Object> convertRawClientConf(Map<String, String> pulsarClientConfMapRaw) {
        Map<String, Object> s4jClientConfObjMap = new HashMap<>();
        s4jClientConfObjMap.putAll(pulsarClientConfMapRaw);

        /**
         * No special handling for non-primitive types
         */

        return s4jClientConfObjMap;
    }

    // <<< https://pulsar.apache.org/docs/client-libraries-java/#configure-producer >>>
    private final static Map<String, String> validPulsarProducerConfKeyTypeMap = Map.ofEntries(
        Map.entry("topicName", "String"),
        Map.entry("producerName","String"),
        Map.entry("sendTimeoutMs","long"),
        Map.entry("blockIfQueueFull","boolean"),
        Map.entry("maxPendingMessages","int"),
        Map.entry("maxPendingMessagesAcrossPartitions","int"),
        Map.entry("messageRoutingMode","MessageRoutingMode"),
        Map.entry("hashingScheme","HashingScheme"),
        Map.entry("cryptoFailureAction","ProducerCryptoFailureAction"),
        Map.entry("batchingMaxPublishDelayMicros","long"),
        Map.entry("batchingMaxMessages","int"),
        Map.entry("batchingEnabled","boolean"),
        Map.entry("chunkingEnabled","boolean"),
        Map.entry("compressionType","CompressionType"),
        Map.entry("initialSubscriptionName","string")
    );
    public static Map<String, Object> convertRawProducerConf(Map<String, String> pulsarProducerConfMapRaw) {
        Map<String, Object> s4jProducerConfObjMap = new HashMap<>();
        setConfObjMapForPrimitives(s4jProducerConfObjMap, pulsarProducerConfMapRaw, validPulsarProducerConfKeyTypeMap);

        /**
         * Non-primitive type processing for Pulsar producer configuration items
         */
        // "compressionType" has value type "CompressionType"
        // - expecting the following values: 'LZ4', 'ZLIB', 'ZSTD', 'SNAPPY'
        String confKeyName = "compressionType";
        String confVal = pulsarProducerConfMapRaw.get(confKeyName);
        String expectedVal = "(LZ4|ZLIB|ZSTD|SNAPPY)";

        if (StringUtils.isNotBlank(confVal)) {
            if (StringUtils.equalsAnyIgnoreCase(confVal, "LZ4", "ZLIB", "ZSTD", "SNAPPY")) {
                CompressionType compressionType = CompressionType.NONE;

                switch (StringUtils.upperCase(confVal)) {
                    case "LZ4":
                        compressionType = CompressionType.LZ4;
                    case "ZLIB":
                        compressionType = CompressionType.ZLIB;
                    case "ZSTD":
                        compressionType = CompressionType.ZSTD;
                    case "SNAPPY":
                        compressionType = CompressionType.SNAPPY;
                }

                s4jProducerConfObjMap.put(confKeyName, compressionType);
            } else {
                throw new S4JConfigParamException(
                    getInvalidConfValStr(confKeyName, confVal, "producer", expectedVal));
            }
        }

        // TODO: Skip the following Pulsar configuration items for now because they're not really
        //       needed in the NB S4J testing at the moment. Add support for them when needed.
        //       * messageRoutingMode
        //       * hashingScheme
        //       * cryptoFailureAction

        return s4jProducerConfObjMap;
    }

    // https://pulsar.apache.org/docs/client-libraries-java/#configure-consumer
    private final static Map<String, String> validPulsarConsumerConfKeyTypeMap = Map.ofEntries(
        Map.entry("topicNames", "Set<String>"),
        Map.entry("topicsPattern","Pattern"),
        Map.entry("subscriptionName","String"),
        Map.entry("subscriptionType","SubscriptionType"),
        Map.entry("receiverQueueSize","int"),
        Map.entry("acknowledgementsGroupTimeMicros","long"),
        Map.entry("negativeAckRedeliveryDelayMicros","long"),
        Map.entry("maxTotalReceiverQueueSizeAcrossPartitions","int"),
        Map.entry("consumerName","String"),
        Map.entry("ackTimeoutMillis","long"),
        Map.entry("tickDurationMillis","long"),
        Map.entry("priorityLevel","int"),
        Map.entry("cryptoFailureAction","ConsumerCryptoFailureAction"),
        Map.entry("properties","SortedMap<String, String>"),
        Map.entry("readCompacted","boolean"),
        Map.entry("subscriptionInitialPosition", "SubscriptionInitialPosition"),
        Map.entry("patternAutoDiscoveryPeriod", "int"),
        Map.entry("regexSubscriptionMode", "RegexSubscriptionMode"),
        Map.entry("deadLetterPolicy", "DeadLetterPolicy"),
        Map.entry("autoUpdatePartitions", "boolean"),
        Map.entry("replicateSubscriptionState", "boolean"),
        Map.entry("negativeAckRedeliveryBackoff", "RedeliveryBackoff"),
        Map.entry("ackTimeoutRedeliveryBackoff", "RedeliveryBackoff"),
        Map.entry("autoAckOldestChunkedMessageOnQueueFull", "boolean"),
        Map.entry("maxPendingChunkedMessage", "int"),
        Map.entry("expireTimeOfIncompleteChunkedMessageMillis", "long")
    );
    public static Map<String, Object> convertRawConsumerConf(Map<String, String> pulsarConsumerConfMapRaw) {
        Map<String, Object> s4jConsumerConfObjMap = new HashMap<>();
        setConfObjMapForPrimitives(s4jConsumerConfObjMap, pulsarConsumerConfMapRaw, validPulsarConsumerConfKeyTypeMap);

        /**
         * Non-primitive type processing for Pulsar consumer configuration items
         */
        // The following non-primitive type configuration items are already excluded
        // and don't need to be processed.
        // * topicNames
        // * topicPattern
        // * subscriptionName
        // * subscriptionType
        // * subscriptionInitialPosition
        // * regexSubscriptionMode

        // "properties" has value type "SortedMap<String, String>"
        // - expecting the value string has the format: a JSON string that includes a set of key/value pairs
        String confKeyName = "properties";
        String confVal = pulsarConsumerConfMapRaw.get(confKeyName);
        String expectedVal = "{\"property1\":\"value1\", \"property2\":\"value2\"}, ...";

        ObjectMapper mapper = new ObjectMapper();

        if (StringUtils.isNotBlank(confVal)) {
            try {
                Map<String, String> consumerProperties = mapper.readValue(confVal, Map.class);

                // Empty map value is considered as no value
                if (!consumerProperties.isEmpty()) {
                    s4jConsumerConfObjMap.put(confKeyName, consumerProperties);
                }

            } catch (Exception e) {
                throw new S4JConfigParamException(
                    getInvalidConfValStr(confKeyName, confVal, "consumer", expectedVal));
            }
        }

        // "deadLetterPolicy"
        // - expecting the value is a JSON string has the format:
        //   {"maxRedeliverCount":"<int_value>","deadLetterTopic":"<topic_name>","initialSubscriptionName":"<sub_name>"}
        confKeyName = "deadLetterPolicy";
        confVal = pulsarConsumerConfMapRaw.get(confKeyName);
        expectedVal = "{" +
            "\"maxRedeliverCount\":\"<int_value>\"," +
            "\"deadLetterTopic\":\"<topic_name>\"," +
            "\"initialSubscriptionName\":\"<sub_name>\"}";

        if (StringUtils.isNotBlank(confVal)) {
            try {
                Map<String, String> dlqPolicyMap = mapper.readValue(confVal, Map.class);

                // Empty map value is considered as no value
                if (!dlqPolicyMap.isEmpty()) {
                    boolean valid = true;

                    // The JSON key must be one of "maxRedeliverCount", "deadLetterTopic", "initialSubscriptionName"
                    for (String key : dlqPolicyMap.keySet()) {
                        if (!StringUtils.equalsAnyIgnoreCase(key,
                            "maxRedeliverCount", "deadLetterTopic", "initialSubscriptionName")) {
                            valid = false;
                            break;
                        }
                    }

                    // DLQ.maxRedeliverCount is mandatory
                    if (valid && !dlqPolicyMap.containsKey("maxRedeliverCount")) {
                        valid = false;
                    }

                    String maxRedeliverCountStr = dlqPolicyMap.get("maxRedeliverCount");
                    if (!NumberUtils.isCreatable(maxRedeliverCountStr)) {
                        valid = false;
                    }

                    if (valid) {
                        // In S4J driver, DLQ setting is done via a Map
                        // <<< https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-implementation.html#dead-letter-policy >>>

                        s4jConsumerConfObjMap.put(confKeyName, dlqPolicyMap);
                    } else {
                        throw new S4JConfigParamException(
                            getInvalidConfValStr(confKeyName, confVal, "consumer", expectedVal));
                    }
                }
            } catch (Exception e) {
                throw new S4JConfigParamException(
                    getInvalidConfValStr(confKeyName, confVal, "consumer", expectedVal));
            }
        }

        // "negativeAckRedeliveryBackoff" or "ackTimeoutRedeliveryBackoff"
        // - expecting the value is a JSON string has the format:
        //   {"minDelayMs":"<int_value>", "maxDelayMs":"<int_value>", "multiplier":"<double_value>"}
        String[] redeliveryBackoffConfigSet = {"negativeAckRedeliveryBackoff", "ackTimeoutRedeliveryBackoff"};
        expectedVal = "{" +
            "\"minDelayMs\":\"<int_value>\"," +
            "\"maxDelayMs\":\"<int_value>\"," +
            "\"multiplier\":\"<double_value>\"}";

        for (String confKey : redeliveryBackoffConfigSet) {
            confVal = pulsarConsumerConfMapRaw.get(confKey);

            if (StringUtils.isNotBlank(confVal)) {
                try {
                    Map<String, String> redliveryBackoffMap = mapper.readValue(confVal, Map.class);

                    // Empty map value is considered as no value
                    if (! redliveryBackoffMap.isEmpty()) {
                        boolean valid = true;

                        // The JSON key must be one of "maxRedeliverCount", "deadLetterTopic", "initialSubscriptionName"
                        for (String key : redliveryBackoffMap.keySet()) {
                            if (!StringUtils.equalsAnyIgnoreCase(key,
                                "minDelayMs", "maxDelayMs", "multiplier")) {
                                valid = false;
                                break;
                            }
                        }

                        String minDelayMsStr = redliveryBackoffMap.get("minDelayMs");
                        String maxDelayMsStr = redliveryBackoffMap.get("maxDelayMs");
                        String multiplierStr = redliveryBackoffMap.get("multiplier");

                        if ((StringUtils.isNotBlank(minDelayMsStr) && !NumberUtils.isCreatable(minDelayMsStr)) ||
                            (StringUtils.isNotBlank(maxDelayMsStr) && !NumberUtils.isCreatable(maxDelayMsStr)) ||
                            (StringUtils.isNotBlank(multiplierStr) && !NumberUtils.isCreatable(multiplierStr))) {
                            valid = false;
                        }

                        if (valid) {
                            // In S4J driver, AckTimeOut and Negative TimeOut is done via a Map
                            // <<< https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-implementation.html#ack-timeout >>>
                            // <<< https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-implementation.html#negative-ack >>>

                            s4jConsumerConfObjMap.put(confKey, redliveryBackoffMap);

                        } else {
                            throw new S4JConfigParamException(
                                getInvalidConfValStr(confKey, confVal, "consumer", expectedVal));
                        }
                    }

                } catch (Exception e) {
                    throw new S4JConfigParamException(
                        getInvalidConfValStr(confKey, confVal, "consumer", expectedVal));
                }
            }
        }


        // TODO: Skip the following Pulsar configuration items for now because they're not really
        //       needed in the NB S4J testing right now. Add the support for them when needed.
        //       * cryptoFailureAction

        return s4jConsumerConfObjMap;
    }

    // https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-reference.html#_configuration_options
    private final static Map<String, String> validS4jJmsConfKeyTypeMap = Map.ofEntries(
        Map.entry("jms.acknowledgeRejectedMessages", "boolean"),
        Map.entry("jms.clientId","String"),
        Map.entry("jms.emulateTransactions","boolean"),
        Map.entry("jms.enableClientSideEmulation","boolean"),
        Map.entry("jms.forceDeleteTemporaryDestinations","boolean"),
        Map.entry("jms.precreateQueueSubscription","boolean"),
        Map.entry("jms.queueSubscriptionName","String"),
        Map.entry("jms.systemNamespace","String"),
        Map.entry("jms.topicSharedSubscriptionType","String"),
        Map.entry("jms.useCredentialsFromCreateConnection","boolean"),
        Map.entry("jms.useExclusiveSubscriptionsForSimpleConsumers","long"),
        Map.entry("jms.usePulsarAdmin","boolean"),
        Map.entry("jms.useServerSideFiltering","boolean"),
        Map.entry("jms.waitForServerStartupTimeout","int"),
        Map.entry("jms.transactionsStickyPartitions", "boolean")
    );
    public static Map<String, Object> convertRawJmsConf(Map<String, String> s4jJmsConfMapRaw) {
        Map<String, Object> s4jJmsConfObjMap = new HashMap<>();
        setConfObjMapForPrimitives(s4jJmsConfObjMap, s4jJmsConfMapRaw, validS4jJmsConfKeyTypeMap);

        /**
         * Non-primitive type processing for Pulsar client configuration items
         */
        // None

        return s4jJmsConfObjMap;
    }

    // https://docs.datastax.com/en/fast-pulsar-jms/docs/1.1/pulsar-jms-reference.html#_configuration_options
    private final static Map<String, String> validS4jMiscConfKeyTypeMap = Map.ofEntries(
        Map.entry("brokerServiceUrl","String"),
        Map.entry("webServiceUrl","String"),
        Map.entry("ackTimeout", "long"),
        Map.entry("ackTimeoutMillis","long"),
        Map.entry("enableTransaction","boolean"),
        Map.entry("consumerConfig","Map<String,Object>"),
        Map.entry("producerConfig","Map<String,Object>")
    );
    public static Map<String, Object> convertRawMiscConf(Map<String, String> s4jMiscConfMapRaw) {
        Map<String, Object> s4jMiscConfObjMap = new HashMap<>();
        setConfObjMapForPrimitives(s4jMiscConfObjMap, s4jMiscConfMapRaw, validS4jMiscConfKeyTypeMap);

        /**
         * Non-primitive type processing for Pulsar client configuration items
         */
        // Only the following 2 non-primitive type settings will be set explicitly
        // * producerConfig
        // * consumerConfig

        return s4jMiscConfObjMap;
    }


    // Utility function
    // - get configuration key names by the value type
    private static List<String> getConfKeyNameByValueType(Map<String, String> confKeyTypeMap, String tgtValType) {
        ArrayList<String> confKeyNames = new ArrayList<>();

        for (Map.Entry entry: confKeyTypeMap.entrySet()) {
            if (StringUtils.equalsIgnoreCase(entry.getValue().toString(), tgtValType)) {
                confKeyNames.add(entry.getKey().toString());
            }
        }

        return confKeyNames;
    }

    // Conversion from Map<String, String> to Map<String, Object> for configuration items with primitive
    // value types
    private static void setConfObjMapForPrimitives(
        Map<String, Object> tgtConfObjMap,
        Map<String, String> srcConfMapRaw,
        Map<String, String> validConfKeyTypeMap)
    {
        List<String> confKeyList = new ArrayList<>();

        // All configuration items with "String" as the value type
        confKeyList = getConfKeyNameByValueType(validConfKeyTypeMap, "String");
        for (String confKey : confKeyList) {
            if (srcConfMapRaw.containsKey(confKey)) {
                String confVal = srcConfMapRaw.get(confKey);
                if (StringUtils.isNotBlank(confVal)) {
                    tgtConfObjMap.put(confKey, confVal);
                }
            }
        }

        // All configuration items with "long" as the value type
        confKeyList = getConfKeyNameByValueType(validConfKeyTypeMap, "long");
        for (String confKey : confKeyList) {
            if (srcConfMapRaw.containsKey(confKey)) {
                String confVal = srcConfMapRaw.get(confKey);
                if (StringUtils.isNotBlank(confVal)) {
                    tgtConfObjMap.put(confKey, Long.valueOf(confVal));
                }
            }
        }

        // All configuration items with "int" as the value type
        confKeyList = getConfKeyNameByValueType(validConfKeyTypeMap, "int");
        for (String confKey : confKeyList) {
            if (srcConfMapRaw.containsKey(confKey)) {
                String confVal = srcConfMapRaw.get(confKey);
                if (StringUtils.isNotBlank(confVal)) {
                    tgtConfObjMap.put(confKey, Integer.valueOf(confVal));
                }
            }
        }

        // All configuration items with "boolean" as the value type
        confKeyList = getConfKeyNameByValueType(validConfKeyTypeMap, "boolean");
        for (String confKey : confKeyList) {
            if (srcConfMapRaw.containsKey(confKey)) {
                String confVal = srcConfMapRaw.get(confKey);
                if (StringUtils.isNotBlank(confVal)) {
                    tgtConfObjMap.put(confKey, Boolean.valueOf(confVal));
                }
            }
        }

        // TODO: So far the above primitive types should be good enough.
        //       Add support for other types when needed
    }

    private static String getInvalidConfValStr(String confKey, String confVal, String configCategory, String expectedVal) {
        return "Incorrect value \"" + confVal + "\" for Pulsar " + configCategory +
            " configuration item of \"" + confKey + "\". Expecting the following value (format): " + expectedVal;
    }
}
