package io.nosqlbench.activitytype.cqld4.errorhandling;

import com.datastax.driver.core.exceptions.*;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.*;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This enumerates all known exception classes, including supertypes,
 * for the purposes of stable naming in error handling.
 * This is current as of com.datastax.cassandra:cassandra-driver-core:3.2.0
 */
public enum CQLExceptionEnum implements ResultReadable {

    FrameTooLongException(FrameTooLongException.class, 1),
    CodecNotFoundException(CodecNotFoundException.class, 2),
    DriverException(DriverException.class, 3),

    AuthenticationException(AuthenticationException.class, 4),
    TraceRetrievalException(TraceRetrievalException.class, 5),
    UnsupportedProtocolVersionException(UnsupportedProtocolVersionException.class, 6),
    NoHostAvailableException(NoHostAvailableException.class, 7),
    QueryValidationException(QueryValidationException.class, 8),
    InvalidQueryException(InvalidQueryException.class, 9),
    InvalidConfigurationInQueryException(InvalidConfigurationInQueryException.class, 10),
    UnauthorizedException(UnauthorizedException.class, 11),
    SyntaxError(SyntaxError.class, 12),
    AlreadyExistsException(AlreadyExistsException.class, 13),
    UnpreparedException(UnpreparedException.class, 14),
    InvalidTypeException(InvalidTypeException.class, 15),
    QueryExecutionException(QueryExecutionException.class, 16),
    UnavailableException(UnavailableException.class, 17),
    BootstrappingException(BootstrappingException.class, 18),
    OverloadedException(OverloadedException.class, 19),
    TruncateException(TruncateException.class, 20),
    QueryConsistencyException(QueryConsistencyException.class, 21),
    WriteTimeoutException(WriteTimeoutException.class, 22),
    WriteFailureException(WriteFailureException.class, 23),
    ReadFailureException(ReadFailureException.class, 24),
    ReadTimeoutException(ReadTimeoutException.class, 25),
    FunctionExecutionException(FunctionExecutionException.class, 26),
    DriverInternalError(DriverInternalError.class, 27),
    ProtocolError(ProtocolError.class, 28),
    ServerError(ServerError.class, 29),
    BusyPoolException(BusyPoolException.class, 30),
    ConnectionException(ConnectionException.class, 31),
    TransportException(TransportException.class, 32),
    OperationTimedOutException(OperationTimedOutException.class, 33),
    PagingStateException(PagingStateException.class, 34),
    UnresolvedUserTypeException(UnresolvedUserTypeException.class, 35),
    UnsupportedFeatureException(UnsupportedFeatureException.class, 36),
    BusyConnectionException(BusyConnectionException.class, 37),

    ChangeUnappliedCycleException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ChangeUnappliedCycleException.class, 38),
    ResultSetVerificationException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ResultSetVerificationException.class, 39),
    RowVerificationException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.RowVerificationException.class, 40),
    UnexpectedPagingException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.UnexpectedPagingException.class, 41),
    EbdseCycleException(CqlGenericCycleException.class, 42),
    MaxTriesExhaustedException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.MaxTriesExhaustedException.class,43);

    private final static Logger logger = LoggerFactory.getLogger(CQLExceptionEnum.class);

    private static Map<String, Integer> codesByName = getCodesByName();
    private static String[] namesByCode = getNamesByCode();

    private final Class<? extends Exception> exceptionClass;
    private final int resultCode;

    CQLExceptionEnum(Class<? extends Exception> clazz, int resultCode) {
        this.exceptionClass = clazz;
        this.resultCode = resultCode;
    }

    public Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }

    public int getResultCode() {
        return resultCode;
    }

    public int getResult() {
        return this.resultCode;
    }

    private static Map<String,Integer> getCodesByName() {
        codesByName = new HashMap<>();
        for (CQLExceptionEnum cqlExceptionEnum : CQLExceptionEnum.values()) {
            codesByName.put(cqlExceptionEnum.toString(), cqlExceptionEnum.resultCode);
        }
        codesByName.put("NONE",0);
        return codesByName;
    }

    private static String[] getNamesByCode() {
        List<String> namesByCode = new ArrayList<>();
        namesByCode.add("NONE");
        for (CQLExceptionEnum cqlExceptionEnum : CQLExceptionEnum.values()) {
            int code = cqlExceptionEnum.resultCode;
            for (int i = namesByCode.size(); i <= code ; i++) {
                namesByCode.add("UNKNOWN");
            }
            namesByCode.set(code, cqlExceptionEnum.toString());
        }
        return namesByCode.toArray(new String[0]);
    }
}
