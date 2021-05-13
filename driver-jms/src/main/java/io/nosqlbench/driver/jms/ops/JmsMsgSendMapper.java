package io.nosqlbench.driver.jms.ops;

import io.nosqlbench.driver.jms.JmsActivity;
import io.nosqlbench.driver.jms.util.JmsHeader;
import io.nosqlbench.driver.jms.util.JmsHeaderLongFunc;

import javax.jms.Destination;
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
public class JmsMsgSendMapper extends JmsOpMapper {
    private final JmsHeaderLongFunc jmsHeaderLongFunc;
    private final Map<String, Object> jmsMsgProperties;
    private final LongFunction<String> msgBodyFunc;

    public JmsMsgSendMapper(JmsActivity jmsActivity,
                            LongFunction<Boolean> asyncApiFunc,
                            LongFunction<Destination> jmsDestinationFunc,
                            JmsHeaderLongFunc jmsHeaderLongFunc,
                            Map<String, Object> jmsMsgProperties,
                            LongFunction<String> msgBodyFunc) {
        super(jmsActivity, asyncApiFunc, jmsDestinationFunc);

        this.jmsHeaderLongFunc = jmsHeaderLongFunc;
        this.jmsMsgProperties = jmsMsgProperties;
        this.msgBodyFunc = msgBodyFunc;
    }

    @Override
    public JmsOp apply(long value) {
        boolean asyncApi = asyncApiFunc.apply(value);
        Destination jmsDestination = jmsDestinationFunc.apply(value);
        JmsHeader jmsHeader = (JmsHeader)jmsHeaderLongFunc.apply(value);
        String msgBody = msgBodyFunc.apply(value);

        return new JmsMsgSendOp(
            jmsActivity,
            asyncApi,
            jmsDestination,
            jmsHeader,
            jmsMsgProperties,
            msgBody
        );
    }
}
