package io.nosqlbench.driver.jms.ops;

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

import io.nosqlbench.driver.jms.S4JActivity;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

/**
 * This maps a set of specifier functions to a pulsar operation. The pulsar operation contains
 * enough state to define a pulsar operation such that it can be executed, measured, and possibly
 * retried if needed.
 *
 * This function doesn't act *as* the operation. It merely maps the construction logic into
 * a simple functional type, given the component functions.
 *
 * For additional parameterization, the command template is also provided.
 */
public class S4JMsgSendMapper extends S4JOpMapper {

    private final static Logger logger = LogManager.getLogger(S4JMsgSendMapper.class);
    private final LongFunction<String> msgHeaderRawJsonStrFunc;
    private final LongFunction<String> msgPropRawJsonStrFunc;
    private final LongFunction<String> msgTypeFunc;
    private final LongFunction<String> msgBodyRawJsonStrFunc;
    private final LongFunction<Boolean> reuseProducerBoolFunc;

    private final static String s4jOpType = S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label;
    // TODO: calculate total size of sent messages
    private long totalMsgSize = 0;

    public S4JMsgSendMapper(S4JActivity s4JActivity,
                            LongFunction<Boolean> tempDestBoolFunc,
                            LongFunction<String> destTypeStrFunc,
                            LongFunction<String> destNameStrFunc,
                            LongFunction<Boolean> reuseProducerBoolFunc,
                            LongFunction<Boolean> asyncAPIBoolFunc,
                            LongFunction<String> msgHeaderRawJsonStrFunc,
                            LongFunction<String> msgPropRawJsonStrFunc,
                            LongFunction<String> msgTypeFunc,
                            LongFunction<String> msgBodyRawJsonStrFun) {
        super(s4JActivity,
            S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label,
            tempDestBoolFunc,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBoolFunc);

        this.msgHeaderRawJsonStrFunc = msgHeaderRawJsonStrFunc;
        this.msgPropRawJsonStrFunc = msgPropRawJsonStrFunc;
        this.msgTypeFunc = msgTypeFunc;
        this.msgBodyRawJsonStrFunc = msgBodyRawJsonStrFun;
        this.reuseProducerBoolFunc = reuseProducerBoolFunc;
    }

