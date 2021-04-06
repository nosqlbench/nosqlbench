package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;

import java.util.function.LongFunction;

public class PulsarBatchProducerStartMapper extends PulsarOpMapper {

    private final LongFunction<Producer<?>> batchProducerFunc;

    public PulsarBatchProducerStartMapper(CommandTemplate cmdTpl,
                                          PulsarSpace clientSpace,
                                          LongFunction<Boolean> asyncApiFunc,
                                          LongFunction<Producer<?>> batchProducerFunc) {
        super(cmdTpl, clientSpace, asyncApiFunc);
        this.batchProducerFunc = batchProducerFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Producer<?> batchProducer = batchProducerFunc.apply(value);
        return new PulsarBatchProducerStartOp(batchProducer);
    }
}
