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

package io.nosqlbench.driver.jms;

import io.nosqlbench.driver.jms.ops.JmsMsgReadMapper;
import io.nosqlbench.driver.jms.ops.JmsMsgSendMapper;
import io.nosqlbench.driver.jms.ops.JmsOp;
import io.nosqlbench.driver.jms.util.JmsHeaderLongFunc;
import io.nosqlbench.driver.jms.util.JmsUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongFunction;
import java.util.stream.Collectors;

public class ReadyPulsarJmsOp extends ReadyJmsOp {

    public ReadyPulsarJmsOp(OpTemplate opTemplate, JmsActivity jmsActivity) {
        super(opTemplate, jmsActivity);
    }

    public LongFunction<JmsOp> resolveJms() {
        // Global/Doc-level parameter: topic_uri
        LongFunction<String> topicUriFunc = (l) -> null;
        if (cmdTpl.containsKey(JmsUtil.PULSAR_JMS_TOPIC_URI_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.PULSAR_JMS_TOPIC_URI_KEY_STR)) {
                topicUriFunc = (l) -> cmdTpl.getStatic(JmsUtil.PULSAR_JMS_TOPIC_URI_KEY_STR);
            } else {
                topicUriFunc = (l) -> cmdTpl.getDynamic(JmsUtil.PULSAR_JMS_TOPIC_URI_KEY_STR, l);
            }
        }

        // Global: JMS destination
        LongFunction<Destination> jmsDestinationFunc;
        try {
            LongFunction<String> finalTopicUriFunc = topicUriFunc;
            jmsDestinationFunc = (l) -> jmsActivity.getOrCreateJmsDestination(
                jmsDestinationTypeFunc.apply(l),
                finalTopicUriFunc.apply(l));
        }
        catch (JMSRuntimeException ex) {
            throw new RuntimeException("Unable to create JMS destination!");
        }

        if (StringUtils.equalsIgnoreCase(stmtOpType, JmsUtil.OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(asyncApiFunc, jmsDestinationFunc);
        } else if (StringUtils.equalsIgnoreCase(stmtOpType, JmsUtil.OP_TYPES.MSG_READ.label)) {
            return resolveMsgRead(asyncApiFunc, jmsDestinationFunc);
        }  else {
            throw new RuntimeException("Unsupported JMS operation type");
        }
    }

