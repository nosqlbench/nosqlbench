package io.nosqlbench.driver.pulsar.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.impl.schema.generic.GenericAvroSchema;
import org.apache.pulsar.common.schema.SchemaInfo;
import org.apache.pulsar.common.schema.SchemaType;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;

public class PulsarActivityUtil {

    private final static Logger logger = LogManager.getLogger(PulsarActivityUtil.class);

    // Supported message operation types
    // TODO: websocket-producer and managed-ledger
    public enum OP_TYPES {
        ADMIN("admin"),
        BATCH_MSG_SEND_START("batch-msg-send-start"),
        BATCH_MSG_SEND("batch-msg-send"),
        BATCH_MSG_SEND_END("batch-msg-send-end"),
        MSG_SEND("msg-send"),
        MSG_CONSUME("msg-consume"),
        MSG_READ("msg-read");

        public final String label;

        OP_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidClientType(String type) {
        return Arrays.stream(OP_TYPES.values()).anyMatch((t) -> t.name().equals(type.toLowerCase()));
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
    public static boolean isValidPersistenceType(String type) {
        return Arrays.stream(PERSISTENT_TYPES.values()).anyMatch((t) -> t.name().equals(type.toLowerCase()));
    }


    ///////
    // Valid Pulsar client configuration (activity-level settings)
    // - https://pulsar.apache.org/docs/en/client-libraries-java/#client
    public enum CLNT_CONF_KEY {
        serviceUrl("serviceUrl"),
        authPulginClassName("authPluginClassName"),
        authParams("AuthParams"),
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
    public static boolean isValidClientConfItem(String item) {
        return Arrays.stream(CLNT_CONF_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
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
    public static boolean isStandardProducerConfItem(String item) {
        return Arrays.stream(PRODUCER_CONF_STD_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
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

    public static boolean isStandardConsumerConfItem(String item) {
        return Arrays.stream(CONSUMER_CONF_STD_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
    }

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
        return Arrays.stream(SUBSCRIPTION_TYPE.values()).anyMatch((t) -> t.name().equals(item));
    }
    public static String getValidSubscriptionTypeList() {
        return Arrays.stream(SUBSCRIPTION_TYPE.values()).map(Object::toString).collect(Collectors.joining(", "));
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
    public static boolean isStandardReaderConfItem(String item) {
        return Arrays.stream(READER_CONF_STD_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
    }

    public enum READER_CONF_CUSTOM_KEY {
        startMessagePos("startMessagePos");

        public final String label;

        READER_CONF_CUSTOM_KEY(String label) {
            this.label = label;
        }
    }

    public static boolean isCustomReaderConfItem(String item) {
        return Arrays.stream(READER_CONF_CUSTOM_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
    }

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
        return Arrays.stream(READER_MSG_POSITION_TYPE.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
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
        boolean isAvroType = false;
        if (typeStr.equalsIgnoreCase("AVRO")) {
            isAvroType = true;
        }
        return isAvroType;
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
                    throw new RuntimeException("Error reading the specified \"Avro\" schema definition file: " + definitionStr);
                }
            }

            SchemaInfo schemaInfo = SchemaInfo.builder()
                .schema(schemaDefinitionStr.getBytes(StandardCharsets.UTF_8))
                .type(SchemaType.AVRO)
                .properties(new HashMap<>())
                //TODO: A unique name for each NB run?
                .name("NBAvro")
                .build();

            schema = new GenericAvroSchema(schemaInfo);
        } else {
            throw new RuntimeException("Trying to create a \"Avro\" schema for a non-Avro schema type string: " + typeStr);
        }

        return schema;
    }

    public static String encode(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : strings) {
            if (!StringUtils.isBlank(str))
                stringBuilder.append(str).append("::");
        }

        String concatenatedStr =
            StringUtils.substringAfterLast(stringBuilder.toString(), "::");

        return Base64.getEncoder().encodeToString(concatenatedStr.getBytes());
    }
}

