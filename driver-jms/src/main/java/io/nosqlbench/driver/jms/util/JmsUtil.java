package io.nosqlbench.driver.jms.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Base64;

public class JmsUtil {

    private final static Logger logger = LogManager.getLogger(JmsUtil.class);

    public final static String ASYNC_API_KEY_STR = "async_api";
    public final static String JMS_PROVIDER_TYPE_KEY_STR = "jms_provider_type";
    public final static String JMS_DESTINATION_TYPE_KEY_STR = "jms_desitation_type";

    public final static String JMS_PRODUCER_MSG_PROPERTY_KEY_STR = "jms_producer_msg_properties";

    // Only applicable to Pulsar JMS provider
    public final static String PULSAR_JMS_TOPIC_URI_KEY_STR = "pulsar_topic_uri";

    // Supported message operation types
    public enum OP_TYPES {
        MSG_SEND("msg_send"),
        MSG_READ("msg_read");

        public final String label;
        OP_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidClientType(String type) {
        return Arrays.stream(OP_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }

    // Supported JMS provider type
    public enum JMS_PROVIDER_TYPES {
        PULSAR("pulsar");

        public final String label;
        JMS_PROVIDER_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsProviderType(String type) {
        return Arrays.stream(JMS_PROVIDER_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }

    // JMS Destination Types
    public enum JMS_DESTINATION_TYPES {
        QUEUE("queue"),
        TOPIC("topic");

        public final String label;
        JMS_DESTINATION_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsDestinationType(String type) {
        return Arrays.stream(JMS_DESTINATION_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }

    // Supported JMS provider type
    public enum JMS_MSG_HEADER_KEYS {
        DELIVERY_MODE("jms_producer_header_msg_delivery_mode"),
        PRIORITY("jms_producer_header_msg_priority"),
        TTL("jms_producer_header_msg_ttl"),
        DELIVERY_DELAY("jms_producer_header_msg_delivery_delay"),
        DISABLE_TIMESTAMP("jms_producer_header_disable_msg_timestamp"),
        DISABLE_ID("jms_producer_header_disable_msg_id");

        public final String label;
        JMS_MSG_HEADER_KEYS(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsHeaderKey(String type) {
        return Arrays.stream(JMS_MSG_HEADER_KEYS.values()).anyMatch(t -> t.label.equals(type));
    }

    public static String encode(String... strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : strings) {
            if (!StringUtils.isBlank(str))
                stringBuilder.append(str).append("::");
        }

        String concatenatedStr =
            StringUtils.substringBeforeLast(stringBuilder.toString(), "::");

        return Base64.getEncoder().encodeToString(concatenatedStr.getBytes());
    }
}

