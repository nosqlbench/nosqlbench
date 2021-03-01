package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.engine.api.templating.CommandTemplate;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;

import java.util.function.LongFunction;

public class PulsarReaderMapper implements LongFunction<PulsarOp> {
    private final CommandTemplate cmdTpl;
    private final Schema<?> pulsarSchema;
    private final LongFunction<Reader<?>> readerFunc;

    public PulsarReaderMapper(CommandTemplate cmdTpl,
                              Schema<?> pulsarSchema,
                              LongFunction<Reader<?>> readerFunc) {
        this.cmdTpl = cmdTpl;
        this.pulsarSchema = pulsarSchema;
        this.readerFunc = readerFunc;
    }

    @Override
    public PulsarOp apply(long value) {
        Reader<?> reader = readerFunc.apply(value);
        return new PulsarReaderOp(reader, pulsarSchema);
    }
}
