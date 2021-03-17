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

    private static final int MAX_TRIALS = 100;
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
        long start = System.nanoTime();

        PulsarOp pulsarOp;
        try (Timer.Context ctx = activity.getBindTimer().time()) {
            LongFunction<PulsarOp> readyPulsarOp = activity.getSequencer().get(cycle);
            pulsarOp = readyPulsarOp.apply(cycle);
        } catch (Exception bindException) {
            // if diagnostic mode ...
            activity.getErrorhandler().handleError(bindException, cycle, 0);
            throw new RuntimeException(
                "while binding request in cycle " + cycle + ": " + bindException.getMessage(), bindException
            );
        }

        for (int i = 0; i < MAX_TRIALS; i++) {
            try (Timer.Context ctx = activity.getExecuteTimer().time()) {
                pulsarOp.run();
                break;
            } catch (RuntimeException err) {
                ErrorDetail errorDetail = activity
                    .getErrorhandler()
                    .handleError(err, cycle, System.nanoTime() - start);
                if (!errorDetail.isRetryable()) {
                    break;
                }
            }
        }

        return 0;
    }
}
