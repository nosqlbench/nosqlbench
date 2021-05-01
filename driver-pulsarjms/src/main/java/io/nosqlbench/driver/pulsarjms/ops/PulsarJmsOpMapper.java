package io.nosqlbench.driver.pulsarjms.ops;

import io.nosqlbench.driver.pulsarjms.PulsarJmsActivity;

import javax.jms.Destination;
import java.util.function.LongFunction;

public abstract class PulsarJmsOpMapper implements LongFunction<PulsarJmsOp> {
    protected final PulsarJmsActivity pulsarJmsActivity;
    protected final LongFunction<Boolean> asyncApiFunc;
    protected final LongFunction<Destination> jmsDestinationFunc;

    public PulsarJmsOpMapper(PulsarJmsActivity pulsarJmsActivity,
                             LongFunction<Boolean> asyncApiFunc,
                             LongFunction<Destination> jmsDestinationFunc)
    {
        this.pulsarJmsActivity = pulsarJmsActivity;
        this.asyncApiFunc = asyncApiFunc;
        this.jmsDestinationFunc = jmsDestinationFunc;
    }
}
