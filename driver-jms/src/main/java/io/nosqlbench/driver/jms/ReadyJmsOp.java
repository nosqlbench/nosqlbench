package io.nosqlbench.driver.jms;

import io.nosqlbench.driver.jms.ops.JmsMsgSendMapper;
import io.nosqlbench.driver.jms.ops.JmsOp;
import io.nosqlbench.driver.jms.util.JmsHeader;
import io.nosqlbench.driver.jms.util.JmsHeaderLongFunc;
import io.nosqlbench.driver.jms.util.JmsUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
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

abstract public class ReadyJmsOp implements OpDispenser<JmsOp> {

    protected final OpTemplate opTpl;
    protected final CommandTemplate cmdTpl;
    protected final JmsActivity jmsActivity;

    protected final String stmtOpType;
    protected LongFunction<Boolean> asyncApiFunc;
    protected LongFunction<String> jmsDestinationTypeFunc;
    protected JmsHeaderLongFunc jmsHeaderLongFunc;
    protected Map<String, Object> jmsMsgProperties = new HashMap<>();

    protected final LongFunction<JmsOp> opFunc;

    public ReadyJmsOp(OpTemplate opTemplate, JmsActivity jmsActivity) {
        this.opTpl = opTemplate;
        this.cmdTpl = new CommandTemplate(opTpl);
        this.jmsActivity = jmsActivity;

        if (!cmdTpl.containsKey("optype") || !cmdTpl.isStatic("optype")) {
            throw new RuntimeException("Statement parameter \"optype\" must be static and have a valid value!");
        }
        this.stmtOpType = cmdTpl.getStatic("optype");

        // Global/Doc-level parameter: async_api
        if (cmdTpl.containsKey(JmsUtil.ASYNC_API_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.ASYNC_API_KEY_STR)) {
                boolean value = BooleanUtils.toBoolean(cmdTpl.getStatic(JmsUtil.ASYNC_API_KEY_STR));
                this.asyncApiFunc = (l) -> value;
            } else {
                throw new RuntimeException("\"" + JmsUtil.ASYNC_API_KEY_STR + "\" parameter cannot be dynamic!");
            }
        }

        // Global/Doc-level parameter: jms_desitation_type
        if (cmdTpl.containsKey(JmsUtil.JMS_DESTINATION_TYPE_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_DESTINATION_TYPE_KEY_STR)) {
                jmsDestinationTypeFunc = (l) -> cmdTpl.getStatic(JmsUtil.JMS_DESTINATION_TYPE_KEY_STR);
            } else {
                throw new RuntimeException("\"" + JmsUtil.JMS_DESTINATION_TYPE_KEY_STR + "\" parameter cannot be dynamic!");
            }
        }

        jmsHeaderLongFunc = new JmsHeaderLongFunc();

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

        if ( !StringUtils.isEmpty(jmsMsgPropertyListStr) ) {
            jmsMsgProperties = Arrays.stream(jmsMsgPropertyListStr.split(";"))
                .map(s -> s.split("=", 2))
                .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : null));
        }

        this.opFunc = resolveJms();
    }

    public JmsOp apply(long value) { return opFunc.apply(value); }

    abstract LongFunction<JmsOp> resolveJms();
}
