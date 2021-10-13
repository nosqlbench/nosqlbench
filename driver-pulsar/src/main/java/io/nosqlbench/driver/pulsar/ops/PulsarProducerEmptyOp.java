package io.nosqlbench.driver.pulsar.ops;

import io.nosqlbench.driver.pulsar.PulsarActivity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PulsarProducerEmptyOp implements PulsarOp {

    private final static Logger logger = LogManager.getLogger(PulsarProducerEmptyOp.class);

    private final PulsarActivity pulsarActivity;

    public PulsarProducerEmptyOp(PulsarActivity pulsarActivity) {
        this.pulsarActivity = pulsarActivity;
    }

    @Override
    public void run(Runnable timeTracker) {
    }
}
