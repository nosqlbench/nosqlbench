package io.nosqlbench.activitytype.cqld4.errorhandling;

import io.nosqlbench.activitytype.cqld4.api.ErrorResponse;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.CQLCycleWithStatementException;
import io.nosqlbench.engine.api.activityapi.errorhandling.CycleErrorHandler;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A contextualized error handler that can catch a cycle-specific error.
 * In this class, the error handlers return a boolean, which indicates
 * to the call whether or not to retry the operation. This handler implements
 * the error handling stack approach, which allows the user to select an
 * entry point in the stack, with all lesser impacting handler rules
 * applied from most impacting to least impacting order.
 *
 * For simplicity, the handler stack is fixed as described below. It is not
 * possible to rearrange the verbs. Some care has been given to making sure
 * that the selected handlers are complete and intuitive.
 *
 * The standard handler stack looks like this:
 *
 * <ol>
 *     <li>stop - log and throw an exception, which should escape to the
 *     next level of exception handling, the level which causes ebdse
 *     to stop running. In this case, and only in this case, the remaining
 *     handlers in the stack are not used.
 *     are not reached.</li>
 *     <li>warn - log an exception without stopping execution.</li>
 *     <li>retry - retry an operation up to a limit, IFF it is retryable</li>
 *     <li>count - count, in metrics, the number of this particular error type</li>
 *     <li>ignore - do nothing</li>
 * </ol>
 *
 * As indicated above, if you specify "warn" for a particular error type, this means
 * that also retry, count, will apply, as well as ignore, in that order. "ignore" is
 * simply a no-op that allows you to specify it as the minimum case.
 */
@SuppressWarnings("Duplicates")
public class NBCycleErrorHandler implements CycleErrorHandler<Throwable, ErrorStatus> {

    private static final Logger logger = LoggerFactory.getLogger(NBCycleErrorHandler.class);

    private ErrorResponse errorResponse;
    private ExceptionCountMetrics exceptionCountMetrics;
    private final ExceptionHistoMetrics exceptionHistoMetrics;
    private boolean throwExceptionOnStop=false;

    public NBCycleErrorHandler(
            ErrorResponse errorResponse,
            ExceptionCountMetrics exceptionCountMetrics,
            ExceptionHistoMetrics exceptionHistoMetrics,
            boolean throwExceptionOnStop) {
        this.errorResponse = errorResponse;
        this.exceptionCountMetrics = exceptionCountMetrics;
        this.exceptionHistoMetrics = exceptionHistoMetrics;
        this.throwExceptionOnStop = throwExceptionOnStop;
    }

    @Override
    public ErrorStatus handleError(long cycle, Throwable contextError) {
        CQLCycleWithStatementException cce = (CQLCycleWithStatementException) contextError;
        Throwable error = cce.getCause();

        boolean retry = false;
        switch (errorResponse) {
            case stop:
                logger.error("error with cycle " + cycle + ": statement: " + cce.getStatement() + " errmsg: " + error.getMessage());
                if (throwExceptionOnStop) {
                    throw new RuntimeException(error);
                }

            case warn:
                logger.warn("error with cycle " + cycle + ": statement: " + cce.getStatement() + " errmsg: " + error.getMessage());
            case retry:
                retry = true;
            case histogram:
                exceptionHistoMetrics.update(error,cce.getDurationNanos());
            case count:
                exceptionCountMetrics.count(error);
            case ignore:
            default:
                break;
        }
        return new ErrorStatus(errorResponse, retry,-1);
    }

    @Override
    public ErrorStatus handleError(long cycle, Throwable contextError, String errMsg) {
        return handleError(cycle,contextError);
    }

    public String toString() {
        return this.errorResponse.toString();
    }
}
