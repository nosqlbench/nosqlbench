package io.nosqlbench.driver.jms;

import com.codahale.metrics.Timer;
import io.nosqlbench.driver.jms.ops.JmsOp;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.LongFunction;

public class JmsAction implements SyncAction {

    private final static Logger logger = LogManager.getLogger(JmsAction.class);

    private final JmsActivity activity;
    private final int slot;

    int maxTries;

    public JmsAction(JmsActivity activity, int slot) {
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

        JmsOp jmsOp;
        try (Timer.Context ctx = activity.getBindTimer().time()) {
            LongFunction<JmsOp> readyPulsarJmsOp = activity.getSequencer().get(cycle);
            jmsOp = readyPulsarJmsOp.apply(cycle);
        } catch (Exception bindException) {
            // if diagnostic mode ...
            activity.getErrorhandler().handleError(bindException, cycle, 0);
            throw new RuntimeException(
                "while binding request in cycle " + cycle + ": " + bindException.getMessage(), bindException
            );
        }

        for (int i = 0; i < maxTries; i++) {
            Timer.Context ctx = activity.getExecuteTimer().time();
            try {
                // it is up to the pulsarOp to call Context#close when the activity is executed
                // this allows us to track time for async operations
                jmsOp.run(ctx::close);
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
