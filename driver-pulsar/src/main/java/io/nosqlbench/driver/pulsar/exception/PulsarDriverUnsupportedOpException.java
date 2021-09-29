package io.nosqlbench.driver.pulsar.exception;

public class PulsarDriverUnsupportedOpException extends RuntimeException {

    public PulsarDriverUnsupportedOpException() { super("Unsupported Pulsar driver operation type"); }

}
