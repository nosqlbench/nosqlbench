package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarSpace;
import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;

import java.util.function.LongFunction;

public class PulsarReaderMapper extends PulsarOpMapper {

    private final LongFunction<Reader<?>> readerFunc;
    private final LongFunction<Boolean> asyncApiFunc;

    public PulsarReaderMapper(CommandTemplate cmdTpl,
                              PulsarSpace clientSpace,
                              LongFunction<Reader<?>> readerFunc,
                              LongFunction<Boolean> asyncApiFunc) {
        super(cmdTpl, clientSpace);
        this.readerFunc = readerFunc;
        this.asyncApiFunc = asyncApiFunc;
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
