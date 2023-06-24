/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.s4j.dispensers;

import io.nosqlbench.adapter.s4j.S4JSpace;
import io.nosqlbench.adapter.s4j.exception.S4JAdapterInvalidParamException;
import io.nosqlbench.adapter.s4j.exception.S4JAdapterUnexpectedException;
import io.nosqlbench.adapter.s4j.ops.MessageProducerOp;
import io.nosqlbench.adapter.s4j.util.S4JAdapterUtil;
import io.nosqlbench.adapter.s4j.util.S4JJMSContextWrapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.templating.ParsedOp;
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
public class MessageProducerOpDispenser extends S4JBaseOpDispenser {

    private final static Logger logger = LogManager.getLogger("MessageProducerOpDispenser");

    public static final String MSG_HEADER_OP_PARAM = "msg_header";
    public static final String MSG_PROP_OP_PARAM = "msg_property";
    public static final String MSG_BODY_OP_PARAM = "msg_body";
    public static final String MSG_TYPE_OP_PARAM = "msg_type";

    private final LongFunction<String> msgHeaderRawJsonStrFunc;
    private final LongFunction<String> msgPropRawJsonStrFunc;
    private final LongFunction<String> msgBodyRawJsonStrFunc;
    private final LongFunction<String> msgTypeFunc;

    public MessageProducerOpDispenser(DriverAdapter adapter,
                                      ParsedOp op,
                                      LongFunction<String> tgtNameFunc,
                                      S4JSpace s4jSpace) {
        super(adapter, op, tgtNameFunc, s4jSpace);

        this.msgHeaderRawJsonStrFunc = lookupOptionalStrOpValueFunc(MSG_HEADER_OP_PARAM);
        this.msgPropRawJsonStrFunc = lookupOptionalStrOpValueFunc(MSG_PROP_OP_PARAM);
        this.msgBodyRawJsonStrFunc = lookupMandtoryStrOpValueFunc(MSG_BODY_OP_PARAM);
        this.msgTypeFunc = lookupOptionalStrOpValueFunc(MSG_TYPE_OP_PARAM);
    }

