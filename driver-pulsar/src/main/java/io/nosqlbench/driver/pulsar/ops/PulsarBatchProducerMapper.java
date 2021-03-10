package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Producer;

import java.util.function.LongFunction;

public class PulsarBatchProducerMapper extends PulsarOpMapper{
    private final LongFunction<String> keyFunc;
    private final LongFunction<String> payloadFunc;

    public PulsarBatchProducerMapper(CommandTemplate cmdTpl,
                                     PulsarSpace clientSpace,
                                     LongFunction<String> keyFunc,
                                     LongFunction<String> payloadFunc) {
        super(cmdTpl, clientSpace);
        this.keyFunc = keyFunc;
        this.payloadFunc = payloadFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        String msgKey = keyFunc.apply(value);
        String msgPayload = payloadFunc.apply(value);

        return new PulsarBatchProducerOp(
            clientSpace.getPulsarSchema(),
            msgKey,
            msgPayload
        );
    }
}
