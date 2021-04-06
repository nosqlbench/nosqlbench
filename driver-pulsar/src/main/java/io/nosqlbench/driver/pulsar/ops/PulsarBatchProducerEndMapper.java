package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;

import java.util.function.LongFunction;

public class PulsarBatchProducerEndMapper extends PulsarOpMapper {

    public PulsarBatchProducerEndMapper(CommandTemplate cmdTpl,
                                        PulsarSpace clientSpace,
                                        LongFunction<Boolean> asyncApiFunc)
    {
        super(cmdTpl, clientSpace, asyncApiFunc);
    }

    @Override
    public PulsarOp apply(long value) {
        return new PulsarBatchProducerEndOp();
    }
}
