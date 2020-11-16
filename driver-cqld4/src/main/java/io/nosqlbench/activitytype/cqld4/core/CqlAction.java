package io.nosqlbench.activitytype.cqld4.core;

import com.codahale.metrics.Timer;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.cql.*;
import io.nosqlbench.activitytype.cqld4.api.D4ResultSetCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.RowCycleOperator;
import io.nosqlbench.activitytype.cqld4.api.StatementFilter;
import io.nosqlbench.activitytype.cqld4.errorhandling.ErrorStatus;
import io.nosqlbench.activitytype.cqld4.errorhandling.HashedCQLErrorHandler;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.CQLCycleWithStatementException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.MaxTriesExhaustedException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.UnexpectedPagingException;
import io.nosqlbench.activitytype.cqld4.statements.core.ReadyCQLStatement;
import io.nosqlbench.engine.api.activityapi.core.ActivityDefObserver;
import io.nosqlbench.engine.api.activityapi.core.MultiPhaseAction;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.ActivityDef;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Duplicates")
public class CqlAction implements SyncAction, MultiPhaseAction, ActivityDefObserver {

    private final static Logger logger = LogManager.getLogger(CqlAction.class);
    private final int slot;
    private final CqlActivity cqlActivity;
    private final ActivityDef activityDef;
    private List<RowCycleOperator> rowOps;
    private List<D4ResultSetCycleOperator> cycleOps;
    private List<StatementModifier> modifiers;
    private StatementFilter statementFilter;
    private OpSequence<ReadyCQLStatement> sequencer;
    private int maxTries = 10;    // how many cycles a statement will be attempted for before giving up

    private HashedCQLErrorHandler ebdseErrorHandler;

    private int pagesFetched = 0;
    private long totalRowsFetchedForQuery = 0L;
    private AsyncResultSet pagingResultSet;
    private Statement pagingStatement;
    private ReadyCQLStatement pagingReadyStatement;
    private boolean showcql;
    private long nanoStartTime;
    private long retryDelay;
    private long maxRetryDelay;
    private boolean retryReplace;

    public CqlAction(ActivityDef activityDef, int slot, CqlActivity cqlActivity) {
        this.activityDef = activityDef;
        this.cqlActivity = cqlActivity;
        this.slot = slot;
        onActivityDefUpdate(activityDef);
    }

    @Override
    public void init() {
        onActivityDefUpdate(activityDef);
        this.sequencer = cqlActivity.getOpSequencer();
    }

    @Override
    public int runCycle(long value) {
        // In this activity type, we use the same phase
        // logic for the initial phase (runCycle(...))
        // as well as subsequent phases.
        return runPhase(value);
    }

