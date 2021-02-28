package io.nosqlbench.driver.pulsar.ops;

import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;

public class PulsarReaderOp implements PulsarOp {
    private final Reader<?> reader;
    private final Schema<?> pulsarSchema;

    public PulsarReaderOp(Reader<?> reader, Schema<?> schema) {
        this.reader = reader;
        this.pulsarSchema = schema;
    }

    @Override
    public void run() {
        //TODO: to be added
    }
}
