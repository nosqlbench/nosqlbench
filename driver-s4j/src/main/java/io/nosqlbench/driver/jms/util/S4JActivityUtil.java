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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class S4JActivityUtil {

    private final static Logger logger = LogManager.getLogger(S4JActivityUtil.class);

    ///////
    // Valid document level parameters for JMS NB yaml file
    public final static String JMS_SPEC_VER_12 = "1.2";
    public final static String JMS_SPEC_VER_20 = "2.0";
    public enum DOC_LEVEL_PARAMS {

        // Temporary destination
        TEMP_DEST("temporary_dest"),
        // JMS destination type - topic or queue
        // String value
        // - valid values: see JMS_DEST_TYPES
        DEST_TYPE("dest_type"),
        // JMS destination name
        // String value
        DEST_NAME("dest_name"),
        // Reuse JMS producer across destinations
        REUSE_PRODUCER("reuse_producer"),
        // Asynchronous message processing
        ASYNC_API("async_api"),
        // Transaction batch size
        // - Only relevant when session mode is SESSION_TRANSACTED
        // - TODO: NOT implemented yet
        TRANSACT_BATCH_SIZE("trans_batch_size");

        public final String label;

        DOC_LEVEL_PARAMS(String label) {
            this.label = label;
        }
    }
    public static boolean isValidDocLevelParam(String param) {
        return Arrays.stream(DOC_LEVEL_PARAMS.values()).anyMatch(t -> t.label.equals(param));
    }
    public static String getValidDocLevelParamList() {
        return Arrays.stream(DOC_LEVEL_PARAMS.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }


    // Supported message operation types
    public enum MSG_OP_TYPES {
        // Queue and Topic
        // - MessageProducer, TopicPublisher, QueueSender
        MSG_SEND("msg_send"),
        // Queue and Topic
        // - MessageConsumer, TopicSubscriber, QueueReceiver
        MSG_READ("msg_read"),
        // Topic only
        MSG_READ_SHARED("msg_read_shared"),
        MSG_READ_DURABLE("msg_read_durable"),
        MSG_READ_SHARED_DURABLE("msg_Read_shared_durable"),
        // Queue only
        // - QueueBrowser
        MSG_BROWSE("msg_browse");

        public final String label;
        MSG_OP_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidMsgOpType(String type) {
        return Arrays.stream(MSG_OP_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }
    public static String getValidMsgOpTypeList() {
        return Arrays.stream(MSG_OP_TYPES.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    // JMS Destination Types
    public enum JMS_DEST_TYPES {
        QUEUE("queue"),
        TOPIC("topic");

        public final String label;
        JMS_DEST_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsDestType(String type) {
        return Arrays.stream(JMS_DEST_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }
    public static String getValidJmsDestTypeList() {
        return Arrays.stream(JMS_DEST_TYPES.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    // Standard JMS message headers (by JMS specification)
    public enum JMS_MSG_HEADER_STD {
        JMSDestination("JMSDestination"),
        JMSDeliveryMode("JMSDeliveryMode"),
        JMSMessageID("JMSMessageID"),
        JMSTimestamp("JMSTimestamp"),
        JMSRedelivered("JMSRedelivered"),
        JMSExpiration("JMSExpiration"),
        JMSCorrelationID("JMSCorrelationID"),
        JMSType("JMSType"),
        JMSReplyTo("JMSReplyTo"),
        JMSPriority("JMSPriority");

        public final String label;
        JMS_MSG_HEADER_STD(String label) {
            this.label = label;
        }
    }
    public static boolean isValidStdJmsMsgHeader(String header) {
        return Arrays.stream(JMS_MSG_HEADER_STD.values()).anyMatch(t -> t.label.equals(header));
    }
    public static String getValidStdJmsMsgHeaderList() {
        return Arrays.stream(JMS_MSG_HEADER_STD.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    // JMS defined message properties (by JMS specification)
    public enum JMS_DEFINED_MSG_PROPERTY {
        JMSDestination("JMSDestination"),
        JMSDeliveryMode("JMSDeliveryMode"),
        JMSMessageID("JMSMessageID"),
        JMSTimestamp("JMSTimestamp"),
        JMSRedelivered("JMSRedelivered"),
        JMSExpiration("JMSExpiration"),
        JMSCorrelationID("JMSCorrelationID"),
        JMSType("JMSType"),
        JMSReplyTo("JMSReplyTo"),
        JMSPriority("JMSPriority");

        public final String label;
        JMS_DEFINED_MSG_PROPERTY(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsDfndMsgProp(String property) {
        return Arrays.stream(JMS_DEFINED_MSG_PROPERTY.values()).anyMatch(t -> t.label.equals(property));
    }
    public static String getValidJmsDfndMsgPropList() {
        return Arrays.stream(JMS_DEFINED_MSG_PROPERTY.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    // JMS Destination Types
    public enum JMS_SESSION_MODES {
        AUTO_ACK("auto_ack"),
        CLIENT_ACK("client_ack"),
        DUPS_OK_ACK("dups_ok_ack"),
        TRANSACT("transact_ack");

        public final String label;
        JMS_SESSION_MODES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsSessionMode(String mode) {
        return Arrays.stream(JMS_SESSION_MODES.values()).anyMatch(t -> t.label.equals(mode));
    }
    public static String getValidJmsSessionModeList() {
        return Arrays.stream(JMS_SESSION_MODES.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    // JMS Message Types
    public enum JMS_MESSAGE_TYPES {
        TEXT("text"),
        BYTE("byte"),
        MAP("map"),
        STREAM("stream"),
        OBJECT("object");

        public final String label;
        JMS_MESSAGE_TYPES(String label) {
            this.label = label;
        }
    }
    public static boolean isValidJmsMessageType(String type) {
        return Arrays.stream(JMS_MESSAGE_TYPES.values()).anyMatch(t -> t.label.equals(type));
    }
    public static String getValidJmsMessageTypeList() {
        return Arrays.stream(JMS_MESSAGE_TYPES.values()).map(t -> t.label).collect(Collectors.joining(", "));
    }

    ///////
    // Check it is valid "dest_type" and "optype" combination:
    // > optype: msg_send and msg_read  --> applicable to both Topic and Queue
    // > optype: msg_read_shared, msg_read_durable, msg_read_shred_durable  --> applicable to Topic only
    // > optype: msg_browse  --> applicable to Queue only
    public static boolean isValidOptypeAndDestTypeCombo(String opType, String destType) {
        boolean validCombo = StringUtils.equalsAnyIgnoreCase(opType,
            MSG_OP_TYPES.MSG_SEND.label, MSG_OP_TYPES.MSG_READ.label);

        if (StringUtils.equalsIgnoreCase(destType, JMS_DEST_TYPES.TOPIC.label)) {
            validCombo = validCombo ||
                StringUtils.equalsAnyIgnoreCase(opType,
                    MSG_OP_TYPES.MSG_READ_DURABLE.label,
                    MSG_OP_TYPES.MSG_READ_SHARED.label,
                    MSG_OP_TYPES.MSG_READ_SHARED_DURABLE.label );
        } else if (StringUtils.equalsIgnoreCase(destType, JMS_DEST_TYPES.TOPIC.label)) {
            validCombo = validCombo ||
                StringUtils.equalsAnyIgnoreCase(opType,
                    MSG_OP_TYPES.MSG_BROWSE.label);
        }

        return validCombo;
    }

    ///////
    // Convert JSON string to a key/value map
    public static Map<String, String> convertJsonToMap(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, Map.class);
    }

    ///////
    // Convert JSON string to a list of objects
    public static List<Object> convertJsonToObjList(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return Arrays.asList(mapper.readValue(jsonStr, Object[].class));
    }

    ///////
    // Get the destination name from the Destination object
    public static String getDestinationName(Destination destination, String destType) throws JMSException {
        String destName;

        boolean isTopic = StringUtils.equalsIgnoreCase(destType, S4JActivityUtil.JMS_DEST_TYPES.TOPIC.label);

        if (isTopic)
            destName = ((Topic) destination).getTopicName();
        else
            destName = ((Queue) destination).getQueueName();

        return destName;
    }

    ///////
    // Get the destination name from the Destination object
    public static String getMsgReadOpType(boolean durable, boolean shared) {

        if (!durable && !shared)
            return MSG_OP_TYPES.MSG_READ.label;
        else if (durable && !shared)
            return MSG_OP_TYPES.MSG_READ_DURABLE.label;
        else if (!durable)
            return MSG_OP_TYPES.MSG_READ_SHARED.label;
        else
            return MSG_OP_TYPES.MSG_READ_SHARED_DURABLE.label;
    }

    ///////
    // Calculate a unique cache key from a series of input parameters
    public static String buildCacheKey(String... keyParts) {
        return String.join("::", keyParts);
    }
}

