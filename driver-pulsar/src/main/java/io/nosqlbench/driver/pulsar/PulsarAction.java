package io.nosqlbench.driver.pulsar;

import com.codahale.metrics.Timer;
import io.nosqlbench.driver.pulsar.ops.PulsarOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class PulsarAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(PulsarAction.class);

    private final int slot;
    private final PulsarActivity activity;
    int maxTries = 1;

    public PulsarAction(PulsarActivity activity, int slot) {
        this.activity = activity;
        this.slot = slot;
        this.maxTries = activity.getActivityDef().getParams().getOptionalInteger("maxtries").orElse(10);
    }

    @Override
    public void init() {
    }

    @Override
    public int runCycle(long cycle) {

        // let's fail the action if some async operation failed
        activity.failOnAsyncOperationFailure();

        long start = System.nanoTime();

        PulsarOp pulsarOp;
        try (Timer.Context ctx = activity.getBindTimer().time()) {
            LongFunction<PulsarOp> readyPulsarOp = activity.getSequencer().apply(cycle);
            pulsarOp = readyPulsarOp.apply(cycle);
        } catch (Exception bindException) {
            // if diagnostic mode ...
            activity.getErrorHandler().handleError(bindException, cycle, 0);
            throw new RuntimeException(
                "while binding request in cycle " + cycle + ": " + bindException.getMessage(), bindException
            );
        }

        for (int i = 0; i < maxTries; i++) {
            Timer.Context ctx = activity.getExecuteTimer().time();
            try {
                // it is up to the pulsarOp to call Context#close when the activity is executed
                // this allows us to track time for async operations
                pulsarOp.run(ctx::close);
                break;
            } catch (RuntimeException err) {
                ErrorDetail errorDetail = activity
                    .getErrorHandler()
                    .handleError(err, cycle, System.nanoTime() - start);
                if (!errorDetail.isRetryable()) {
                    break;
                }
            }
        }

        return 0;
    }
}
