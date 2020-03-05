package io.nosqlbench.activitytype.cql.errorhandling;

import com.datastax.driver.core.exceptions.*;
import io.nosqlbench.activitytype.cql.errorhandling.exceptions.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumerates all known exception classes, including supertypes,
 * for the purposes of stable naming in error handling.
 * This is current as of com.datastax.cassandra:cassandra-driver-core:3.2.0
 */
public class ExceptionMap {

    private final static Map<Class<? extends Exception>, Class<? extends Exception>> map
            = new LinkedHashMap<Class<? extends Exception>, Class<? extends Exception>>() {
        {
            put(FrameTooLongException.class, DriverException.class);
            put(CodecNotFoundException.class, DriverException.class);
            put(AuthenticationException.class, DriverException.class);
            put(TraceRetrievalException.class, DriverException.class);
            put(UnsupportedProtocolVersionException.class, DriverException.class);
            put(NoHostAvailableException.class, DriverException.class);
            put(QueryValidationException.class, DriverException.class);
            put(InvalidQueryException.class, QueryValidationException.class);
            put(InvalidConfigurationInQueryException.class, InvalidQueryException.class);
            put(UnauthorizedException.class, QueryValidationException.class);
            put(SyntaxError.class, QueryValidationException.class);
            put(AlreadyExistsException.class, QueryValidationException.class);
            put(UnpreparedException.class, QueryValidationException.class);
            put(InvalidTypeException.class, DriverException.class);
            put(QueryExecutionException.class, DriverException.class);
            put(UnavailableException.class, QueryValidationException.class);
            put(BootstrappingException.class, QueryValidationException.class);
            put(OverloadedException.class, QueryValidationException.class);
            put(TruncateException.class, QueryValidationException.class);
            put(QueryConsistencyException.class, QueryValidationException.class);
            put(WriteTimeoutException.class, QueryConsistencyException.class);
            put(WriteFailureException.class, QueryConsistencyException.class);
            put(ReadFailureException.class, QueryConsistencyException.class);
            put(ReadTimeoutException.class, QueryConsistencyException.class);
            put(FunctionExecutionException.class, QueryValidationException.class);
            put(DriverInternalError.class, DriverException.class);
            put(ProtocolError.class, DriverInternalError.class);
            put(ServerError.class, DriverInternalError.class);
            put(BusyPoolException.class, DriverException.class);
            put(ConnectionException.class, DriverException.class);
            put(TransportException.class, ConnectionException.class);
            put(OperationTimedOutException.class, ConnectionException.class);
            put(PagingStateException.class, DriverException.class);
            put(UnresolvedUserTypeException.class, DriverException.class);
            put(UnsupportedFeatureException.class, DriverException.class);
            put(BusyConnectionException.class, DriverException.class);

            put(ChangeUnappliedCycleException.class, CqlCycleException.class);
            put(ResultSetVerificationException.class, CqlCycleException.class);
            put(RowVerificationException.class, CqlCycleException.class);
            put(UnexpectedPagingException.class, CqlCycleException.class);
            put(CqlCycleException.class, RuntimeException.class);
        }
    };

    public Class<? extends Exception> put(
            Class<? extends Exception> exceptionClass,
            Class<? extends Exception> parentClass) {
        if (exceptionClass.getSuperclass() != parentClass) {
            throw new RuntimeException("Sanity check failed: " + exceptionClass +
                    " is not a parent class of " + parentClass);
        }
        return map.put(exceptionClass, parentClass);
    }

    public static Map<Class<? extends Exception>, Class<? extends Exception>> getMap() {
        return map;
    }



}
