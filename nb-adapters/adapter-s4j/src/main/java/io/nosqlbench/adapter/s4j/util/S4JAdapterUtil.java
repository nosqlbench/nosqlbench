package io.nosqlbench.adapter.s4j.util;

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

import com.datastax.oss.pulsar.jms.PulsarJMSConstants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class S4JAdapterUtil {

    private final static Logger logger = LogManager.getLogger(S4JAdapterUtil.class);

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
        // Asynchronous message processing
        ASYNC_API("async_api"),
        // Transaction batch size
        // - Only relevant when session mode is SESSION_TRANSACTED
        TXN_BATCH_NUM("txn_batch_num"),
        // Whether to use blocking message receiving as the default behavior
        BLOCKING_MSG_RECV("blocking_msg_recv"),
        // Whether the destination is a shared topic
        SHARED_TOPIC("shared_topic"),
        // Whether the destination is a durable topic
        DURABLE_TOPIC("durable_topic");

        public final String label;

        DOC_LEVEL_PARAMS(String label) {
            this.label = label;
        }
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

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static boolean isValidStdJmsMsgHeader(String header) {
        return JMS_MSG_HEADER_STD.isValidLabel(header);
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

    public final static String NB_MSG_SEQ_PROP = "NBMsgSeqProp";
    public final static String NB_MSG_SIZE_PROP = "NBMsgSize";

    // JMS Destination Types
    public enum JMS_SESSION_MODES {
        AUTO_ACK("auto_ack"),
        CLIENT_ACK("client_ack"),
        DUPS_OK_ACK("dups_ok_ack"),
        INDIVIDUAL_ACK("individual_ack"),
        TRANSACT("transact_ack");

        public final String label;
        JMS_SESSION_MODES(String label) {
            this.label = label;
        }

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static String getValidJmsSessionModeList() {
        return StringUtils.join(JMS_SESSION_MODES.LABELS, ", ");
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

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static boolean isValidJmsMessageType(String type) {
        return JMS_MESSAGE_TYPES.isValidLabel(type);
    }

    // JMS Message Types
    public enum JMS_MSG_PROP_TYPES {
        SHORT("short"),
        INT("int"),
        LONG("long"),
        FLOAT("float"),
        DOUBLE("double"),
        STRING("string"),
        BOOLEAN("boolean"),
        BYTE("byte");

        public final String label;
        JMS_MSG_PROP_TYPES(String label) {
            this.label = label;
        }

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());

        public static boolean isValidLabel(String label) {
            return LABELS.contains(label);
        }
    }
    public static String getValidJmsMsgPropTypeList() {
        return StringUtils.join(JMS_MESSAGE_TYPES.LABELS, ", ");
    }

    // Message compression types
    public enum MSG_COMPRESSION_TYPE_STR {
        LZ4("LZ4"),
        ZSTD("ZSTD"),
        ZLIB("ZLIB"),
        SNAPPY("SNAPPY");
        public final String label;
        MSG_COMPRESSION_TYPE_STR(String label) {
            // case insensitive
            this.label = StringUtils.upperCase(label);
        }

        private static final Set<String> LABELS = Stream.of(values()).map(v -> v.label)
            .collect(Collectors.toUnmodifiableSet());
        private static boolean isValidLabel(String label) {
            return LABELS.contains(StringUtils.upperCase(label));
        }
    }
    public static String getValidMsgCompressionTypeList() {
        return StringUtils.join(MSG_COMPRESSION_TYPE_STR.LABELS, ", ");
    }
    public static boolean isValidMsgCompressionTypeStr(String type) {
        return MSG_COMPRESSION_TYPE_STR.isValidLabel(type);
    }

    ///////
    // Convert JSON string to a key/value map
    public static Map<String, String> convertJsonToMap(String jsonStr) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonStr, new TypeReference<Map<String, String>>(){});
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

        boolean isTopic = StringUtils.equalsIgnoreCase(destType, JMS_DEST_TYPES.TOPIC.label);

        if (isTopic)
            destName = ((Topic) destination).getTopicName();
        else
            destName = ((Queue) destination).getQueueName();

        return destName;
    }

    ///////
    public static int getSessionModeFromStr(String sessionModeStr) {
        // default ack mode: auto_ack
        int sessionMode = -1;

        if (StringUtils.isBlank(sessionModeStr))
            sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, JMS_SESSION_MODES.AUTO_ACK.label))
            sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, JMS_SESSION_MODES.CLIENT_ACK.label))
            sessionMode = JMSContext.CLIENT_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, JMS_SESSION_MODES.DUPS_OK_ACK.label))
            sessionMode = JMSContext.DUPS_OK_ACKNOWLEDGE;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, JMS_SESSION_MODES.TRANSACT.label))
            sessionMode = JMSContext.SESSION_TRANSACTED;
        else if (StringUtils.equalsIgnoreCase(sessionModeStr, JMS_SESSION_MODES.INDIVIDUAL_ACK.label))
            sessionMode = PulsarJMSConstants.INDIVIDUAL_ACKNOWLEDGE;
        else {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid session mode string \"{}\". Valid values are: {}. Use the default \"auto_ack\" mode!"
                    ,sessionModeStr, getValidJmsSessionModeList());
                sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
            }
        }

        return sessionMode;
    }

    public static boolean isAuthNRequired(S4JClientConf s4jClientConf) {
        assert (s4jClientConf != null);

        boolean required = false;
        Map<String, Object> s4jClientConfObjMap = s4jClientConf.getS4jConfMapObj_client();

        if (s4jClientConfObjMap.containsKey("authPlugin") && s4jClientConfObjMap.containsKey("authParams")) {
            Object authPluginObj = s4jClientConfObjMap.get("authPlugin");
            Object authParamsObj = s4jClientConfObjMap.get("authParams");

            if ( (authPluginObj != null) && StringUtils.isNotBlank(authPluginObj.toString()) &&
                 (authParamsObj != null) && StringUtils.isNotBlank(authParamsObj.toString()) ) {
                required = true;
            }
        }
        return required;
    }


    public static boolean isUseCredentialsEnabled(S4JClientConf s4jClientConf) {
        assert (s4jClientConf != null);

        boolean enabled = false;
        Map<String, Object> s4jConfMap = s4jClientConf.getS4jConfObjMap();

        if (s4jConfMap.containsKey("jms.useCredentialsFromCreateConnection")) {
            enabled = BooleanUtils.toBoolean(s4jConfMap.get("jms.useCredentialsFromCreateConnection").toString());
        }
        return enabled;
    }

    public static String getCredentialUserName(S4JClientConf s4jClientConf) {
        return "dummy";
    }

    public static String getCredentialPassword(S4JClientConf s4jClientConf) {
        Map<String, Object> s4jConfMap = s4jClientConf.getS4jConfObjMap();
        if (s4jConfMap.containsKey("authParams"))
            return s4jConfMap.get("authParams").toString();
        else
            return "";
    }

    ///////
    // Pause the execution of the current thread
    public static void pauseCurThreadExec(int pauseInSec) {
        if (pauseInSec > 0) {
            try {
                Thread.sleep(pauseInSec * 1000L);
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    ///////
    // Error handling for message processing
    public static void processMsgErrorHandling(Exception exception, boolean strictErrorHandling, String errorMsg) {
        exception.printStackTrace();

        if (strictErrorHandling) {
            throw new RuntimeException(errorMsg + " [ " + exception.getMessage() + " ]");
        }
        else {
            S4JAdapterUtil.pauseCurThreadExec(1);
        }
    }
}

