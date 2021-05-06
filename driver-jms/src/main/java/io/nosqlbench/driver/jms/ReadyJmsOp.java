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
        // - queue: point-to-point
        // - topic: pub/sub
        if (cmdTpl.containsKey(JmsUtil.JMS_DESTINATION_TYPE_KEY_STR)) {
            if (cmdTpl.isStatic(JmsUtil.JMS_DESTINATION_TYPE_KEY_STR)) {
                jmsDestinationTypeFunc = (l) -> cmdTpl.getStatic(JmsUtil.JMS_DESTINATION_TYPE_KEY_STR);
            } else {
                throw new RuntimeException("\"" + JmsUtil.JMS_DESTINATION_TYPE_KEY_STR + "\" parameter cannot be dynamic!");
            }
        }

        this.opFunc = resolveJms();
    }

    public JmsOp apply(long value) { return opFunc.apply(value); }

    abstract LongFunction<JmsOp> resolveJms();
}
