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
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label).collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static boolean isValidPulsarApiType(String param) {
        return PULSAR_API_TYPE.isValidLabel(param);
    }
    public static String getValidPulsarApiTypeList() {
        return Arrays.stream(PULSAR_API_TYPE.values()).map(t -> t.label).collect(Collectors.joining(", "));
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

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static boolean isValidSubscriptionType(String item) {
        return SUBSCRIPTION_TYPE.isValidLabel(item);
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

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static boolean isValideReaderStartPosition(String item) {
        return READER_MSG_POSITION_TYPE.isValidLabel(item);
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

        private static final Map<String, SEQ_ERROR_SIMU_TYPE> MAPPING = Stream.of(values())
            .flatMap(simuType ->
                Stream.of(simuType.label,
                        simuType.label.toLowerCase(),
                        simuType.label.toUpperCase(),
                        simuType.name(),
                        simuType.name().toLowerCase(),
                        simuType.name().toUpperCase())
                    .distinct().map(key -> Map.entry(key, simuType)))
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

        public static Optional<SEQ_ERROR_SIMU_TYPE> parseSimuType(String simuTypeString) {
            return Optional.ofNullable(MAPPING.get(simuTypeString.trim()));
        }
    }

    private static final Map<String, Schema<?>> PRIMITIVE_SCHEMA_TYPE_MAPPING = Stream.of(SchemaType.values())
        .filter(SchemaType::isPrimitive)
        .collect(Collectors.toUnmodifiableMap(schemaType -> schemaType.name().toUpperCase(),
            schemaType -> Schema.getSchema(SchemaInfo.builder().type(schemaType).build())));

    ///////
    // Primitive Schema type
    public static boolean isPrimitiveSchemaTypeStr(String typeStr) {
        return StringUtils.isBlank(typeStr) || PRIMITIVE_SCHEMA_TYPE_MAPPING.containsKey(typeStr.toUpperCase());
    }

    public static Schema<?> getPrimitiveTypeSchema(String typeStr) {
        String lookupKey = StringUtils.isBlank(typeStr) ? "BYTES" : typeStr.toUpperCase();
        Schema<?> schema = PRIMITIVE_SCHEMA_TYPE_MAPPING.get(lookupKey);
        if (schema == null) {
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

