package io.nosqlbench.driver.pulsar.exception;

public class PulsarDriverUnexpectedException extends RuntimeException {

    public PulsarDriverUnexpectedException(String message) {
        super(message);
    }
    public PulsarDriverUnexpectedException(Exception e) { super(e); }
}
