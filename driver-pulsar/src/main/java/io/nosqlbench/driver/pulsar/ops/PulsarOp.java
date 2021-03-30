package io.nosqlbench.driver.pulsar.ops;

/**
 * Base type of all Pulsar Operations including Producers and Consumers.
 */
public interface PulsarOp {

    void run(Runnable callback);
}
