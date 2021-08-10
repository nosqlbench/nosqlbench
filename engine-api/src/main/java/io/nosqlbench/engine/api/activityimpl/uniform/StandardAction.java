package io.nosqlbench.engine.api.activityimpl.uniform;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Timer;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.NBErrorHandler;
import io.nosqlbench.engine.api.activityapi.planning.OpSource;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.*;

import java.util.concurrent.TimeUnit;

/**
 * This is the generified version of an Action. All activity types should endeavor to use
 * this, as the API surface is being consolidated so that the internal machinery of NB
 * works in a very consistent and uniform way.
 * There will be changes to multiple drivers to support this consolidation, but the bulk
 * of this work will be undertaken by the project maintainers.
 *
 * @param <A> The type of activity
 * @param <R> The type of operation
 */
public class StandardAction<A extends StandardActivity<R, ?>, R extends Op> implements SyncAction, ActivityDefObserver {

    private final A activity;
    private final OpSource<R> opsource;
    private final int slot;
    private final Timer executeTimer;
    private final Histogram triesHistogram;
    private final Timer resultSuccessTimer;
    private final Timer resultTimer;
    private final Timer bindTimer;
    private final NBErrorHandler errorHandler;

    public StandardAction(A activity, int slot) {
        this.activity = activity;
        this.opsource = activity.getOpSource();
        this.slot = slot;
        bindTimer = activity.getInstrumentation().getOrCreateBindTimer();
        executeTimer = activity.getInstrumentation().getOrCreateExecuteTimer();
        triesHistogram = activity.getInstrumentation().getOrCreateTriesHistogram();
        resultTimer = activity.getInstrumentation().getOrCreateResultTimer();
        resultSuccessTimer = activity.getInstrumentation().getOrCreateResultSuccessTimer();
        errorHandler = activity.getErrorHandler();
    }

    @Override
    public int runCycle(long cycle) {

        Op op = null;
        try (Timer.Context ct = bindTimer.time()) {
            op = opsource.apply(cycle);
        }

        int tries = 0;
        int code = 0;

        Object result = null;
        while (tries++ <= activity.getMaxTries()) {
            Throwable error = null;
            long startedAt = System.nanoTime();

            try (Timer.Context ct = executeTimer.time()) {

                if (op instanceof CycleOp<?>) {
                    result = ((CycleOp) op).apply(cycle);
                } else if (op instanceof ChainingOp) {
                    result = ((ChainingOp) op).apply(result);
                } else {
                    throw new RuntimeException("The op implementation did not implement any active logic. Implement " +
                        "either InitialCycleFunction or ChainedCycleFunction");
                }

                if (op instanceof OpGenerator) {
                    op = ((OpGenerator) op).getNextOp();
                } else {
                    break;
                }

            } catch (Exception e) {
                error = e;
            } finally {
                long nanos = System.nanoTime() - startedAt;
                resultTimer.update(nanos, TimeUnit.NANOSECONDS);
                if (error == null) {
                    resultSuccessTimer.update(nanos, TimeUnit.NANOSECONDS);
                } else {
                    ErrorDetail detail = errorHandler.handleError(error, cycle, nanos);
                    code = detail.resultCode;
                    if (!detail.isRetryable()) {
                        break;
                    }
                }
            }
        }
        triesHistogram.update(tries);
        return code;
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
    }
}
