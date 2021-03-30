package io.nosqlbench.driver.pulsar.ops;

/**
 * Base type of all Pulsar Operations including Producers and Consumers.
 */
public abstract class SimplePulsarOp implements PulsarOp {

    public void run(Runnable timeTracker) {
        this.run();
        timeTracker.run();
    }

    public abstract void run();
}
