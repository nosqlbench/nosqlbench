package io.nosqlbench.activitytype.cqld4.errorhandling;

import com.datastax.oss.driver.api.core.RequestThrottlingException;
import com.datastax.oss.driver.api.core.connection.ClosedConnectionException;
import com.datastax.oss.driver.api.core.servererrors.*;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.CqlGenericCycleException;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This enumerates all known exception classes, including supertypes,
 * for the purposes of stable naming in error handling.
 * This is current as of driver 4.6.0
 *
 * TODO: for cqld4, add all exceptions again, keeping the previous ones in their existing places, but eliding the
 * removed ones and leaving a place holder there, adding the new ones after
 */
public enum CQLExceptionEnum implements ResultReadable {

    FrameTooLongException(com.datastax.oss.driver.api.core.connection.FrameTooLongException.class, 1),
    CodecNotFoundException(com.datastax.oss.driver.api.core.type.codec.CodecNotFoundException.class, 2),
    DriverException(com.datastax.oss.driver.api.core.DriverException.class, 3),

    AuthenticationException(com.datastax.oss.driver.api.core.auth.AuthenticationException.class, 4),
//    TraceRetrievalException(TraceRetrievalException.class, 5),
    UnsupportedProtocolVersionException(com.datastax.oss.driver.api.core.UnsupportedProtocolVersionException.class, 6),
//    NoHostAvailableException(NoHostAvailableException.class, 7),
    QueryValidationException(com.datastax.oss.driver.api.core.servererrors.QueryValidationException.class, 8),
    InvalidQueryException(com.datastax.oss.driver.api.core.servererrors.InvalidQueryException.class, 9),
    InvalidConfigurationInQueryException(com.datastax.oss.driver.api.core.servererrors.InvalidConfigurationInQueryException.class, 10),
    UnauthorizedException(com.datastax.oss.driver.api.core.servererrors.UnauthorizedException.class, 11),
    SyntaxError(com.datastax.oss.driver.api.core.servererrors.SyntaxError.class, 12),
    AlreadyExistsException(AlreadyExistsException.class, 13),
//    UnpreparedException(UnpreparedException.class, 14),
//    InvalidTypeException(InvalidTypeException.class, 15),
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
//    FunctionExecutionException(FunctionExecutionException.class, 26),
//    DriverInternalError(DriverInternalError.class, 27),
    ProtocolError(ProtocolError.class, 28),
    ServerError(ServerError.class, 29),
//    BusyPoolException(BusyPoolException.class, 30),
//    ConnectionException(ConnectionException.class, 31),
//    TransportException(TransportException.class, 32),
//    OperationTimedOutException(OperationTimedOutException.class, 33),
//    PagingStateException(PagingStateException.class, 34),
//    UnresolvedUserTypeException(UnresolvedUserTypeException.class, 35),
//    UnsupportedFeatureException(UnsupportedFeatureException.class, 36),
    BusyConnectionException(com.datastax.oss.driver.api.core.connection.BusyConnectionException.class, 37),

    ChangeUnappliedCycleException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ChangeUnappliedCycleException.class, 38),
    ResultSetVerificationException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.ResultSetVerificationException.class, 39),
    RowVerificationException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.RowVerificationException.class, 40),
    UnexpectedPagingException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.UnexpectedPagingException.class, 41),
    EbdseCycleException(CqlGenericCycleException.class, 42),
    MaxTriesExhaustedException(io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.MaxTriesExhaustedException.class,43),

    // Added for 4.6
    ClusterNameMismatchException(com.datastax.oss.driver.internal.core.channel.ClusterNameMismatchException.class, 44),
    ComputationException(com.datastax.oss.driver.shaded.guava.common.collect.ComputationException.class,45),
    AllNodesFailedException(com.datastax.oss.driver.api.core.AllNodesFailedException.class,46),
    NoNodeAvailableException(com.datastax.oss.driver.api.core.NoNodeAvailableException.class,47),
    ClosedConnectionException(ClosedConnectionException.class,48),
    ConnectionInitException(com.datastax.oss.driver.api.core.connection.ConnectionInitException.class,49),
    CoordinatorException(CoordinatorException.class,50),
    FunctionFailureException(FunctionFailureException.class,51),
    UnfitClientException(com.datastax.dse.driver.api.core.servererrors.UnfitClientException.class,52),
    DriverExecutionException(com.datastax.oss.driver.api.core.DriverExecutionException.class,53),
    DriverTimeoutException(com.datastax.oss.driver.api.core.DriverTimeoutException.class,54),
    HeartbeatException(com.datastax.oss.driver.api.core.connection.HeartbeatException.class,55),
    InvalidKeyspaceException(com.datastax.oss.driver.api.core.InvalidKeyspaceException.class,56),
    RequestThrottlingException(RequestThrottlingException.class,57),
    CqlGenericCycleException(CqlGenericCycleException.class,58);

    private final static Logger logger = LogManager.getLogger(CQLExceptionEnum.class);

    private static Map<String, Integer> codesByName = getCodesByName();
    private static final String[] namesByCode = getNamesByCode();

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
