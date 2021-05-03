package io.nosqlbench.driver.jms.ops;

import io.nosqlbench.driver.jms.JmsActivity;

import javax.jms.Destination;
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
    private final LongFunction<String> msgBodyFunc;

    public JmsMsgSendMapper(JmsActivity jmsActivity,
                            LongFunction<Boolean> asyncApiFunc,
                            LongFunction<Destination> jmsDestinationFunc,
                            LongFunction<String> msgBodyFunc) {
        super(jmsActivity, asyncApiFunc, jmsDestinationFunc);
        this.msgBodyFunc = msgBodyFunc;
    }

    @Override
    public JmsOp apply(long value) {
        Destination jmsDestination = jmsDestinationFunc.apply(value);
        boolean asyncApi = asyncApiFunc.apply(value);
        String msgBody = msgBodyFunc.apply(value);

        return new JmsMsgSendOp(
            jmsActivity,
            asyncApi,
            jmsDestination,
            msgBody
        );
    }
}
