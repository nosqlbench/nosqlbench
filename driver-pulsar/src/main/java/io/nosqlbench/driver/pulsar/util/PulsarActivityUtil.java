package io.nosqlbench.driver.pulsar.util;

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


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Schema;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PulsarActivityUtil {

    private final static Logger logger = LogManager.getLogger(PulsarActivityUtil.class);

    // Supported message operation types
    // TODO: websocket-producer and managed-ledger
    public enum OP_TYPES {
        ADMIN_TENANT("admin-tenant"),
        ADMIN_NAMESPACE("admin-namespace"),
        ADMIN_TOPIC("admin-topic"),
        E2E_MSG_PROC_SEND("ec2-msg-proc-send"),
        E2E_MSG_PROC_CONSUME("ec2-msg-proc-consume"),
        BATCH_MSG_SEND_START("batch-msg-send-start"),
        BATCH_MSG_SEND("batch-msg-send"),
        BATCH_MSG_SEND_END("batch-msg-send-end"),
        MSG_SEND("msg-send"),
        MSG_CONSUME("msg-consume"),
        MSG_READ("msg-read"),
        MSG_MULTI_CONSUME("msg-mt-consume");

        public final String label;

        OP_TYPES(String label) {
            this.label = label;
        }
    }


    public static final String MSG_SEQUENCE_NUMBER = "sequence_number";

    ///////
    // Valid document level parameters for Pulsar NB yaml file
    public enum DOC_LEVEL_PARAMS {
        TOPIC_URI("topic_uri"),
        ASYNC_API("async_api"),
        USE_TRANSACTION("use_transaction"),
        ADMIN_DELOP("admin_delop"),
        SEQ_TRACKING("seq_tracking"),
        MSG_DEDUP_BROKER("msg_dedup_broker"),
        E2E_STARTING_TIME_SOURCE("e2e_starting_time_source");

        public final String label;

        DOC_LEVEL_PARAMS(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid Pulsar API type
    public enum PULSAR_API_TYPE {
        PRODUCER("producer"),
        CONSUMER("consumer"),
        READER("reader");

        public final String label;

        PULSAR_API_TYPE(String label) {
            this.label = label;
        }
    }
    public static boolean isValidPulsarApiType(String param) {
        return Arrays.stream(PULSAR_API_TYPE.values()).anyMatch(t -> t.label.equals(param));
    }
    public static String getValidPulsarApiTypeList() {
        return Arrays.stream(PULSAR_API_TYPE.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    ///////
    // Valid persistence type
    public enum PERSISTENT_TYPES {
        PERSISTENT("persistent"),
        NON_PERSISTENT("non-persistent")
        ;

        public final String label;
        PERSISTENT_TYPES(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid Pulsar client configuration (activity-level settings)
    // - https://pulsar.apache.org/docs/en/client-libraries-java/#client
    public enum CLNT_CONF_KEY {
        serviceUrl("serviceUrl"),
        authPulginClassName("authPluginClassName"),
        authParams("authParams"),
        pperationTimeoutMs("operationTimeoutMs"),
        statsIntervalSeconds("statsIntervalSeconds"),
        numIoThreads("numIoThreads"),
        numListenerThreads("numListenerThreads"),
        useTcpNoDelay("useTcpNoDelay"),
        useTls("useTls"),
        tlsTrustCertsFilePath("tlsTrustCertsFilePath"),
        tlsAllowInsecureConnection("tlsAllowInsecureConnection"),
        tlsHostnameVerificationEnable("tlsHostnameVerificationEnable"),
        concurrentLookupRequest("concurrentLookupRequest"),
        maxLookupRequest("maxLookupRequest"),
        maxNumberOfRejectedRequestPerConnection("maxNumberOfRejectedRequestPerConnection"),
        keepAliveIntervalSeconds("keepAliveIntervalSeconds"),
        connectionTimeoutMs("connectionTimeoutMs"),
        requestTimeoutMs("requestTimeoutMs"),
        defaultBackoffIntervalNanos("defaultBackoffIntervalNanos"),
        maxBackoffIntervalNanos("maxBackoffIntervalNanos")
        ;

        public final String label;
        CLNT_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Standard producer configuration (activity-level settings)
    // - https://pulsar.apache.org/docs/en/client-libraries-java/#configure-producer
    public enum PRODUCER_CONF_STD_KEY {
        topicName("topicName"),
        producerName("producerName"),
        sendTimeoutMs("sendTimeoutMs"),
        blockIfQueueFull("blockIfQueueFull"),
        maxPendingMessages("maxPendingMessages"),
        maxPendingMessagesAcrossPartitions("maxPendingMessagesAcrossPartitions"),
        messageRoutingMode("messageRoutingMode"),
        hashingScheme("hashingScheme"),
        cryptoFailureAction("cryptoFailureAction"),
        batchingMaxPublishDelayMicros("batchingMaxPublishDelayMicros"),
        batchingMaxMessages("batchingMaxMessages"),
        batchingEnabled("batchingEnabled"),
        compressionType("compressionType");

        public final String label;

        PRODUCER_CONF_STD_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Standard consumer configuration (activity-level settings)
    // - https://pulsar.apache.org/docs/en/client-libraries-java/#consumer
    public enum CONSUMER_CONF_STD_KEY {
        topicNames("topicNames"),
        topicsPattern("topicsPattern"),
        subscriptionName("subscriptionName"),
        subscriptionType("subscriptionType"),
        receiverQueueSize("receiverQueueSize"),
        acknowledgementsGroupTimeMicros("acknowledgementsGroupTimeMicros"),
        negativeAckRedeliveryDelayMicros("negativeAckRedeliveryDelayMicros"),
        maxTotalReceiverQueueSizeAcrossPartitions("maxTotalReceiverQueueSizeAcrossPartitions"),
        consumerName("consumerName"),
        ackTimeoutMillis("ackTimeoutMillis"),
        tickDurationMillis("tickDurationMillis"),
        priorityLevel("priorityLevel"),
        cryptoFailureAction("cryptoFailureAction"),
        properties("properties"),
        readCompacted("readCompacted"),
        subscriptionInitialPosition("subscriptionInitialPosition"),
        patternAutoDiscoveryPeriod("patternAutoDiscoveryPeriod"),
        regexSubscriptionMode("regexSubscriptionMode"),
        deadLetterPolicy("deadLetterPolicy"),
        autoUpdatePartitions("autoUpdatePartitions"),
        replicateSubscriptionState("replicateSubscriptionState");

        public final String label;

        CONSUMER_CONF_STD_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Custom consumer configuration (activity-level settings)
    // - NOT part of https://pulsar.apache.org/docs/en/client-libraries-java/#consumer
    // - NB Pulsar driver consumer operation specific
    public enum CONSUMER_CONF_CUSTOM_KEY {
        timeout("timeout");

        public final String label;

        CONSUMER_CONF_CUSTOM_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Pulsar subscription type
    public enum SUBSCRIPTION_TYPE {
        Exclusive("Exclusive"),
        Failover("Failover"),
        Shared("Shared"),
        Key_Shared("Key_Shared");

        public final String label;

        SUBSCRIPTION_TYPE(String label) {
            this.label = label;
        }
    }
    public static boolean isValidSubscriptionType(String item) {
        return Arrays.stream(SUBSCRIPTION_TYPE.values()).anyMatch(t -> t.label.equals(item));
    }
    public static String getValidSubscriptionTypeList() {
        return Arrays.stream(SUBSCRIPTION_TYPE.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    ///////
    // Standard reader configuration (activity-level settings)
    // - https://pulsar.apache.org/docs/en/client-libraries-java/#reader
    public enum READER_CONF_STD_KEY {
        topicName("topicName"),
        receiverQueueSize("receiverQueueSize"),
        readerListener("readerListener"),
        readerName("readerName"),
        subscriptionRolePrefix("subscriptionRolePrefix"),
        cryptoKeyReader("cryptoKeyReader"),
        cryptoFailureAction("cryptoFailureAction"),
        readCompacted("readCompacted"),
        resetIncludeHead("resetIncludeHead");

        public final String label;

        READER_CONF_STD_KEY(String label) {
            this.label = label;
        }
    }
    ///////
    // Custom reader configuration (activity-level settings)
    // - NOT part of https://pulsar.apache.org/docs/en/client-libraries-java/#reader
    // - NB Pulsar driver reader operation specific
    public enum READER_CONF_CUSTOM_KEY {
        startMessagePos("startMessagePos");

        public final String label;

        READER_CONF_CUSTOM_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid read positions for a Pulsar reader
    public enum READER_MSG_POSITION_TYPE {
        earliest("earliest"),
        latest("latest"),
        custom("custom");

        public final String label;

        READER_MSG_POSITION_TYPE(String label) {
            this.label = label;
        }
    }
    public static boolean isValideReaderStartPosition(String item) {
        return Arrays.stream(READER_MSG_POSITION_TYPE.values()).anyMatch(t -> t.label.equals(item));
    }

    ///////
    // Pulsar subscription type
    public enum SEQ_ERROR_SIMU_TYPE {
        OutOfOrder("out_of_order"),
        MsgLoss("msg_loss"),
        MsgDup("msg_dup");

        public final String label;

        SEQ_ERROR_SIMU_TYPE(String label) {
            this.label = label;
        }

        private static final Map<String, SEQ_ERROR_SIMU_TYPE> MAPPING = new HashMap<>();

        static {
            for (SEQ_ERROR_SIMU_TYPE simuType : values()) {
                MAPPING.put(simuType.label, simuType);
                MAPPING.put(simuType.label.toLowerCase(), simuType);
                MAPPING.put(simuType.label.toUpperCase(), simuType);
                MAPPING.put(simuType.name(), simuType);
                MAPPING.put(simuType.name().toLowerCase(), simuType);
                MAPPING.put(simuType.name().toUpperCase(), simuType);
            }
        }

        public static Optional<SEQ_ERROR_SIMU_TYPE> parseSimuType(String simuTypeString) {
            return Optional.ofNullable(MAPPING.get(simuTypeString.trim()));
        }
    }

    ///////
    // Valid websocket-producer configuration (activity-level settings)
    // TODO: to be added
    public enum WEBSKT_PRODUCER_CONF_KEY {
        ;

        public final String label;

        WEBSKT_PRODUCER_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid managed-ledger configuration (activity-level settings)
    // TODO: to be added
    public enum MANAGED_LEDGER_CONF_KEY {
        ;

        public final String label;
        MANAGED_LEDGER_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Primitive Schema type
    public static boolean isPrimitiveSchemaTypeStr(String typeStr) {
        boolean isPrimitive = false;

        // Use "BYTES" as the default type if the type string is not explicitly specified
        if (StringUtils.isBlank(typeStr)) {
            typeStr = "BYTES";
        }

        if (typeStr.equalsIgnoreCase("BOOLEAN") || typeStr.equalsIgnoreCase("INT8") ||
            typeStr.equalsIgnoreCase("INT16") || typeStr.equalsIgnoreCase("INT32") ||
            typeStr.equalsIgnoreCase("INT64") || typeStr.equalsIgnoreCase("FLOAT") ||
            typeStr.equalsIgnoreCase("DOUBLE") || typeStr.equalsIgnoreCase("BYTES") ||
            typeStr.equalsIgnoreCase("DATE") || typeStr.equalsIgnoreCase("TIME") ||
            typeStr.equalsIgnoreCase("TIMESTAMP") || typeStr.equalsIgnoreCase("INSTANT") ||
            typeStr.equalsIgnoreCase("LOCAL_DATE") || typeStr.equalsIgnoreCase("LOCAL_TIME") ||
            typeStr.equalsIgnoreCase("LOCAL_DATE_TIME")) {
            isPrimitive = true;
        }

        return isPrimitive;
    }
    public static Schema<?> getPrimitiveTypeSchema(String typeStr) {
        Schema<?> schema;

        switch (typeStr.toUpperCase()) {
            case "BOOLEAN":
                schema = Schema.BOOL;
                break;
            case "INT8":
                schema = Schema.INT8;
                break;
            case "INT16":
                schema = Schema.INT16;
                break;
            case "INT32":
                schema = Schema.INT32;
                break;
            case "INT64":
                schema = Schema.INT64;
                break;
            case "FLOAT":
                schema = Schema.FLOAT;
                break;
            case "DOUBLE":
                schema = Schema.DOUBLE;
                break;
            case "DATE":
                schema = Schema.DATE;
                break;
            case "TIME":
                schema = Schema.TIME;
                break;
            case "TIMESTAMP":
                schema = Schema.TIMESTAMP;
                break;
            case "INSTANT":
                schema = Schema.INSTANT;
                break;
            case "LOCAL_DATE":
                schema = Schema.LOCAL_DATE;
                break;
            case "LOCAL_TIME":
                schema = Schema.LOCAL_TIME;
                break;
            case "LOCAL_DATE_TIME":
                schema = Schema.LOCAL_DATE_TIME;
                break;
            // Use BYTES as the default schema type if the type string is not specified
            case "":
            case "BYTES":
                schema = Schema.BYTES;
                break;
            // Report an error if non-valid, non-empty schema type string is provided
            default:
                throw new RuntimeException("Invalid Pulsar primitive schema type string : " + typeStr);
        }

        return schema;
    }

    ///////
    // Complex strut type: Avro or Json
    public static boolean isAvroSchemaTypeStr(String typeStr) {
        return typeStr.equalsIgnoreCase("AVRO");
    }

    // automatic decode the type from the Registry
    public static boolean isAutoConsumeSchemaTypeStr(String typeStr) {
        return typeStr.equalsIgnoreCase("AUTO_CONSUME");
    }
    public static Schema<?> getAvroSchema(String typeStr, String definitionStr) {
        String schemaDefinitionStr = definitionStr;
        String filePrefix = "file://";
        Schema<?> schema;

        // Check if payloadStr points to a file (e.g. "file:///path/to/a/file")
        if (isAvroSchemaTypeStr(typeStr)) {
            if (StringUtils.isBlank(schemaDefinitionStr)) {
                throw new RuntimeException("Schema definition must be provided for \"Avro\" schema type!");
            } else if (schemaDefinitionStr.startsWith(filePrefix)) {
                try {
                    Path filePath = Paths.get(URI.create(schemaDefinitionStr));
                    schemaDefinitionStr = Files.readString(filePath, StandardCharsets.US_ASCII);
                } catch (IOException ioe) {
                    throw new RuntimeException("Error reading the specified \"Avro\" schema definition file: " + definitionStr + ": " + ioe.getMessage());
                }
            }

            schema = AvroUtil.GetSchema_PulsarAvro("NBAvro", schemaDefinitionStr);
        } else {
            throw new RuntimeException("Trying to create a \"Avro\" schema for a non-Avro schema type string: " + typeStr);
        }

        return schema;
    }

    ///////
    // Generate effective key string
    public static String buildCacheKey(String... keyParts) {
        // Ignore blank keyPart
        String joinedKeyStr =
            Stream.of(keyParts)
            .filter(s -> !StringUtils.isBlank(s))
            .collect(Collectors.joining(","));

        return Base64.getEncoder().encodeToString(joinedKeyStr.getBytes());
    }

    ///////
    // Convert JSON string to a key/value map
    private static final ObjectMapper JACKSON_OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE_REF = new TypeReference<>() {};

    public static Map<String, String> convertJsonToMap(String jsonStr) throws IOException {
        return JACKSON_OBJECT_MAPPER.readValue(jsonStr, MAP_TYPE_REF);
    }

    ///////
    // Get full namespace name (<tenant>/<namespace>) from a Pulsar topic URI
    public static String getFullNamespaceName(String topicUri) {
        // Get tenant/namespace string
        // - topicUri   : persistent://<tenant>/<namespace>/<topic>
        // - tmpStr     : <tenant>/<namespace>/<topic>
        // - fullNsName : <tenant>/<namespace>

        String tmpStr = StringUtils.substringAfter(topicUri,"://");
        return StringUtils.substringBeforeLast(tmpStr, "/");
    }
}

