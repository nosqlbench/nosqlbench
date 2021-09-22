package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.transaction.Transaction;

import java.util.function.LongFunction;
import java.util.function.Supplier;

public abstract class PulsarOpMapper implements LongFunction<PulsarOp> {
    protected final CommandTemplate cmdTpl;
    protected final PulsarSpace clientSpace;
    protected final PulsarActivity pulsarActivity;
    protected final LongFunction<Boolean> asyncApiFunc;

    public PulsarOpMapper(CommandTemplate cmdTpl,
                          PulsarSpace clientSpace,
                          PulsarActivity pulsarActivity,
                          LongFunction<Boolean> asyncApiFunc)
    {
        this.cmdTpl = cmdTpl;
        this.clientSpace = clientSpace;
        this.pulsarActivity = pulsarActivity;
        this.asyncApiFunc = asyncApiFunc;
    }
}