    private LongFunction<JmsOp> resolveMsgSend(
        LongFunction<Boolean> async_api_func,
        LongFunction<Destination> jmsDestinationFunc
    ) {
        JmsHeaderLongFunc jmsHeaderLongFunc = new JmsHeaderLongFunc();

        // JMS header: delivery mode
        LongFunction<Integer> msgDeliveryModeFunc = (l) -> DeliveryMode.PERSISTENT;
        if (cmdTpl.containsKey(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_MODE.label)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_MODE.label)) {
                msgDeliveryModeFunc = (l) -> NumberUtils.toInt(cmdTpl.getStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_MODE.label));
            }
            else {
                msgDeliveryModeFunc = (l) -> NumberUtils.toInt(cmdTpl.getDynamic(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_MODE.label, l));
            }
        }
        jmsHeaderLongFunc.setDeliveryModeFunc(msgDeliveryModeFunc);

        // JMS header: message priority
        LongFunction<Integer> msgPriorityFunc = (l) -> Message.DEFAULT_PRIORITY;
        if (cmdTpl.containsKey(JmsUtil.JMS_MSG_HEADER_KEYS.PRIORITY.label)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_MSG_HEADER_KEYS.PRIORITY.label)) {
                msgPriorityFunc = (l) -> NumberUtils.toInt(cmdTpl.getStatic(JmsUtil.JMS_MSG_HEADER_KEYS.PRIORITY.label));
            }
            else {
                msgPriorityFunc = (l) -> NumberUtils.toInt(cmdTpl.getDynamic(JmsUtil.JMS_MSG_HEADER_KEYS.PRIORITY.label, l));
            }
        }
        jmsHeaderLongFunc.setMsgPriorityFunc(msgPriorityFunc);

        // JMS header: message TTL
        LongFunction<Long> msgTtlFunc = (l) -> Message.DEFAULT_TIME_TO_LIVE;
        if (cmdTpl.containsKey(JmsUtil.JMS_MSG_HEADER_KEYS.TTL.label)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_MSG_HEADER_KEYS.TTL.label)) {
                msgTtlFunc = (l) -> NumberUtils.toLong(cmdTpl.getStatic(JmsUtil.JMS_MSG_HEADER_KEYS.TTL.label));
            }
            else {
                msgTtlFunc = (l) -> NumberUtils.toLong(cmdTpl.getDynamic(JmsUtil.JMS_MSG_HEADER_KEYS.TTL.label, l));
            }
        }
        jmsHeaderLongFunc.setMsgTtlFunc(msgTtlFunc);

        // JMS header: message delivery delay
        LongFunction<Long> msgDeliveryDelayFunc = (l) -> Message.DEFAULT_DELIVERY_DELAY;
        if (cmdTpl.containsKey(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_DELAY.label)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_DELAY.label)) {
                msgDeliveryDelayFunc = (l) -> NumberUtils.toLong(cmdTpl.getStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_DELAY.label));
            }
            else {
                msgDeliveryDelayFunc = (l) -> NumberUtils.toLong(cmdTpl.getDynamic(JmsUtil.JMS_MSG_HEADER_KEYS.DELIVERY_DELAY.label, l));
            }
        }
        jmsHeaderLongFunc.setMsgDeliveryDelayFunc(msgDeliveryDelayFunc);

        // JMS header: disable message timestamp
        LongFunction<Boolean> disableMsgTimestampFunc = (l) -> false;
        if (cmdTpl.containsKey(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_TIMESTAMP.label)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_TIMESTAMP.label)) {
                disableMsgTimestampFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_TIMESTAMP.label));
            }
            else {
                disableMsgTimestampFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getDynamic(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_TIMESTAMP.label, l));
            }
        }
        jmsHeaderLongFunc.setDisableMsgTimestampFunc(disableMsgTimestampFunc);

        // JMS header: disable message ID
        LongFunction<Boolean> disableMsgIdFunc = (l) -> false;
        if (cmdTpl.containsKey(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_ID.label)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_ID.label)) {
                disableMsgIdFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_ID.label));
            }
            else {
                disableMsgIdFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getDynamic(JmsUtil.JMS_MSG_HEADER_KEYS.DISABLE_ID.label, l));
            }
        }
        jmsHeaderLongFunc.setDisableMsgIdFunc(disableMsgIdFunc);

        // JMS message properties
        String jmsMsgPropertyListStr = "";
        if (cmdTpl.containsKey(JmsUtil.JMS_PRODUCER_MSG_PROPERTY_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_PRODUCER_MSG_PROPERTY_KEY_STR)) {
                jmsMsgPropertyListStr = cmdTpl.getStatic(JmsUtil.JMS_PRODUCER_MSG_PROPERTY_KEY_STR);
            } else {
                throw new RuntimeException("\"" + JmsUtil.JMS_PRODUCER_MSG_PROPERTY_KEY_STR + "\" parameter cannot be dynamic!");
            }
        }

        Map<String, Object> jmsMsgProperties =  new HashMap<>();
        if ( !StringUtils.isEmpty(jmsMsgPropertyListStr) ) {
            jmsMsgProperties = Arrays.stream(jmsMsgPropertyListStr.split(";"))
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
        }

        LongFunction<String> msgBodyFunc;
        if (cmdTpl.containsKey(JmsUtil.JMS_PRODUCER_MSG_BODY_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_PRODUCER_MSG_BODY_KEY_STR)) {
                msgBodyFunc = (l) -> cmdTpl.getStatic(JmsUtil.JMS_PRODUCER_MSG_BODY_KEY_STR);
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_PRODUCER_MSG_BODY_KEY_STR)) {
                msgBodyFunc = (l) -> cmdTpl.getDynamic(JmsUtil.JMS_PRODUCER_MSG_BODY_KEY_STR, l);
            } else {
                msgBodyFunc = (l) -> null;
            }
        } else {
            throw new RuntimeException("JMS message send:: \"msg_body\" field must be specified!");
        }

        return new JmsMsgSendMapper(
            jmsActivity,
            async_api_func,
            jmsDestinationFunc,
            jmsHeaderLongFunc,
            jmsMsgProperties,
            msgBodyFunc);
    }

    private LongFunction<JmsOp> resolveMsgRead(
        LongFunction<Boolean> async_api_func,
        LongFunction<Destination> jmsDestinationFunc
    ) {
        // For Pulsar JMS, make "durable" as the default
        LongFunction<Boolean> jmsConsumerDurableFunc = (l) -> true;
        if (cmdTpl.containsKey(JmsUtil.JMS_CONSUMER_DURABLE_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_CONSUMER_DURABLE_KEY_STR)) {
                jmsConsumerDurableFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(JmsUtil.JMS_CONSUMER_DURABLE_KEY_STR));
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_CONSUMER_DURABLE_KEY_STR)) {
                jmsConsumerDurableFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getDynamic(JmsUtil.JMS_CONSUMER_DURABLE_KEY_STR, l));
            }
        }

        LongFunction<Boolean> jmsConsumerSharedFunc = (l) -> true;
        if (cmdTpl.containsKey(JmsUtil.JMS_CONSUMER_SHARED_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_CONSUMER_SHARED_KEY_STR)) {
                jmsConsumerSharedFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(JmsUtil.JMS_CONSUMER_SHARED_KEY_STR));
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_CONSUMER_SHARED_KEY_STR)) {
                jmsConsumerSharedFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getDynamic(JmsUtil.JMS_CONSUMER_SHARED_KEY_STR, l));
            }
        }

        LongFunction<String> jmsMsgSubscriptionFunc = (l) -> "";
        if (cmdTpl.containsKey(JmsUtil.JMS_CONSUMER_MSG_SUBSCRIPTIOn_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_CONSUMER_MSG_SUBSCRIPTIOn_KEY_STR)) {
                jmsMsgSubscriptionFunc = (l) -> cmdTpl.getStatic(JmsUtil.JMS_CONSUMER_MSG_SUBSCRIPTIOn_KEY_STR);
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_CONSUMER_MSG_SUBSCRIPTIOn_KEY_STR)) {
                jmsMsgSubscriptionFunc = (l) -> cmdTpl.getDynamic(JmsUtil.JMS_CONSUMER_MSG_SUBSCRIPTIOn_KEY_STR, l);
            }
        }

        LongFunction<String> jmsMsgReadSelectorFunc = (l) -> "";
        if (cmdTpl.containsKey(JmsUtil.JMS_CONSUMER_MSG_READ_SELECTOR_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_CONSUMER_MSG_READ_SELECTOR_KEY_STR)) {
                jmsMsgReadSelectorFunc = (l) -> cmdTpl.getStatic(JmsUtil.JMS_CONSUMER_MSG_READ_SELECTOR_KEY_STR);
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_CONSUMER_MSG_READ_SELECTOR_KEY_STR)) {
                jmsMsgReadSelectorFunc = (l) -> cmdTpl.getDynamic(JmsUtil.JMS_CONSUMER_MSG_READ_SELECTOR_KEY_STR, l);
            }
        }

        LongFunction<Boolean> jmsMsgNoLocalFunc = (l) -> true;
        if (cmdTpl.containsKey(JmsUtil.JMS_CONSUMER_MSG_NOLOCAL_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_CONSUMER_MSG_NOLOCAL_KEY_STR)) {
                jmsMsgNoLocalFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getStatic(JmsUtil.JMS_CONSUMER_MSG_NOLOCAL_KEY_STR));
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_CONSUMER_MSG_NOLOCAL_KEY_STR)) {
                jmsMsgNoLocalFunc = (l) -> BooleanUtils.toBoolean(cmdTpl.getDynamic(JmsUtil.JMS_CONSUMER_MSG_NOLOCAL_KEY_STR, l));
            }
        }

        LongFunction<Long> jmsReadTimeoutFunc = (l) -> 0L;
        if (cmdTpl.containsKey(JmsUtil.JMS_CONSUMER_READ_TIMEOUT_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_CONSUMER_READ_TIMEOUT_KEY_STR)) {
                jmsReadTimeoutFunc = (l) -> NumberUtils.toLong(cmdTpl.getStatic(JmsUtil.JMS_CONSUMER_READ_TIMEOUT_KEY_STR));
            } else if (cmdTpl.isDynamic(JmsUtil.JMS_CONSUMER_READ_TIMEOUT_KEY_STR)) {
                jmsReadTimeoutFunc = (l) -> NumberUtils.toLong(cmdTpl.getDynamic(JmsUtil.JMS_CONSUMER_READ_TIMEOUT_KEY_STR, l));
            }
        }

        return new JmsMsgReadMapper(
            jmsActivity,
            async_api_func,
            jmsDestinationFunc,
            jmsConsumerDurableFunc,
            jmsConsumerSharedFunc,
            jmsMsgSubscriptionFunc,
            jmsMsgReadSelectorFunc,
            jmsMsgNoLocalFunc,
            jmsReadTimeoutFunc);
    }
}
