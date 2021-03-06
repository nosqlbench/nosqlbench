package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;

public class PulsarBatchProducerEndMapper extends PulsarOpMapper{

    public PulsarBatchProducerEndMapper(CommandTemplate cmdTpl,
                                        PulsarSpace clientSpace) {
        super(cmdTpl, clientSpace);
    }

    @Override
    public PulsarOp apply(long value) {
        return new PulsarBatchProducerEndOp();
    }
}
