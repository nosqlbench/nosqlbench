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

    public JmsOpMapper(JmsActivity jmsActivity,
                       LongFunction<Boolean> asyncApiFunc,
                       LongFunction<Destination> jmsDestinationFunc)
    {
        this.jmsActivity = jmsActivity;
        this.asyncApiFunc = asyncApiFunc;
        this.jmsDestinationFunc = jmsDestinationFunc;
    }
}
