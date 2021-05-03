package io.nosqlbench.driver.jms.ops;

/**
 * Base type of all Pulsar Operations including Producers and Consumers.
 */
public interface JmsOp {

    /**
     * Execute the operation, invoke the timeTracker when the operation ended.
     * The timeTracker can be invoked in a separate thread, it is only used for metrics.
     */
    void run(Runnable timeTracker);
}
