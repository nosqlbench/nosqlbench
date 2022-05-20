package com.datastax.ebdrivers.dsegraph;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@SuppressWarnings("Duplicates")
public class GraphAction implements SyncAction, ActivityDefObserver {

    private static final Logger logger = LogManager.getLogger(GraphAction.class);
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
    public int runCycle(long cycle) {

        int tries = 0;
        BindableGraphStatement readyGraphStatement;
        SimpleGraphStatement simpleGraphStatement;
        ListenableFuture<GraphResultSet> resultSetFuture;

        try (Timer.Context graphOpTime = activity.logicalGraphOps.time()) {

            try (Timer.Context bindTime = activity.bindTimer.time()) {

                BindableGraphStatement bindableGraphStatement = opSequencer.apply(cycle);
                simpleGraphStatement = bindableGraphStatement.bind(cycle);

                if (showstmts) {
                    logger.info("GRAPH QUERY(cycle=" + cycle + "):\n" + simpleGraphStatement.getQueryString());
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
                    if (!graphErrorHandler.HandleError(e, simpleGraphStatement, cycle)) {
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
            activity.getExceptionMetrics().getExceptionMeterMetrics());

        this.opSequencer = activity.getOpSequence();

    }

}
