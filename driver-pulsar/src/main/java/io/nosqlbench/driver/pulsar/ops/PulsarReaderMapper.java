package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Reader;

import java.util.function.LongFunction;

public class PulsarReaderMapper extends PulsarOpMapper {

    private final LongFunction<Reader<?>> readerFunc;

    public PulsarReaderMapper(CommandTemplate cmdTpl,
                              PulsarSpace clientSpace,
                              PulsarActivity pulsarActivity,
                              LongFunction<Boolean> asyncApiFunc,
                              LongFunction<Reader<?>> readerFunc)
    {
        super(cmdTpl, clientSpace, pulsarActivity, asyncApiFunc);
        this.readerFunc = readerFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Reader<?> reader = readerFunc.apply(value);
        boolean asyncApi = asyncApiFunc.apply(value);

        return new PulsarReaderOp(
            reader,
            clientSpace.getPulsarSchema(),
            asyncApi
        );
    }
}