    private Message createAndSetMessagePayload(String msgType, String msgBodyRawJsonStr) throws JMSException {
        Message message;
        int messageSize = 0;

        if (StringUtils.equalsIgnoreCase(msgType, S4JActivityUtil.JMS_MESSAGE_TYPES.TEXT.label)) {
            message = jmsContext.createTextMessage();
            ((TextMessage) message).setText(msgBodyRawJsonStr);
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JActivityUtil.JMS_MESSAGE_TYPES.MAP.label)) {
            message = jmsContext.createMapMessage();

            // The message body json string must be in the format of a collection of key/value pairs
            // Otherwise, it is an error
            Map<String, String> jmsMsgBodyMap;
            try {
                jmsMsgBodyMap = S4JActivityUtil.convertJsonToMap(msgBodyRawJsonStr);
            } catch (Exception e) {
                throw new RuntimeException("The specified message payload can't be converted to a map when requiring a 'Map' message type!");
            }

            for (String key : jmsMsgBodyMap.keySet()) {
                ((MapMessage)message).setString(key, jmsMsgBodyMap.get(key));
            }
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JActivityUtil.JMS_MESSAGE_TYPES.STREAM.label)) {
            message = jmsContext.createStreamMessage();

            // The message body json string must be in the format of a list of objects
            // Otherwise, it is an error
            List<Object> jmsMsgBodyObjList;
            try {
                jmsMsgBodyObjList = S4JActivityUtil.convertJsonToObjList(msgBodyRawJsonStr);
            } catch (Exception e) {
                throw new RuntimeException("The specified message payload can't be converted to a list of Objects when requiring a 'Stream' message type!");
            }

            for (Object obj : jmsMsgBodyObjList) {
                ((StreamMessage)message).writeObject(obj);
            }
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JActivityUtil.JMS_MESSAGE_TYPES.OBJECT.label)) {
            message = jmsContext.createObjectMessage();
            ((ObjectMessage) message).setObject(msgBodyRawJsonStr);
        }
        // default: BYTE message type
        else {
            message = jmsContext.createBytesMessage();
            ((BytesMessage)message).writeBytes(msgBodyRawJsonStr.getBytes());
        }

        return message;
    }

    private Message updateMessageHeaders(Message message, String msgType, String msgHeaderRawJsonStr) throws JMSException {
        // Check if msgHeaderRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> jmsMsgHeaders = new HashMap<>();
        if (!StringUtils.isBlank(msgHeaderRawJsonStr)) {
            try {
                jmsMsgHeaders = S4JActivityUtil.convertJsonToMap(msgHeaderRawJsonStr);
            } catch (Exception e) {
                logger.warn(
                    "Error parsing message header JSON string {}, ignore message headers!",
                    msgHeaderRawJsonStr);
            }
        }
        // make sure the actual message type is used
        jmsMsgHeaders.put(S4JActivityUtil.JMS_MSG_HEADER_STD.JMSType.label, msgType);

        Message outMessage = message;
        for (String msgHeaderKey:jmsMsgHeaders.keySet()) {
            // Ignore non-standard message headers
            if (S4JActivityUtil.isValidStdJmsMsgHeader(msgHeaderKey)) {
                Object objVal = jmsMsgHeaders.get(msgHeaderKey);

                try {
                    if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSType.label)) {
                        outMessage.setJMSType(msgType);
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSPriority.label)) {
                        if (objVal != null) outMessage.setJMSPriority(Integer.parseInt(objVal.toString()));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSDeliveryMode.label)) {
                        if (objVal != null) outMessage.setJMSDeliveryMode(Integer.parseInt(objVal.toString()));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSExpiration.label)) {
                        // TODO: convert from a Date/Time string to the required long value
                        if (objVal != null) outMessage.setJMSExpiration(Long.parseLong(objVal.toString()));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSCorrelationID.label)) {
                        if (objVal != null) outMessage.setJMSCorrelationID(objVal.toString());
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSReplyTo.label)) {
                        // 'JMSReplyTo' value format: "[topic|queue]:<destination_name>"
                        if (objVal != null) {
                            String destType = StringUtils.substringBefore(objVal.toString(), ':');
                            String destName = StringUtils.substringAfter(objVal.toString(), ':');
                            outMessage.setJMSReplyTo(s4JActivity.getOrCreateJmsDestination(false, destType, destName));
                        }
                    }
                    // Ignore these headers - handled by S4J API automatically
                    /* else if (StringUtils.equalsAnyIgnoreCase(msgHeaderKey,
                        S4JActivityUtil.JMS_MSG_HEADER_STD.JMSDestination.label,
                        S4JActivityUtil.JMS_MSG_HEADER_STD.JMSMessageID.label,
                        S4JActivityUtil.JMS_MSG_HEADER_STD.JMSTimestamp.label,
                        S4JActivityUtil.JMS_MSG_HEADER_STD.JMSRedelivered.label
                        )) {
                    }*/
                } catch (NumberFormatException nfe) {
                    logger.warn("Incorrect value format ('{}') for the message header field ('{}')!",
                        objVal.toString(), msgHeaderKey);
                }
            }
        }

        return outMessage;
    }

    private Message updateMessageProperties(Message message, String msgPropertyRawJsonStr) throws JMSException {
        // Check if jmsMsgPropertyRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> jmsMsgProperties = new HashMap<>();
        if (!StringUtils.isBlank(msgPropertyRawJsonStr)) {
            try {
                jmsMsgProperties = S4JActivityUtil.convertJsonToMap(msgPropertyRawJsonStr);
            } catch (Exception e) {
                logger.warn(
                    "Error parsing message property JSON string {}, ignore message properties!",
                    msgPropertyRawJsonStr);
            }
        }

        Message outMessage = message;
        for (Map.Entry<String, String> entry : jmsMsgProperties.entrySet()) {
            outMessage.setObjectProperty(entry.getKey(), entry.getValue());
        }

        return outMessage;
    }

    @Override
    public S4JOp apply(long value) {
        boolean tempDest = tempDestBoolFunc.apply(value);
        String destType = destTypeStrFunc.apply(value);
        String destName = destNameStrFunc.apply(value);
        String jmsMsgHeaderRawJsonStr = msgHeaderRawJsonStrFunc.apply(value);
        String jmsMsgPropertyRawJsonStr = msgPropRawJsonStrFunc.apply(value);
        String jmsMsgBodyRawJsonStr = msgBodyRawJsonStrFunc.apply(value);

        boolean reuseProducer = reuseProducerBoolFunc.apply(value);
        boolean asyncApi = asyncAPIBoolFunc.apply(value);

        if (!S4JActivityUtil.isValidOptypeAndDestTypeCombo(s4jOpType, destType)) {
            throw new RuntimeException("Invalid S4J 'optype' value (\"" + s4jOpType + "\") for destination type (\"" + destType + "\")");
        }

        Destination destination;
        try {
            destination = s4JActivity.getOrCreateJmsDestination(tempDest, destType, destName);
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new RuntimeException("Unable to create the JMS destination!");
        }

        JMSProducer producer;
        try {
            producer = s4JActivity.getOrCreateJmsProducer(destination, destType, reuseProducer, asyncApi);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Unable to create the JMS producer!");
        }

        // Get the right JMS message type
        String jmsMsgType = msgTypeFunc.apply(value);
        if (! S4JActivityUtil.isValidJmsMessageType(jmsMsgType) ) {
            logger.warn(
                "The specified JMS message type {} is not valid, use the default TextMessage type!",
                jmsMsgType);
            jmsMsgType = S4JActivityUtil.JMS_MESSAGE_TYPES.TEXT.label;
        }
        if (StringUtils.isBlank(jmsMsgBodyRawJsonStr)) {
            throw new RuntimeException("Message payload must be specified and can't be empty!");
        }

        /////////////
        // Set proper message payload based on the message type and the specified input
        // -----------------------
        //
        Message message;
        try {
            message = createAndSetMessagePayload(jmsMsgType, jmsMsgBodyRawJsonStr);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Failed to set create a JMS message and set its payload!");
        }

        /////////////
        // Set standard message headers
        // -----------------------
        //
        try {
            message = updateMessageHeaders(message, jmsMsgType, jmsMsgHeaderRawJsonStr);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Failed to set create a JMS message and set its payload!");
        }

        /////////////
        // Set defined JMS message properties and other custom properties
        // -----------------------
        //
        try {
            message = updateMessageProperties(message, jmsMsgPropertyRawJsonStr);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Failed to set JMS message properties!");
        }

        return new S4JMsgSendOp(
            s4JActivity,
            destination,
            asyncApi,
            producer,
            message);
    }
}
