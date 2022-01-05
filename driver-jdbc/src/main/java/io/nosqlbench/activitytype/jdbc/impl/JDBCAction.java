package io.nosqlbench.activitytype.jdbc.impl;

import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.jdbc.api.JDBCActivity;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class JDBCAction implements SyncAction {
    private static final Logger LOGGER = LogManager.getLogger(JDBCAction.class);

    private final JDBCActivity activity;
    private OpSequence<OpDispenser<? extends String>> sequencer;

    public JDBCAction(JDBCActivity a, int slot) {
        activity = a;
    }

    @Override
    public void init() {
        sequencer = activity.getOpSequence();
    }

    @Override
    public int runCycle(long cycle) {
        String boundStmt;

        LongFunction<? extends String> unboundStmt = sequencer.apply(cycle);

        try (Timer.Context bindTime = activity.getBindTimer().time()) {
            boundStmt = unboundStmt.apply(cycle);
        }

        int maxTries = activity.getMaxTries();
        Exception error = null;

        for (int tries = 1; tries <= maxTries; tries++) {
            long startTimeNanos = System.nanoTime();

            try (Connection conn = activity.getDataSource().getConnection()) {
                Statement jdbcStmt = conn.createStatement();
                jdbcStmt.execute(boundStmt);

            } catch (Exception e) {
                error = e;
            }

            long executionTimeNanos = System.nanoTime() - startTimeNanos;

            activity.getResultTimer().update(executionTimeNanos, TimeUnit.NANOSECONDS);
            activity.getTriesHisto().update(tries);

            if (error == null) {
                activity.getResultSuccessTimer().update(executionTimeNanos, TimeUnit.NANOSECONDS);
                return 0;
            } else {
                ErrorDetail detail = activity.getErrorHandler().handleError(error, cycle, executionTimeNanos);
                if (!detail.isRetryable()) {
                    LOGGER.debug("Exit failure after non-retryable error");
                    throw new RuntimeException("non-retryable error", error);
                }
            }

            try {
                int retryDelay = retryDelayMs(tries, activity.getMinRetryDelayMs());
                LOGGER.debug("tries=" + tries + " sleeping for " + retryDelay + " ms");
                Thread.sleep(retryDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException("thread interrupted", e);
            }
        }

        LOGGER.debug("Exit failure after maxretries=" + maxTries);
        throw new RuntimeException("maxtries exceeded", error);
    }

    /**
     * Compute retry delay based on exponential backoff with full jitter
     * @param tries 1-indexed
     * @param minDelayMs lower bound of retry delay
     * @return retry delay
     */
    private int retryDelayMs(int tries, int minDelayMs) {
        int exponentialDelay = minDelayMs * (int) Math.pow(2.0, tries - 1);
        return (int) (Math.random() * exponentialDelay);
    }
}
