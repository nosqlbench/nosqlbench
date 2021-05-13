package io.nosqlbench.driver.jms.ops;

/**
 * Base type of all Sync Pulsar Operations including Producers and Consumers.
 */
public abstract class JmsTimeTrackOp implements JmsOp {

    public void run(Runnable timeTracker) {
        try {
            this.run();
        } finally {
            timeTracker.run();
        }
    }

    public abstract void run();
}
