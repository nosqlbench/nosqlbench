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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class JmsUtil {

    private final static Logger logger = LogManager.getLogger(JmsUtil.class);

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

    /////
    // NB command line parameters
    // - JMS provider type
    public final static String JMS_PROVIDER_TYPE_KEY_STR = "provider_type";

    /// Only applicable when the provider is "Pulsar"
    // - Pulsar configuration properties file
    public final static String JMS_PULSAR_PROVIDER_CFG_FILE_KEY_STR = "pulsar_cfg_file";
    public final static String JMS_PULSAR_PROVIDER_DFT_CFG_FILE_NAME = "pulsar_config.properties";
    // - Pulsar web url
    public final static String JMS_PULSAR_PROVIDER_WEB_URL_KEY_STR = "web_url";
    // - Pulsar service url
    public final static String JMS_PULSAR_PROVIDER_SVC_URL_KEY_STR = "service_url";


    public final static String ASYNC_API_KEY_STR = "async_api";
    public final static String JMS_DESTINATION_TYPE_KEY_STR = "jms_desitation_type";

    ///// JMS Producer
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
    public final static String JMS_PRODUCER_MSG_PROPERTY_KEY_STR = "jms_producer_msg_properties";
    public final static String JMS_PRODUCER_MSG_BODY_KEY_STR = "msg_body";

    ///// JMS Consumer
    public final static String JMS_CONSUMER_DURABLE_KEY_STR = "jms_consumer_msg_durable";
    public final static String JMS_CONSUMER_SHARED_KEY_STR = "jms_consumer_msg_shared";
    public final static String JMS_CONSUMER_MSG_SUBSCRIPTIOn_KEY_STR = "jms_consumer_subscription";
    public final static String JMS_CONSUMER_MSG_READ_SELECTOR_KEY_STR = "jms_consumer_msg_read_selector";
    public final static String JMS_CONSUMER_MSG_NOLOCAL_KEY_STR = "jms_consumer_msg_nolocal";
    public final static String JMS_CONSUMER_READ_TIMEOUT_KEY_STR = "jms_consumer_msg_read_timeout";


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
}

