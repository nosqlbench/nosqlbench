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
    private OpSequence<OpDispenser<String>> sequencer;

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

        LongFunction<String> unboundStmt = sequencer.apply(cycle);

        try (Timer.Context bindTime = activity.getBindTimer().time()) {
            boundStmt = unboundStmt.apply(cycle);
        }

        int maxTries = activity.getMaxTries();

        for (int tries = 1; tries <= maxTries; tries++) {
            Exception error = null;
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
                    return 1;
                }
            }
        }

        LOGGER.debug("Exit failure after maxretries=" + maxTries);
        return 1;
    }
}
