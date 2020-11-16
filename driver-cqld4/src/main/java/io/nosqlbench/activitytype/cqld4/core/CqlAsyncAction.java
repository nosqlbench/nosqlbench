package io.nosqlbench.activitytype.cqld4.core;

import com.codahale.metrics.Timer;
import com.datastax.oss.driver.api.core.config.TypedDriverOption;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.nosqlbench.activitytype.cqld4.api.ErrorResponse;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.StatementFilter;
import io.nosqlbench.activitytype.cqld4.errorhandling.ErrorStatus;
import io.nosqlbench.activitytype.cqld4.errorhandling.HashedCQLErrorHandler;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.CQLCycleWithStatementException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ChangeUnappliedCycleException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.UnexpectedPagingException;
import io.nosqlbench.activitytype.cqld4.statements.core.CQLSessionCache;
import io.nosqlbench.activitytype.cqld4.statements.core.ReadyCQLStatement;
import io.nosqlbench.engine.api.activityapi.core.BaseAsyncAction;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.FailedOp;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.StartedOp;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.SucceededOp;
import io.nosqlbench.engine.api.activityapi.core.ops.fluent.opfacets.TrackedOp;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

@SuppressWarnings("Duplicates")
public class CqlAsyncAction extends BaseAsyncAction<CqlOpData, CqlActivity> {

    private final static Logger logger = LogManager.getLogger(CqlAsyncAction.class);
    private final ActivityDef activityDef;

    private List<RowCycleOperator> rowOps;
    private List<D4ResultSetCycleOperator> cycleOps;
    private List<StatementModifier> modifiers;
    private StatementFilter statementFilter;
    private OpSequence<ReadyCQLStatement> sequencer;

    // how many cycles a statement will be attempted for before giving up
    private int maxTries = 10;

    private HashedCQLErrorHandler cqlActivityErrorHandler;

    //    private int pagesFetched = 0;
//    private long totalRowsFetchedForQuery = 0L;
//    private ResultSet pagingResultSet;
//    private Statement pagingStatement;
//    private ReadyCQLStatement pagingReadyStatement;
    private boolean showcql;
//    private long opsInFlight = 0L;
//    private long maxOpsInFlight = 1L;
//    private long pendingResults = 0;
//    private LinkedBlockingQueue<CqlOpContext> resultQueue = new LinkedBlockingQueue<>();

    public CqlAsyncAction(CqlActivity activity, int slot) {
        super(activity, slot);
        onActivityDefUpdate(activity.getActivityDef());
        this.activityDef = activity.getActivityDef();
    }

    @Override
    public void init() {
        onActivityDefUpdate(activityDef);
        this.sequencer = activity.getOpSequencer();
    }

    @Override
    public LongFunction<CqlOpData> getOpInitFunction() {
        return (l) -> {
            return new CqlOpData(l, this);
        };
    }

    @Override
    public void startOpCycle(TrackedOp<CqlOpData> opc) {
        CqlOpData cqlop = opc.getData();
        long cycle = opc.getCycle();

        // bind timer covers all statement selection and binding, skipping, transforming logic
        try (Timer.Context bindTime = activity.bindTimer.time()) {
            cqlop.readyCQLStatement = sequencer.get(cycle);
            cqlop.statement = cqlop.readyCQLStatement.bind(cycle);

            // If a filter is defined, skip and count any statements that do not match it
            if (statementFilter != null) {
                if (!statementFilter.matches(cqlop.statement)) {
                    activity.skippedTokensHisto.update(cycle);
                    //opc.start().stop(-2);
                    cqlop.skipped = true;
                    opc.skip(0);
                    return;
                }
            }

            // Transform the statement if there are any statement transformers defined for this CQL activity
            if (modifiers != null) {
                for (StatementModifier modifier : modifiers) {
                    cqlop.statement = modifier.modify(cqlop.statement, cycle);
                }
            }

            // Maybe show the CQl in log/console - only for diagnostic use
            if (showcql) {
                logger.info("CQL(cycle=" + cycle + "):\n" + cqlop.readyCQLStatement.getQueryString(cycle));
            }
        }

        StartedOp<CqlOpData> startedOp = opc.start();
        cqlop.startedOp = startedOp;

        // The execute timer covers only the point at which EB hands the op to the driver to be executed
        try (Timer.Context executeTime = activity.executeTimer.time()) {
            CompletionStage<AsyncResultSet> completionStage = activity.getSession().executeAsync(cqlop.statement);
            completionStage.whenComplete(cqlop::handleAsyncResult);
        }
    }


