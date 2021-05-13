package io.nosqlbench.engine.api.activityimpl.uniform;

import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;

import java.util.concurrent.TimeUnit;

/**
 * This is the generified version of an Action. All activity types should endeavor to use
 * this, as the API surface is being consolidated so that the internal machinery of NB
 * works in a very consistent and uniform way.
 * There will be changes to multiple drivers to support this consolidation, but the bulk
 * of this work will be undertaken by the project maintainers.
 *
 * @param <A> The type of activity
 * @param <O> The type of operation
 */
public class StandardAction<A extends StandardActivity<O>, O extends Runnable> implements SyncAction, ActivityDefObserver {

    private final A activity;
    private final OpSequence<OpDispenser<O>> opsource;

    public StandardAction(A activity, OpSequence<OpDispenser<O>> opsource) {
        this.activity = activity;
        this.opsource = opsource;
    }

    @Override
    public int runCycle(long cycle) {

        O op = null;
        try (Timer.Context ct = activity.getInstrumentation().getOrCreateInputTimer().time()) {
            OpDispenser<O> ready = opsource.apply(cycle);
            op = ready.apply(cycle);
        }

        int tries = 0;
        int code= 0;
        Throwable error = null;
        while (tries++ <= activity.getMaxTries()) {

            long startedAt = System.nanoTime();
            try (Timer.Context ct = activity.getInstrumentation().getOrCreateExecuteTimer().time()) {
                op.run();
            } catch (Exception e) {
                error = e;
            } finally {
                long nanos = System.nanoTime() - startedAt;
                activity.getInstrumentation().getOrCreateResultTimer().update(nanos, TimeUnit.NANOSECONDS);
                if (error == null) {
                    activity.getInstrumentation().getOrCreateResultSuccessTimer().update(nanos, TimeUnit.NANOSECONDS);
                } else {
                    ErrorDetail detail = activity.getErrorHandler().handleError(error, cycle, nanos);
                    code=detail.resultCode;
                    if (!detail.isRetryable()) {
                        break;
                    }
                }
            }
        }
        activity.getInstrumentation().getOrCreateTriesHistogram().update(tries);
        return code;
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
    }
}
