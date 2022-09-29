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
import io.nosqlbench.driver.jms.S4JSpace;
import io.nosqlbench.driver.jms.excption.S4JDriverParamException;
import io.nosqlbench.driver.jms.excption.S4JDriverUnexpectedException;
import io.nosqlbench.driver.jms.util.S4JActivityUtil;
import io.nosqlbench.driver.jms.util.S4JJMSContextWrapper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongFunction;

public class S4JMsgSendMapper extends S4JOpMapper {

    private final static Logger logger = LogManager.getLogger(S4JMsgSendMapper.class);
    private final LongFunction<String> msgHeaderRawJsonStrFunc;
    private final LongFunction<String> msgPropRawJsonStrFunc;
    private final LongFunction<String> msgTypeFunc;
    private final LongFunction<String> msgBodyRawJsonStrFunc;

    private final static String s4jOpType = S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label;

    public S4JMsgSendMapper(S4JSpace s4JSpace,
                            S4JActivity s4JActivity,
                            boolean tempDestBool,
                            LongFunction<String> destTypeStrFunc,
                            LongFunction<String> destNameStrFunc,
                            boolean asyncAPIBool,
                            int txnBatchNum,
                            boolean blockingMsgRecvBool,
                            LongFunction<String> msgHeaderRawJsonStrFunc,
                            LongFunction<String> msgPropRawJsonStrFunc,
                            LongFunction<String> msgTypeFunc,
                            LongFunction<String> msgBodyRawJsonStrFun) {
        super(s4JSpace,
            s4JActivity,
            S4JActivityUtil.MSG_OP_TYPES.MSG_SEND.label,
            tempDestBool,
            destTypeStrFunc,
            destNameStrFunc,
            asyncAPIBool,
            txnBatchNum,
            blockingMsgRecvBool);

        this.msgHeaderRawJsonStrFunc = msgHeaderRawJsonStrFunc;
        this.msgPropRawJsonStrFunc = msgPropRawJsonStrFunc;
        this.msgTypeFunc = msgTypeFunc;
        this.msgBodyRawJsonStrFunc = msgBodyRawJsonStrFun;
    }

