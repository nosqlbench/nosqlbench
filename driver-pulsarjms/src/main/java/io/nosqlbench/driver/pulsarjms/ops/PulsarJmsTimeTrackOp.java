package io.nosqlbench.driver.pulsarjms.ops;

/**
 * Base type of all Sync Pulsar Operations including Producers and Consumers.
 */
public abstract class PulsarJmsTimeTrackOp implements PulsarJmsOp {

    public void run(Runnable timeTracker) {
        try {
            this.run();
        } finally {
            timeTracker.run();
        }
    }

    public abstract void run();
}
