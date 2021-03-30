package io.nosqlbench.driver.pulsar.ops;

/**
 * Base type of all Pulsar Operations including Producers and Consumers.
 */
public interface PulsarOp {

    /**
     * Execute the operation, invoke the timeTracker when the operation ended.
     * The timeTracker can be invoked in a separate thread, it is only used for metrics.
     * @param timeTracker
     */
    void run(Runnable timeTracker);
}