    private Message createAndSetMessagePayload(
        S4JJMSContextWrapper s4JJMSContextWrapper,
        String msgType, String msgBodyRawJsonStr) throws JMSException
    {
        Message message;
        int messageSize = 0;

        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();

        if (StringUtils.equalsIgnoreCase(msgType, S4JAdapterUtil.JMS_MESSAGE_TYPES.TEXT.label)) {
            message = jmsContext.createTextMessage();
            ((TextMessage) message).setText(msgBodyRawJsonStr);
            messageSize = msgBodyRawJsonStr.length();
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JAdapterUtil.JMS_MESSAGE_TYPES.MAP.label)) {
            message = jmsContext.createMapMessage();

            // The message body json string must be in the format of a collection of key/value pairs
            // Otherwise, it is an error
            Map<String, String> jmsMsgBodyMap;
            try {
                jmsMsgBodyMap = S4JAdapterUtil.convertJsonToMap(msgBodyRawJsonStr);
            } catch (Exception e) {
                throw new RuntimeException("The specified message payload can't be converted to a map when requiring a 'Map' message type!");
            }

            for (String key : jmsMsgBodyMap.keySet()) {
                String value = jmsMsgBodyMap.get(key);
                ((MapMessage)message).setString(key, value);
                messageSize += key.length();
                messageSize += value.length();
            }
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JAdapterUtil.JMS_MESSAGE_TYPES.STREAM.label)) {
            message = jmsContext.createStreamMessage();

            // The message body json string must be in the format of a list of objects
            // Otherwise, it is an error
            List<Object> jmsMsgBodyObjList;
            try {
                jmsMsgBodyObjList = S4JAdapterUtil.convertJsonToObjList(msgBodyRawJsonStr);
            } catch (Exception e) {
                throw new RuntimeException("The specified message payload can't be converted to a list of Objects when requiring a 'Stream' message type!");
            }

            for (Object obj : jmsMsgBodyObjList) {
                ((StreamMessage)message).writeObject(obj);
                messageSize += ((String)obj).length();
            }
        } else if (StringUtils.equalsIgnoreCase(msgType, S4JAdapterUtil.JMS_MESSAGE_TYPES.OBJECT.label)) {
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

        message.setStringProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize));

        return message;
    }

    private Message updateMessageHeaders(S4JJMSContextWrapper s4JJMSContextWrapper, Message message, String msgType, String msgHeaderRawJsonStr) throws JMSException {
        int messageSize = Integer.parseInt(message.getStringProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP));

        // Check if msgHeaderRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> jmsMsgHeaders = new HashMap<>();
        if (!StringUtils.isBlank(msgHeaderRawJsonStr)) {
            try {
                jmsMsgHeaders = S4JAdapterUtil.convertJsonToMap(msgHeaderRawJsonStr);
            } catch (Exception e) {
                logger.warn(
                    "Error parsing message header JSON string {}, ignore message headers!",
                    msgHeaderRawJsonStr);
            }
        }
        // make sure the actual message type is used
        jmsMsgHeaders.put(S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSType.label, msgType);

        Message outMessage = message;
        for (String msgHeaderKey:jmsMsgHeaders.keySet()) {
            // Ignore non-standard message headers
            if (S4JAdapterUtil.isValidStdJmsMsgHeader(msgHeaderKey)) {
                String value = jmsMsgHeaders.get(msgHeaderKey);
                messageSize += msgHeaderKey.length();
                if (value != null) {
                    messageSize += value.length();
                }

                try {
                    if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSType.label)) {
                        outMessage.setJMSType(msgType);
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSPriority.label)) {
                        if (value != null) outMessage.setJMSPriority(Integer.parseInt(value));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSDeliveryMode.label)) {
                        if (value != null) outMessage.setJMSDeliveryMode(Integer.parseInt(value));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSExpiration.label)) {
                        // TODO: convert from a Date/Time string to the required long value
                        if (value != null) outMessage.setJMSExpiration(Long.parseLong(value));
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSCorrelationID.label)) {
                        if (value != null) outMessage.setJMSCorrelationID(value);
                    } else if (StringUtils.equalsIgnoreCase(msgHeaderKey, S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSReplyTo.label)) {
                        // 'JMSReplyTo' value format: "[topic|queue]:<destination_name>"
                        if (value != null) {
                            String destType = StringUtils.substringBefore(value, ':');
                            String destName = StringUtils.substringAfter(value, ':');
                            outMessage.setJMSReplyTo(getJmsDestination(s4JJMSContextWrapper,false, destType, destName));
                        }
                    }
                    // Ignore these headers - handled by S4J API automatically
                    /* else if (StringUtils.equalsAnyIgnoreCase(msgHeaderKey,
                        S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSDestination.label,
                        S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSMessageID.label,
                        S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSTimestamp.label,
                        S4JAdapterUtil.JMS_MSG_HEADER_STD.JMSRedelivered.label
                        )) {
                    }*/
                } catch (NumberFormatException nfe) {
                    logger.warn("Incorrect value format ('{}') for the message header field ('{}')!",
                        value, msgHeaderKey);
                }
            }
        }

        outMessage.setStringProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize));

        return outMessage;
    }

    private Message updateMessageProperties(Message message, String msgPropertyRawJsonStr) throws JMSException {
        int messageSize = Integer.parseInt(message.getStringProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP));

        // Check if jmsMsgPropertyRawJsonStr is a valid JSON string with a collection of key/value pairs
        // - if Yes, convert it to a map
        // - otherwise, log an error message and ignore message headers without throwing a runtime exception
        Map<String, String> jmsMsgProperties = new HashMap<>();
        if (!StringUtils.isBlank(msgPropertyRawJsonStr)) {
            try {
                jmsMsgProperties = S4JAdapterUtil.convertJsonToMap(msgPropertyRawJsonStr);
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
                String valueType = S4JAdapterUtil.JMS_MSG_PROP_TYPES.STRING.label;

                if (StringUtils.contains(rawKeyStr, '(')) {
                    key = StringUtils.substringBefore(rawKeyStr, "(").trim();
                    valueType = StringUtils.substringAfter(rawKeyStr, "(");
                    valueType = StringUtils.substringBefore(valueType, ")").trim();
                }

                if (StringUtils.isBlank(valueType)) {
                    message.setStringProperty(entry.getKey(), value);
                }
                else {
                    if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.SHORT.label))
                        message.setShortProperty(key, NumberUtils.toShort(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.INT.label))
                        message.setIntProperty(key, NumberUtils.toInt(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.LONG.label))
                        message.setLongProperty(key, NumberUtils.toLong(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.FLOAT.label))
                        message.setFloatProperty(key, NumberUtils.toFloat(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.DOUBLE.label))
                        message.setDoubleProperty(key, NumberUtils.toDouble(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.BOOLEAN.label))
                        message.setBooleanProperty(key, BooleanUtils.toBoolean(value));
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.STRING.label))
                        message.setStringProperty(key, value);
                    else if (StringUtils.equalsIgnoreCase(valueType, S4JAdapterUtil.JMS_MSG_PROP_TYPES.BYTE.label))
                        message.setByteProperty(key, NumberUtils.toByte(value));
                    else
                        throw new S4JAdapterInvalidParamException(
                            "Unsupported JMS message property value type (\"" + valueType + "\"). " +
                                "Value types are: \"" + S4JAdapterUtil.getValidJmsMsgPropTypeList() + "\"");
                }

                messageSize += key.length();
                messageSize += value.length();
            }
        }

        message.setStringProperty(S4JAdapterUtil.NB_MSG_SIZE_PROP, String.valueOf(messageSize));

        return message;
    }

    @Override
    public MessageProducerOp apply(long cycle) {
        String destName = destNameStrFunc.apply(cycle);
        String jmsMsgHeaderRawJsonStr = msgHeaderRawJsonStrFunc.apply(cycle);
        String jmsMsgPropertyRawJsonStr = msgPropRawJsonStrFunc.apply(cycle);
        String jmsMsgBodyRawJsonStr = msgBodyRawJsonStrFunc.apply(cycle);

        if (StringUtils.isBlank(jmsMsgBodyRawJsonStr)) {
            throw new S4JAdapterInvalidParamException("Message payload must be specified and can't be empty!");
        }

        S4JJMSContextWrapper s4JJMSContextWrapper = getS4jJmsContextWrapper(cycle);
        JMSContext jmsContext = s4JJMSContextWrapper.getJmsContext();
        boolean commitTransaction = super.commitTransaction(txnBatchNum, jmsContext.getSessionMode(), cycle);

        Destination destination;
        try {
            destination = getJmsDestination(s4JJMSContextWrapper, temporaryDest, destType, destName);
        }
        catch (JMSRuntimeException jmsRuntimeException) {
            throw new S4JAdapterUnexpectedException("Unable to create the JMS destination!");
        }

        JMSProducer producer;
        try {
            producer = getJmsProducer(s4JJMSContextWrapper, asyncAPI);
        }
        catch (JMSException jmsException) {
            throw new S4JAdapterUnexpectedException("Unable to create the JMS producer!");
        }

        // Get the right JMS message type
        String jmsMsgType = msgTypeFunc.apply(cycle);
        if (! S4JAdapterUtil.isValidJmsMessageType(jmsMsgType) ) {
            logger.warn(
                "The specified JMS message type {} is not valid, use the default TextMessage type!",
                jmsMsgType);
            jmsMsgType = S4JAdapterUtil.JMS_MESSAGE_TYPES.BYTE.label;
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
            throw new S4JAdapterUnexpectedException("Failed to set create a JMS message and set its payload!");
        }

        /////////////
        // Set defined JMS message properties and other custom properties
        // -----------------------
        //
        try {
            message = updateMessageProperties(message, jmsMsgPropertyRawJsonStr);
            // for testing purpose
            message.setLongProperty(S4JAdapterUtil.NB_MSG_SEQ_PROP, cycle);
        }
        catch (JMSException jmsException) {
            throw new S4JAdapterUnexpectedException("Failed to set JMS message properties!");
        }

        return new MessageProducerOp(
            s4jAdapterMetrics,
            s4jSpace,
            jmsContext,
            destination,
            asyncAPI,
            commitTransaction,
            producer,
            message);
    }
}
