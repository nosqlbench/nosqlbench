package com.datastax.ebdrivers.dsegraph;

import com.codahale.metrics.Timer;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.datastax.ebdrivers.dsegraph.errorhandling.ErrorResponse;
import com.datastax.ebdrivers.dsegraph.errorhandling.GraphErrorHandler;
import com.datastax.ebdrivers.dsegraph.statements.BindableGraphStatement;
import com.google.common.util.concurrent.ListenableFuture;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("Duplicates")
public class GraphAction implements SyncAction, ActivityDefObserver {

    private static final Logger logger = LoggerFactory.getLogger(GraphAction.class);
    List<BindableGraphStatement> readyGraphStmts;
    private final int slot;
    private final GraphActivity activity;
    private int maxTries = 10;
    private boolean showstmts;
    private GraphErrorHandler graphErrorHandler;
    private ErrorResponse retryableResponse;
    private ErrorResponse realErrorResponse;
    private OpSequence<BindableGraphStatement> opSequencer;

    public GraphAction(int slot, GraphActivity activity) {
        this.slot = slot;
        this.activity = activity;
    }

    @Override
    public void init() {
        onActivityDefUpdate(activity.getActivityDef());
    }

    @Override
    public int runCycle(long cycleValue) {

        int tries = 0;
        BindableGraphStatement readyGraphStatement;
        SimpleGraphStatement simpleGraphStatement;
        ListenableFuture<GraphResultSet> resultSetFuture;

        try (Timer.Context graphOpTime = activity.logicalGraphOps.time()) {

            try (Timer.Context bindTime = activity.bindTimer.time()) {

                BindableGraphStatement bindableGraphStatement = opSequencer.get(cycleValue);
                simpleGraphStatement = bindableGraphStatement.bind(cycleValue);

                if (showstmts) {
                    logger.info("GRAPH QUERY(cycle=" + cycleValue + "):\n" + simpleGraphStatement.getQueryString());
                }
            }

            while (tries < maxTries) {
                tries++;

                try (Timer.Context executeTime = activity.executeTimer.time()) {
                    resultSetFuture = activity.getSession().executeGraphAsync(simpleGraphStatement);
                }

                try (Timer.Context resultTime = activity.resultTimer.time()) {

                    GraphResultSet resultSet = resultSetFuture.get();
                    break; // This is normal termination of this loop, when retries aren't needed
                } catch (Exception e) {
                    if (!graphErrorHandler.HandleError(e, simpleGraphStatement, cycleValue)) {
                        e.printStackTrace();
                        logger.error(e.toString(), e);
                        break;
                    }
                }
            }
        }

        activity.triesHisto.update(tries);
        return 0;

//        ReadyGraphStmt = activity.get
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {

        this.maxTries = activityDef.getParams().getOptionalInteger("maxtries").orElse(10);
        this.showstmts = activityDef.getParams().getOptionalBoolean("showcql").orElse(false);

        boolean diagnose = activityDef.getParams().getOptionalBoolean("diagnose").orElse(false);

        if (diagnose) {
            logger.warn("You are wiring all error handlers to stop for any exception." +
                    " This is useful for setup and troubleshooting, but unlikely to" +
                    " be useful for long-term or bulk testing, as retryable errors" +
                    " are normal in a busy system.");
            this.realErrorResponse = this.retryableResponse = ErrorResponse.stop;
        } else {
            String realErrorsSpec = activityDef.getParams()
                    .getOptionalString("realerrors").orElse(ErrorResponse.stop.toString());
            this.realErrorResponse = ErrorResponse.valueOf(realErrorsSpec);

            String retryableSpec = activityDef.getParams()
                    .getOptionalString("retryable").orElse(ErrorResponse.retry.toString());

            this.retryableResponse = ErrorResponse.valueOf(retryableSpec);
        }
        graphErrorHandler = new GraphErrorHandler(
                realErrorResponse,
                retryableResponse,
                activity.getExceptionCountMetrics());

        this.opSequencer = activity.getOpSequence();

    }

}