    public void onSuccess(StartedOp<CqlOpData> sop, AsyncResultSet resultSet) {
        CqlOpData cqlop = sop.getData();

        HashedCQLErrorHandler.resetThreadStatusCode();
        if (cqlop.skipped) {
            return;
        }

        try {

            cqlop.totalPagesFetchedForQuery++;

            // Apply any defined ResultSetCycleOperators
            // TODO: Implement result and row operators for cqld4 actions
//            if (cycleOps != null) {
//                for (ResultSetCycleOperator cycleOp : cycleOps) {
//                    cycleOp.apply(resultSet, cqlop.statement, cqlop.cycle);
//                    resultSet.
//                }
//            }
//
            int rowsInPage = 0;
//            if (rowOps==null) {
                for (Row row : resultSet.currentPage()) {
                    rowsInPage++;
                }
//            } else {
//                for (Row row : resultSet.currentPage()) {
//                    rowsInPage++;
//                    for (RowCycleOperator rowOp : rowOps) {
//                        rowOp.apply(row, cqlop.cycle);
//                    }
//                }
//            }
            cqlop.totalRowsFetchedForQuery += rowsInPage;

            if (cqlop.totalPagesFetchedForQuery++ > activity.maxpages) {
                Integer pagesize = CQLSessionCache.get().getSession(activityDef).optionsMap.get(TypedDriverOption.REQUEST_PAGE_SIZE);
                throw new UnexpectedPagingException(
                        cqlop.cycle,
                        resultSet,
                        cqlop.readyCQLStatement.getQueryString(cqlop.cycle),
                        1,
                        activity.maxpages,
                        pagesize
                );
            }

            if (!resultSet.wasApplied()) {
                // To make exception handling logic flow more uniformly
                throw new ChangeUnappliedCycleException(
                        cqlop.cycle, resultSet, cqlop.readyCQLStatement.getQueryString(cqlop.cycle)
                );
            }


            if (!resultSet.hasMorePages()) {
                logger.trace("async paging request " + cqlop.totalPagesFetchedForQuery + " for cycle " + cqlop.cycle);

                resultSet.fetchNextPage().whenComplete(cqlop::handleAsyncResult);
                return;
            }

            SucceededOp<CqlOpData> success = sop.succeed(0);
            cqlop.readyCQLStatement.onSuccess(cqlop.cycle, success.getServiceTimeNanos(), cqlop.totalRowsFetchedForQuery);

            activity.triesHisto.update(cqlop.triesAttempted);
            activity.rowsCounter.mark(cqlop.totalRowsFetchedForQuery);
            activity.resultSuccessTimer.update(success.getServiceTimeNanos(), TimeUnit.NANOSECONDS);
            activity.resultSetSizeHisto.update(cqlop.totalRowsFetchedForQuery);
            activity.resultTimer.update(success.getServiceTimeNanos(), TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            long currentServiceTime = sop.getCurrentServiceTimeNanos();

            cqlop.readyCQLStatement.onError(cqlop.cycle, currentServiceTime, e);

            CQLCycleWithStatementException cqlCycleException = new CQLCycleWithStatementException(cqlop.cycle, currentServiceTime, e, cqlop.readyCQLStatement);
            ErrorStatus errorStatus = cqlActivityErrorHandler.handleError(cqlop.cycle, cqlCycleException);

            if (errorStatus.isRetryable() && ++cqlop.triesAttempted < maxTries) {
                activity.getSession().executeAsync(cqlop.statement).whenComplete(cqlop::handleAsyncResult);
                return;
            } else {
                sop.fail(errorStatus.getResultCode());
                if (errorStatus.getResponse() == ErrorResponse.stop) {
                    cqlop.throwable = cqlCycleException;
                    activity.getActivityController().stopActivityWithErrorAsync(cqlCycleException);
                }
            }
        }

    }

    public void onFailure(StartedOp<CqlOpData> startedOp) {

        CqlOpData cqlop = startedOp.getData();
        long serviceTime = startedOp.getCurrentServiceTimeNanos();

        // Even if this is retryable, we expose error events
        cqlop.readyCQLStatement.onError(startedOp.getCycle(),serviceTime,cqlop.throwable);

        long cycle = startedOp.getCycle();
        CQLCycleWithStatementException cqlCycleException1 = new CQLCycleWithStatementException(cqlop.cycle, serviceTime, cqlop.throwable, cqlop.readyCQLStatement);
        ErrorStatus errorStatus = cqlActivityErrorHandler.handleError(startedOp.getCycle(), cqlCycleException1);

        if (errorStatus.getResponse() == ErrorResponse.stop) {
            activity.getActivityController().stopActivityWithErrorAsync(cqlop.throwable);
            return;
        }

        if (errorStatus.isRetryable() && cqlop.triesAttempted < maxTries) {
            startedOp.retry();
            try (Timer.Context executeTime = activity.executeTimer.time()) {
                activity.getSession().executeAsync(cqlop.statement).whenComplete(cqlop::handleAsyncResult);
                return;
            }
        }

        FailedOp<CqlOpData> failed = startedOp.fail(errorStatus.getResultCode());
        activity.resultTimer.update(failed.getServiceTimeNanos(), TimeUnit.NANOSECONDS);
        activity.triesHisto.update(cqlop.triesAttempted);


    }


    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        this.maxTries = activity.getMaxTries();
        this.showcql = activity.isShowCql();
        this.cqlActivityErrorHandler = activity.getCqlErrorHandler();
        this.statementFilter = activity.getStatementFilter();
        this.rowOps = activity.getRowCycleOperators();
        this.cycleOps = activity.getPageInfoCycleOperators();
        this.modifiers = activity.getStatementModifiers();
    }

    public String toString() {
        return "CqlAsyncAction["+this.slot+"]";
    }
}
