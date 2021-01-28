package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.ops.PulsarOp;
import io.nosqlbench.driver.pulsar.ops.ReadyPulsarOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PulsarAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(PulsarAction.class);

    private final int slot;
    private final PulsarActivity activity;

    public PulsarAction(PulsarActivity activity, int slot) {
        this.activity = activity;
        this.slot = slot;
    }

    @Override
    public void init() {
    }

    @Override
    public int runCycle(long cycle) {

        PulsarOp pulsarOp;
        try (Timer.Context ctx = activity.getBindTimer().time()) {
            ReadyPulsarOp readyPulsarOp = activity.getSequencer().get(cycle);
            pulsarOp = readyPulsarOp.bind(cycle);
        } catch (Exception bindException) {
            // if diagnostic mode ...
            throw new RuntimeException(
                "while binding request in cycle " + cycle + ": " + bindException.getMessage(), bindException
            );
        }

        int tries = 0;
        int maxTries = 1;

        while (tries < maxTries) {
            tries++;
        }


    }
}
