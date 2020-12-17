package io.nosqlbench.activitytype.cqld4.errorhandling;

import com.datastax.oss.driver.api.core.DriverTimeoutException;
import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.RequestThrottlingException;
import com.datastax.oss.driver.api.core.connection.BusyConnectionException;
import com.datastax.oss.driver.api.core.connection.ClosedConnectionException;
import com.datastax.oss.driver.api.core.servererrors.OverloadedException;
import com.datastax.oss.driver.api.core.servererrors.ReadTimeoutException;
import com.datastax.oss.driver.api.core.servererrors.UnavailableException;
import com.datastax.oss.driver.api.core.servererrors.WriteTimeoutException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.CQLCycleWithStatementException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ChangeUnappliedCycleException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ResultSetVerificationException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.RowVerificationException;
import io.nosqlbench.engine.api.activityapi.errorhandling.CycleErrorHandler;
import io.nosqlbench.engine.api.activityapi.errorhandling.HashedErrorHandler;
import io.nosqlbench.engine.api.metrics.ExceptionCountMetrics;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class HashedCQLErrorHandler extends HashedErrorHandler<Throwable, ErrorStatus> {
    private static final Logger logger = LogManager.getLogger(HashedCQLErrorHandler.class);

//    private static Set<Class<? extends Throwable>> UNVERIFIED_ERRORS = new HashSet<Class<? extends Throwable>>() {{
//        add(RowVerificationException.class);
//        add(ResultSetVerificationException.class);
//    }};
    private final ExceptionCountMetrics exceptionCountMetrics;
    private static final ThreadLocal<Integer> tlResultCode = ThreadLocal.withInitial(() -> (0));

    public HashedCQLErrorHandler(ExceptionCountMetrics exceptionCountMetrics) {
        this.exceptionCountMetrics = exceptionCountMetrics;
        this.setGroup("retryable",
                NoNodeAvailableException.class,
                UnavailableException.class,
                BusyConnectionException.class,
                ClosedConnectionException.class,
                OverloadedException.class,
                WriteTimeoutException.class,
                ReadTimeoutException.class,
                DriverTimeoutException.class,
                RequestThrottlingException.class
        );
        this.setGroup(
                "unapplied",
                ChangeUnappliedCycleException.class
        );
        this.setGroup("unverified",
                RowVerificationException.class,
                ResultSetVerificationException.class
        );
        // realerrors is everything else but the above
    }

    private static class UncaughtErrorHandler implements CycleErrorHandler<Throwable, ErrorStatus> {
        @Override
        public ErrorStatus handleError(long cycle, Throwable error, String errMsg) {
            throw new RuntimeException(
                    "An exception was thrown in cycle " + cycle + " that has no error: " + errMsg + ", error:" + error
            );
        }
    }

    @Override
    public ErrorStatus handleError(long cycle, Throwable throwable, String errMsg) {
        int resultCode = 127;
        if (throwable instanceof CQLCycleWithStatementException) {
            CQLCycleWithStatementException cce = (CQLCycleWithStatementException) throwable;
            Throwable cause = cce.getCause();
            try {
                String simpleName = cause.getClass().getSimpleName();
                CQLExceptionEnum cqlExceptionEnum = CQLExceptionEnum.valueOf(simpleName);
                resultCode = cqlExceptionEnum.getResult();
            } catch (Throwable t) {
                logger.warn("unrecognized exception while mapping status code via Enum: " + throwable.getClass());
            }
        } else {
            logger.warn("un-marshaled exception while mapping status code: " + throwable.getClass());
        }
        ErrorStatus errorStatus = super.handleError(cycle, throwable, errMsg);
        errorStatus.setResultCode(resultCode);
        return errorStatus;
    }

    public static int getThreadStatusCode() {
        return tlResultCode.get();
    }

    public static void resetThreadStatusCode() {
        tlResultCode.set(0);
    }
}