    private Message createAndSetMessagePayload(S4JJMSContextWrapper s4JJMSContextWrapper, String msgType, String msgBodyRawJsonStr) throws JMSException {
        Message message;
        int messageSize = 0;

        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        if (StringUtils.equalsIgnoreCase(msgType, S4JActivityUtil.JMS_MESSAGE_TYPES.TEXT.label)) {
            message = jmsContext.createTextMessage();
            ((TextMessage) message).setText(msgBodyRawJsonStr);
            messageSize = msgBodyRawJsonStr.length();
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
                String value = jmsMsgBodyMap.get(key);
                ((MapMessage)message).setString(key, value);
                messageSize += key.length();
                messageSize += value.length();
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
                messageSize += ((String)obj).length();
            }
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JActivityUtil.JMS_MESSAGE_TYPES.OBJECT.label)) {
            message = jmsContext.createObjectMessage();
            ((ObjectMessage) message).setObject(msgBodyRawJsonStr);
            messageSize += msgBodyRawJsonStr.getBytes().length;
        }
        // default: BYTE message type
        else {
            message = jmsContext.createBytesMessage();
            byte[] msgBytePayload =  msgBodyRawJsonStr.getBytes();
            ((BytesMessage)message).writeBytes(msgBytePayload);
            messageSize += msgBytePayload.length;
        }

        message.setStringProperty(S4JActivityUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize));

        return message;
    }

    private Message updateMessageHeaders(S4JJMSContextWrapper s4JJMSContextWrapper, Message message, String msgType, String msgHeaderRawJsonStr) throws JMSException {
        int messageSize = Integer.parseInt(message.getStringProperty(S4JActivityUtil.NB_MSG_SIZE_PROP));

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
                String value = jmsMsgHeaders.get(msgHeaderKey);
                messageSize += msgHeaderKey.length();
                if (value != null) {
                    messageSize += value.length();
                }

                try {
                    if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSType.label)) {
                        outMessage.setJMSType(msgType);
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSPriority.label)) {
                        if (value != null) outMessage.setJMSPriority(Integer.parseInt(value));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSDeliveryMode.label)) {
                        if (value != null) outMessage.setJMSDeliveryMode(Integer.parseInt(value));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSExpiration.label)) {
                        // TODO: convert from a Date/Time string to the required long value
                        if (value != null) outMessage.setJMSExpiration(Long.parseLong(value));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSCorrelationID.label)) {
                        if (value != null) outMessage.setJMSCorrelationID(value);
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JActivityUtil.JMS_MSG_HEADER_STD.JMSReplyTo.label)) {
                        // 'JMSReplyTo' value format: "[topic|queue]:<destination_name>"
                        if (value != null) {
                            String destType = StringUtils.substringBefore(value, ':');
                            String destName = StringUtils.substringAfter(value, ':');
                            outMessage.setJMSReplyTo(s4JSpace.getOrCreateJmsDestination(s4JJMSContextWrapper,false, destType, destName));
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
                        value, msgHeaderKey);
                }
            }
        }

        outMessage.setStringProperty(S4JActivityUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize));

        return outMessage;
    }

    private Message updateMessageProperties(Message message, String msgPropertyRawJsonStr) throws JMSException {
        int messageSize = Integer.parseInt(message.getStringProperty(S4JActivityUtil.NB_MSG_SIZE_PROP));

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

        // Each key in the property json file may include value type information, such as:
        // - key(string): value
        // The above format specifies a message property that has "key" as the property key
        // and "value" as the property value; and the type of the property value is "string"
        //
        // If the value type is not specified, use "string" as the default value type.
        for (Map.Entry<String, String> entry : jmsMsgProperties.entrySet()) {
            String rawKeyStr = entry.getKey();
            String value = entry.getValue();

            if (! StringUtils.isAnyBlank(rawKeyStr, value)) {
                String key = rawKeyStr;
                String valueType = S4JActivityUtil.JMS_MSG_PROP_TYPES.STRING.label;

                if (StringUtils.contains(rawKeyStr, '(')) {
                    key = StringUtils.substringBefore(rawKeyStr, "(").trim();
                    valueType = StringUtils.substringAfter(rawKeyStr, "(");
                    valueType = StringUtils.substringBefore(valueType, ")").trim();
                }

                if (StringUtils.isBlank(valueType)) {
                    message.setStringProperty(entry.getKey(), value);
                }
                else {
                    if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.SHORT.label))
                        message.setShortProperty(key, NumberUtils.toShort(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.INT.label))
                        message.setIntProperty(key, NumberUtils.toInt(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.LONG.label))
                        message.setLongProperty(key, NumberUtils.toLong(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.FLOAT.label))
                        message.setFloatProperty(key, NumberUtils.toFloat(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.DOUBLE.label))
                        message.setDoubleProperty(key, NumberUtils.toDouble(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.BOOLEAN.label))
                        message.setBooleanProperty(key, BooleanUtils.toBoolean(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.STRING.label))
                        message.setStringProperty(key, value);
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JActivityUtil.JMS_MSG_PROP_TYPES.BYTE.label))
                        message.setByteProperty(key, NumberUtils.toByte(value));
                    else
                        throw new S4JDriverParamException(
                            "Unsupported JMS message property value type (\"" + valueType + "\"). " +
                                "Value types are: \"" + S4JActivityUtil.getValidJmsMsgPropTypeList() + "\"");
                }

                messageSize += key.length();
                messageSize += value.length();
            }
        }

        message.setStringProperty(S4JActivityUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize));

        return message;
    }

    @Override
    public S4JOp apply(long value) {
        String destType = destTypeStrFunc.apply(value);
        String destName = destNameStrFunc.apply(value);
        String jmsMsgHeaderRawJsonStr = msgHeaderRawJsonStrFunc.apply(value);
        String jmsMsgPropertyRawJsonStr = msgPropRawJsonStrFunc.apply(value);
        String jmsMsgBodyRawJsonStr = msgBodyRawJsonStrFunc.apply(value);

        if (!S4JActivityUtil.isValidOptypeAndDestTypeCombo(s4jOpType, destType)) {
            throw new S4JDriverParamException("Invalid S4J 'optype' value (\"" + s4jOpType + "\") for destination type (\"" + destType + "\")");
        }

        if (StringUtils.isBlank(jmsMsgBodyRawJsonStr)) {
            throw new S4JDriverParamException("Message payload must be specified and can't be empty!");
        }

        S4JJMSContextWrapper s4JJMSContextWrapper = s4JSpace.getOrCreateS4jJmsContextWrapper(value);
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean commitTransaction = !super.commitTransaction(txnBatchNum, jmsContext.getSessionMode(), value);

        Destination destination;
        try {
            destination = s4JSpace.getOrCreateJmsDestination(s4JJMSContextWrapper, tempDestBool, destType, destName);
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new S4JDriverUnexpectedException("Unable to create the JMS destination!");
        }

        JMSProducer producer;
        try {
            producer = s4JSpace.getOrCreateJmsProducer(s4JJMSContextWrapper, asyncAPIBool);
        }
        catch (JMSException jmsException) {
            throw new S4JDriverUnexpectedException("Unable to create the JMS producer!");
        }

        // Get the right JMS message type
        String jmsMsgType = msgTypeFunc.apply(value);
        if (! S4JActivityUtil.isValidJmsMessageType(jmsMsgType) ) {
            logger.warn(
                "The specified JMS message type {} is not valid, use the default TextMessage type!",
                jmsMsgType);
            jmsMsgType = S4JActivityUtil.JMS_MESSAGE_TYPES.TEXT.label;
        }


        /////////////
        // Set proper message payload based on the message type and the specified input
        // -----------------------
        //
        Message message;
        try {
            message = createAndSetMessagePayload(s4JJMSContextWrapper, jmsMsgType, jmsMsgBodyRawJsonStr);
        }
        catch (JMSException jmsException) {
            throw new RuntimeException("Failed to set create a JMS message and set its payload!");
        }

        /////////////
        // Set standard message headers
        // -----------------------
        //
        try {
            message = updateMessageHeaders(s4JJMSContextWrapper, message, jmsMsgType, jmsMsgHeaderRawJsonStr);
        }
        catch (JMSException jmsException) {
            throw new S4JDriverUnexpectedException("Failed to set create a JMS message and set its payload!");
        }

        /////////////
        // Set defined JMS message properties and other custom properties
        // -----------------------
        //
        try {
            message = updateMessageProperties(message, jmsMsgPropertyRawJsonStr);
            // for testing purpose
            message.setLongProperty(S4JActivityUtil.NB_MSG_SEQ_PROP, value);
        }
        catch (JMSException jmsException) {
            throw new S4JDriverUnexpectedException("Failed to set JMS message properties!");
        }

        return new S4JMsgSendOp(
            value,
            s4JSpace,
            s4JActivity,
            jmsContext,
            destination,
            asyncAPIBool,
            producer,
            message,
            commitTransaction);
    }
}
