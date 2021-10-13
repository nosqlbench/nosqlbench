package io.nosqlbench.driver.pulsar.ops;

import com.codahale.metrics.Counter;
import io.nosqlbench.driver.pulsar.PulsarActivity;

public class PulsarConumerEmptyOp implements PulsarOp {

    private final PulsarActivity pulsarActivity;

    // message loss error counter
    private final Counter msgErrLossCounter;

    public PulsarConumerEmptyOp(PulsarActivity pulsarActivity) {
        this.pulsarActivity = pulsarActivity;
        this.msgErrLossCounter = pulsarActivity.getMsgErrDuplicateCounter();
    }

    @Override
    public void run(Runnable timeTracker) {
    }
}