    public int runPhase(long cycleValue) {

        HashedCQLErrorHandler.resetThreadStatusCode();

        if (pagingResultSet == null) {

            totalRowsFetchedForQuery = 0L;

            Statement statement;
            CompletionStage<AsyncResultSet> resultSetFuture;
            ReadyCQLStatement readyCQLStatement;

            int tries = 0;

            try (Timer.Context bindTime = cqlActivity.bindTimer.time()) {
                readyCQLStatement = sequencer.get(cycleValue);
                statement = readyCQLStatement.bind(cycleValue);

                if (statementFilter != null) {
                    if (!statementFilter.matches(statement)) {
                        cqlActivity.skippedTokensHisto.update(cycleValue);
                        return 0;
                    }
                }

                if (modifiers != null) {
                    for (StatementModifier modifier : modifiers) {
                        statement = modifier.modify(statement, cycleValue);
                    }
                }

                if (showcql) {
                    logger.info("CQL(cycle=" + cycleValue + "):\n" + readyCQLStatement.getQueryString(cycleValue));
                }
            }
            nanoStartTime = System.nanoTime();

            while (tries < maxTries) {
                tries++;

                if (tries > maxTries) {
                    throw new MaxTriesExhaustedException(cycleValue, maxTries);
                }

                if (tries > 1) {
                    try (Timer.Context retryTime = cqlActivity.retryDelayTimer.time()) {
                        Thread.sleep(Math.min((retryDelay << tries) / 1000, maxRetryDelay / 1000));
                    } catch (InterruptedException ignored) {
                    }
                }

                CompletionStage<AsyncResultSet> completion;
                try (Timer.Context executeTime = cqlActivity.executeTimer.time()) {
                    completion = cqlActivity.getSession().executeAsync(statement);
                }

                Timer.Context resultTime = cqlActivity.resultTimer.time();
                try {
                    AsyncResultSet resultSet = completion.toCompletableFuture().get();

                    if (cycleOps != null) {
                        for (D4ResultSetCycleOperator cycleOp : cycleOps) {
                            cycleOp.apply(resultSet, statement, cycleValue);
                        }
                    }

                    D4ResultSetCycleOperator[] rsOperators = readyCQLStatement.getResultSetOperators();
                    if (rsOperators != null) {
                        for (D4ResultSetCycleOperator perStmtRSOperator : rsOperators) {
                            perStmtRSOperator.apply(resultSet, statement, cycleValue);
                        }
                    }

                    // TODO: Add parameter rebind support in cqld4 via op
//                    if (!resultSet.wasApplied()) {
//                        //resultSet.b
//                        Row row = resultSet.one();
//                        ColumnDefinitions defs = row.getColumnDefinitions();
//                        if (retryReplace) {
//                            statement =
//                                new CQLBindHelper(getCqlActivity().getSession()).rebindUnappliedStatement(statement, defs,row);
//                        }
//
//                        logger.trace(readyCQLStatement.getQueryString(cycleValue));
//                        // To make exception handling logic flow more uniformly
//                        throw new ChangeUnappliedCycleException(
//                                cycleValue, resultSet, readyCQLStatement.getQueryString(cycleValue)
//                        );
//                    }

//                    int pageRows = resultSet.getAvailableWithoutFetching();

                    int rowsInPage=0;
                    RowCycleOperator[] perStmtRowOperators = readyCQLStatement.getRowCycleOperators();

                    if (rowOps==null && perStmtRowOperators==null) {
                        for (Row row : resultSet.currentPage()) {
                            rowsInPage++;
                        }
                    } else {
                        for (Row row : resultSet.currentPage()) {
                            if (rowOps!=null) {
                                for (RowCycleOperator rowOp : rowOps) {
                                    rowOp.apply(row, cycleValue);
                                }
                            }
                            if (perStmtRowOperators!=null) {
                                for (RowCycleOperator rowOp : perStmtRowOperators) {
                                    rowOp.apply(row, cycleValue);
                                }
                            }
                            rowsInPage++;
                        }
                    }

                    cqlActivity.rowsCounter.mark(rowsInPage);
                    totalRowsFetchedForQuery += rowsInPage;

                    if (resultSet.hasMorePages()) {
                        if (cqlActivity.maxpages > 1) {
                            pagingResultSet = resultSet;
                            pagingStatement = statement;
                            pagingReadyStatement = readyCQLStatement;
                            pagesFetched = 1;
                        } else {
                            throw new UnexpectedPagingException(
                                    cycleValue,
                                    resultSet,
                                    readyCQLStatement.getQueryString(cycleValue),
                                    1,
                                    cqlActivity.maxpages,
                                    cqlActivity.getSession().getContext().getConfig().getDefaultProfile().getInt(DefaultDriverOption.REQUEST_PAGE_SIZE)
                            );
                        }
                    } else {
                        long resultNanos = System.nanoTime() - nanoStartTime;
                        cqlActivity.resultSuccessTimer.update(resultNanos, TimeUnit.NANOSECONDS);
                        cqlActivity.resultSetSizeHisto.update(totalRowsFetchedForQuery);
                        readyCQLStatement.onSuccess(cycleValue, resultNanos, totalRowsFetchedForQuery);
                    }
                    break; // This is normal termination of this loop, when retries aren't needed
                } catch (Exception e) {
                    long resultNanos = resultTime.stop();
                    resultTime = null;
                    readyCQLStatement.onError(cycleValue, resultNanos, e);
                    CQLCycleWithStatementException cqlCycleException = new CQLCycleWithStatementException(cycleValue, resultNanos, e, readyCQLStatement);
                    ErrorStatus errorStatus = ebdseErrorHandler.handleError(cycleValue, cqlCycleException);
                    if (!errorStatus.isRetryable()) {
                        cqlActivity.triesHisto.update(tries);
                        return errorStatus.getResultCode();
                    }
                } finally {
                    if (resultTime != null) {
                        resultTime.stop();
                    }
                }
            }
            cqlActivity.triesHisto.update(tries);

        } else {

            int tries = 0;

            while (tries < maxTries) {
                tries++;
                if (tries > maxTries) {
                    throw new MaxTriesExhaustedException(cycleValue, maxTries);
                }

                try (Timer.Context pagingTime = cqlActivity.pagesTimer.time()) {

                    CompletionStage<AsyncResultSet> completion;
                    try (Timer.Context executeTime = cqlActivity.executeTimer.time()) {
                        completion = pagingResultSet.fetchNextPage();
                    }

                    Timer.Context resultTime = cqlActivity.resultTimer.time();
                    try {
                        AsyncResultSet resultSet = completion.toCompletableFuture().get();

                        if (cycleOps != null) {
                            for (D4ResultSetCycleOperator cycleOp : cycleOps) {
                                cycleOp.apply(resultSet, pagingStatement, cycleValue);
                            }
                        }
                        D4ResultSetCycleOperator[] perStmtRSOperators = pagingReadyStatement.getResultSetOperators();
                        if (perStmtRSOperators != null) {
                            for (D4ResultSetCycleOperator perStmtRSOperator : perStmtRSOperators) {
                                perStmtRSOperator.apply(resultSet, pagingStatement, cycleValue);
                            }
                        }

                        pagesFetched++;

                        RowCycleOperator[] perStmtRowCycleOp = pagingReadyStatement.getRowCycleOperators();
                        int rowsInPage=0;

                        if (rowOps==null && perStmtRowCycleOp==null) {
                            for (Row row : resultSet.currentPage()) {
                                rowsInPage++;
                            }
                        } else {
                            for (Row row : resultSet.currentPage()) {
                                rowsInPage++;
                                if (rowOps!=null) {
                                    for (RowCycleOperator rowOp : rowOps) {
                                        rowOp.apply(row,cycleValue);
                                    }
                                }
                                if (perStmtRowCycleOp!=null) {
                                    for (RowCycleOperator rowCycleOperator : perStmtRowCycleOp) {
                                        rowCycleOperator.apply(row,cycleValue);
                                    }
                                }
                            }
                        }

                        cqlActivity.rowsCounter.mark(rowsInPage);
                        totalRowsFetchedForQuery += rowsInPage;

                        if (resultSet.hasMorePages()) {
                            if (pagesFetched > cqlActivity.maxpages) {
                                throw new UnexpectedPagingException(
                                        cycleValue,
                                        pagingResultSet,
                                        pagingReadyStatement.getQueryString(cycleValue),
                                        pagesFetched,
                                        cqlActivity.maxpages,
                                        cqlActivity.getSession().getContext().getConfig().getDefaultProfile().getInt(DefaultDriverOption.REQUEST_PAGE_SIZE)
                                );
                            }
                            pagingResultSet = resultSet;
                        } else {
                            long nanoTime = System.nanoTime() - nanoStartTime;
                            cqlActivity.resultSuccessTimer.update(nanoTime, TimeUnit.NANOSECONDS);
                            cqlActivity.resultSetSizeHisto.update(totalRowsFetchedForQuery);
                            pagingReadyStatement.onSuccess(cycleValue, nanoTime, totalRowsFetchedForQuery);
                            pagingResultSet = null;
                        }
                        break; // This is normal termination of this loop, when retries aren't needed
                    } catch (Exception e) {
                        long resultNanos = resultTime.stop();
                        resultTime = null;

                        pagingReadyStatement.onError(cycleValue, resultNanos, e);
                        CQLCycleWithStatementException cqlCycleException = new CQLCycleWithStatementException(cycleValue, resultNanos, e, pagingReadyStatement);
                        ErrorStatus errorStatus = ebdseErrorHandler.handleError(cycleValue, cqlCycleException);
                        if (!errorStatus.isRetryable()) {
                            cqlActivity.triesHisto.update(tries);
                            return errorStatus.getResultCode();
                        }
                    } finally {
                        if (resultTime != null) {
                            resultTime.stop();
                        }
                    }
                }
            }
            cqlActivity.triesHisto.update(tries);
        }
        return 0;
    }


    @Override
    public boolean incomplete() {
        return pagingResultSet != null;
    }

    @Override
    public void onActivityDefUpdate(ActivityDef activityDef) {
        this.maxTries = cqlActivity.getMaxTries();
        this.retryDelay = cqlActivity.getRetryDelay();
        this.maxRetryDelay = cqlActivity.getMaxRetryDelay();
        this.retryReplace = cqlActivity.isRetryReplace();
        this.showcql = cqlActivity.isShowCql();
        this.ebdseErrorHandler = cqlActivity.getCqlErrorHandler();
        this.statementFilter = cqlActivity.getStatementFilter();
        this.rowOps = cqlActivity.getRowCycleOperators();
        this.cycleOps = cqlActivity.getPageInfoCycleOperators();
        this.modifiers = cqlActivity.getStatementModifiers();
    }

    protected CqlActivity getCqlActivity() {
        return cqlActivity;
    }


}
