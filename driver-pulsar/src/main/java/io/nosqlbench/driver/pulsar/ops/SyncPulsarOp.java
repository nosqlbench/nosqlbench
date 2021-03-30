package io.nosqlbench.driver.pulsar.ops;

/**
 * Base type of all Sync Pulsar Operations including Producers and Consumers.
 */
public abstract class SyncPulsarOp implements PulsarOp {

    public void run(Runnable timeTracker) {
        try {
            this.run();
        } finally {
            timeTracker.run();
        }
    }

    public abstract void run();
}
