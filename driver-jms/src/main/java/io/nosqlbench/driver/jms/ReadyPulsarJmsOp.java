package io.nosqlbench.driver.jms;

import io.nosqlbench.driver.jms.ops.JmsMsgSendMapper;
import io.nosqlbench.driver.jms.ops.JmsOp;
import io.nosqlbench.driver.jms.util.JmsHeader;
import io.nosqlbench.driver.jms.util.JmsUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import org.apache.commons.lang3.StringUtils;

import javax.jms.Destination;
import javax.jms.JMSRuntimeException;
import java.util.function.LongFunction;

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

        // Global: JMS destinaion
        LongFunction<Destination> jmsDestinationFunc;
        try {
            LongFunction<String> finalTopicUriFunc = topicUriFunc;
            jmsDestinationFunc = (l) -> jmsActivity.getOrCreateJmsDestination(
                jmsDestinationTypeFunc.apply(l),
                (JmsHeader) jmsHeaderLongFunc.apply(l),
                finalTopicUriFunc.apply(l));
        }
        catch (JMSRuntimeException ex) {
            throw new RuntimeException("PulsarJMS message send:: unable to create JMS desit!");
        }

        if (StringUtils.equalsIgnoreCase(stmtOpType, JmsUtil.OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(asyncApiFunc, jmsDestinationFunc);
        } /*else if (StringUtils.equalsIgnoreCase(stmtOpType, JmsUtil.OP_TYPES.MSG_READ.label)) {
            return resolveMsgConsume(asyncApiFunc, jmsDestinationFunc);
        } */ else {
            throw new RuntimeException("Unsupported Pulsar operation type");
        }
    }

    private LongFunction<JmsOp> resolveMsgSend(
        LongFunction<Boolean> async_api_func,
        LongFunction<Destination> jmsDestinationFunc
    ) {
        LongFunction<String> msgBodyFunc;
        if (cmdTpl.containsKey("msg_body")) {
            if (cmdTpl.isStatic("msg_body")) {
                msgBodyFunc = (l) -> cmdTpl.getStatic("msg_body");
            } else if (cmdTpl.isDynamic("msg_body")) {
                msgBodyFunc = (l) -> cmdTpl.getDynamic("msg_body", l);
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
}
