package io.nosqlbench.driver.pularjms;

import com.datastax.oss.pulsar.jms.PulsarConnectionFactory;
import io.nosqlbench.driver.pularjms.ops.PulsarJmsMsgSendMapper;
import io.nosqlbench.driver.pularjms.ops.PulsarJmsOp;
import io.nosqlbench.driver.pularjms.util.PulsarJmsActivityUtil;
import io.nosqlbench.engine.api.activityconfig.yaml.OpTemplate;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.jms.Destination;
import javax.jms.JMSRuntimeException;
import java.util.function.LongFunction;

public class ReadyPulsarJmsOp implements OpDispenser<PulsarJmsOp> {

    private final OpTemplate opTpl;
    private final CommandTemplate cmdTpl;
    private final LongFunction<PulsarJmsOp> opFunc;
    private final PulsarJmsActivity pulsarJmsActivity;

    public ReadyPulsarJmsOp(OpTemplate opTemplate, PulsarJmsActivity pulsarJmsActivity) {
        this.opTpl = opTemplate;
        this.cmdTpl = new CommandTemplate(opTpl);
        this.pulsarJmsActivity = pulsarJmsActivity;

        this.opFunc = resolve();
    }

    public PulsarJmsOp apply(long value) {
        return opFunc.apply(value);
    }

    public LongFunction<PulsarJmsOp> resolve() {
        if (!cmdTpl.containsKey("optype") || !cmdTpl.isStatic("optype")) {
            throw new RuntimeException("Statement parameter \"optype\" must be static and have a valid value!");
        }
        String stmtOpType = cmdTpl.getStatic("optype");

        // Global/Doc-level parameter: topic_uri
        LongFunction<String> topicUriFunc = (l) -> null;
        if (cmdTpl.containsKey(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label)) {
            if (cmdTpl.isStatic(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label)) {
                topicUriFunc = (l) -> cmdTpl.getStatic(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label);
            } else {
                topicUriFunc = (l) -> cmdTpl.getDynamic(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.TOPIC_URI.label, l);
            }
        }

        // Global/Doc-level parameter: async_api
        LongFunction<Boolean> asyncApiFunc = (l) -> false;
        if (cmdTpl.containsKey(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label)) {
            if (cmdTpl.isStatic(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label)) {
                boolean value = BooleanUtils.toBoolean(cmdTpl.getStatic(PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label));
                asyncApiFunc = (l) -> value;
            } else {
                throw new RuntimeException("\"" + PulsarJmsActivityUtil.DOC_LEVEL_PARAMS.ASYNC_API.label + "\" parameter cannot be dynamic!");
            }
        }

        // Global: JMS destinaion
        LongFunction<Destination> jmsDestinationFunc = (l) -> null;
        try {
            LongFunction<String> finalTopicUriFunc = topicUriFunc;
            jmsDestinationFunc = (l) -> pulsarJmsActivity.getOrCreateJmsDestination(finalTopicUriFunc.apply(l));
        }
        catch (JMSRuntimeException ex) {
            throw new RuntimeException("PulsarJMS message send:: unable to create JMS desit!");
        }

        if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarJmsActivityUtil.OP_TYPES.MSG_SEND.label)) {
            return resolveMsgSend(asyncApiFunc, jmsDestinationFunc);
        } /*else if (StringUtils.equalsIgnoreCase(stmtOpType, PulsarJmsActivityUtil.OP_TYPES.MSG_READ.label)) {
            return resolveMsgConsume(topicUriFunc, asyncApiFunc);
        } */
        else {
            throw new RuntimeException("Unsupported Pulsar operation type");
        }
    }

    private LongFunction<PulsarJmsOp> resolveMsgSend(
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
            throw new RuntimeException("PulsarJMS message send:: \"msg_body\" field must be specified!");
        }

        return new PulsarJmsMsgSendMapper(
            pulsarJmsActivity,
            async_api_func,
            jmsDestinationFunc,
            msgBodyFunc);
    }
}
