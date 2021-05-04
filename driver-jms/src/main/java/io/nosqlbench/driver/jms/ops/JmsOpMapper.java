package io.nosqlbench.driver.jms.ops;

import io.nosqlbench.driver.jms.JmsActivity;
import io.nosqlbench.driver.jms.util.JmsHeaderLongFunc;

import javax.jms.Destination;
import java.util.Map;
import java.util.function.LongFunction;

public abstract class JmsOpMapper implements LongFunction<JmsOp> {
    protected final JmsActivity jmsActivity;
    protected final LongFunction<Boolean> asyncApiFunc;
    protected final LongFunction<Destination> jmsDestinationFunc;
    protected final JmsHeaderLongFunc jmsHeaderLongFunc;
    protected final Map<String, Object> jmsMsgProperties;

    public JmsOpMapper(JmsActivity jmsActivity,
                       LongFunction<Boolean> asyncApiFunc,
                       LongFunction<Destination> jmsDestinationFunc,
                       JmsHeaderLongFunc jmsHeaderLongFunc,
                       Map<String, Object> jmsMsgProperties)
    {
        this.jmsActivity = jmsActivity;
        this.asyncApiFunc = asyncApiFunc;
        this.jmsDestinationFunc = jmsDestinationFunc;
        this.jmsHeaderLongFunc = jmsHeaderLongFunc;
        this.jmsMsgProperties = jmsMsgProperties;
    }
}
