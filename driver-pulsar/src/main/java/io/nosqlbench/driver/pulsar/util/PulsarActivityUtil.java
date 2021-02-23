package io.nosqlbench.driver.pulsar.util;

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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

public class PulsarActivityUtil {

    private final static Logger logger = LogManager.getLogger(PulsarActivityUtil.class);

    // Supported message operation types
    public enum CLIENT_TYPES {
        PRODUCER("producer"),
        CONSUMER("consumer"),
        READER("reader"),
        WSOKT_PRODUCER("websocket-producer"),
        MANAGED_LEDGER("managed-ledger")
        ;

        public final String label;
        private CLIENT_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidClientType(String type) {
        return Arrays.stream(CLIENT_TYPES.values()).anyMatch((t) -> t.name().equals(type.toLowerCase()));
    }


    ///////
    // Valid persistence type
    public enum PERSISTENT_TYPES {
        PERSISTENT("persistent"),
        NON_PERSISTENT("non-persistent")
        ;

        public final String label;
        private PERSISTENT_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidPersistenceType(String type) {
        return Arrays.stream(PERSISTENT_TYPES.values()).anyMatch((t) -> t.name().equals(type.toLowerCase()));
    }


    ///////
    // Valid Pulsar client configuration (activity-level settings)
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
        private CLNT_CONF_KEY(String label) {
            this.label = label;
        }
    }
    public static boolean isValidClientConfItem(String item) {
        return Arrays.stream(CLNT_CONF_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
    }

    ///////
    // Valid producer configuration (activity-level settings)
    public enum PRODUCER_CONF_KEY {
        // NOTE:
        //   For "topicName" and "producerName", they're ignore at activity-level.
        //   Instead, op-level settings are respected
        // topicName("topicName"),
        // producerName("producerName"),
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
        compressionType("compressionType")
        ;

        public final String label;
        private PRODUCER_CONF_KEY(String label) {
            this.label = label;
        }
    }
    public static boolean isValidProducerConfItem(String item) {
        return Arrays.stream(PRODUCER_CONF_KEY.values()).anyMatch((t) -> t.name().equals(item.toLowerCase()));
    }

    ///////
    // Valid consumer configuration (activity-level settings)
    // TODO: to be added
    public enum CONSUMER_CONF_KEY {
        ;

        public final String label;
        private CONSUMER_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid reader configuration (activity-level settings)
    // TODO: to be added
    public enum READER_CONF_KEY {
        ;

        public final String label;
        private READER_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid websocket-producer configuration (activity-level settings)
    // TODO: to be added
    public enum WEBSKT_PRODUCER_CONF_KEY {
        ;

        public final String label;
        private WEBSKT_PRODUCER_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Valid managed-ledger configuration (activity-level settings)
    // TODO: to be added
    public enum MANAGED_LEDGER_CONF_KEY {
        ;

        public final String label;
        private MANAGED_LEDGER_CONF_KEY(String label) {
            this.label = label;
        }
    }

    ///////
    // Primitive Schema type
    public static boolean isPrimitiveSchemaTypeStr(String typeStr) {
        boolean isPrimitive = false;

        // Use "BYTES" as the default type if the type string is not explicitly specified
        if ((typeStr == null) || typeStr.isEmpty()) {
            typeStr = "BYTES";
        }

        if ( typeStr.toUpperCase().equals("BOOLEAN") || typeStr.toUpperCase().equals("INT8") ||
             typeStr.toUpperCase().equals("INT16") || typeStr.toUpperCase().equals("INT32") ||
             typeStr.toUpperCase().equals("INT64") || typeStr.toUpperCase().equals("FLOAT") ||
             typeStr.toUpperCase().equals("DOUBLE") || typeStr.toUpperCase().equals("BYTES") ||
             typeStr.toUpperCase().equals("DATE") || typeStr.toUpperCase().equals("TIME") ||
             typeStr.toUpperCase().equals("TIMESTAMP") || typeStr.toUpperCase().equals("INSTANT") ||
             typeStr.toUpperCase().equals("LOCAL_DATE") || typeStr.toUpperCase().equals("LOCAL_TIME") ||
             typeStr.toUpperCase().equals("LOCAL_DATE_TIME") ) {
            isPrimitive = true;
        }

        return isPrimitive;
    }
    public static Schema getPrimitiveTypeSchema(String typeStr) {
        Schema schema = null;

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
            case "BTYES":
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
        if ( typeStr.toUpperCase().equals("AVRO") ) {
            isAvroType = true;
        }
        return isAvroType;
    }
    public static Schema getAvroSchema(String typeStr, String definitionStr) {
        String schemaDefinitionStr = definitionStr;
        String filePrefix = "file://";
        Schema schema = null;

        // Check if payloadStr points to a file (e.g. "file:///path/to/a/file")
        if (isAvroSchemaTypeStr(typeStr)) {
            if ( (schemaDefinitionStr == null) || schemaDefinitionStr.isEmpty()) {
                throw new RuntimeException("Schema definition must be provided for \"Avro\" schema type!");
            } else if (schemaDefinitionStr.startsWith(filePrefix)) {
                try {
                    Path filePath = Paths.get(URI.create(schemaDefinitionStr));
                    schemaDefinitionStr = Files.readString(filePath, StandardCharsets.US_ASCII);
                } catch (IOException ioe) {
                    throw new RuntimeException("Error reading the specified \"Avro\" schema definition file: " + definitionStr);
                }
            }

            System.out.println(schemaDefinitionStr);

            SchemaInfo schemaInfo = SchemaInfo.builder()
                .schema(schemaDefinitionStr.getBytes(StandardCharsets.UTF_8))
                .type(SchemaType.AVRO)
                .properties(new HashMap<>())
                //TODO: A unique name for each NB run?
                .name("NBAvro")
                .build();

            schema = new GenericAvroSchema(schemaInfo);
        }
        else {
            throw new RuntimeException("Trying to create a \"Avro\" schema for a non-Avro schema type string: " + typeStr);
        }

        return schema;
    }
}

