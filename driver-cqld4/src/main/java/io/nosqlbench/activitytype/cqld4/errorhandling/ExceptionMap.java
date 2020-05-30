package io.nosqlbench.activitytype.cqld4.errorhandling;

import com.datastax.dse.driver.api.core.servererrors.UnfitClientException;
import com.datastax.oss.driver.api.core.*;
import com.datastax.oss.driver.api.core.auth.AuthenticationException;
import com.datastax.oss.driver.api.core.connection.*;
import com.datastax.oss.driver.api.core.servererrors.*;
import com.datastax.oss.driver.api.core.type.codec.CodecNotFoundException;
import com.datastax.oss.driver.internal.core.channel.ClusterNameMismatchException;
import com.datastax.oss.driver.shaded.guava.common.collect.ComputationException;
import io.nosqlbench.activitytype.cqld4.errorhandling.exceptions.*;
import org.apache.tinkerpop.gremlin.driver.exception.ConnectionException;

import java.sql.Driver;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This enumerates all known exception classes, including supertypes,
 * for the purposes of stable naming in error handling.
 * This is current as of CQL OSS driver 4.6.0
 */
public class ExceptionMap {

    private final static Map<Class<? extends Exception>, Class<? extends Exception>> map
            = new LinkedHashMap<Class<? extends Exception>, Class<? extends Exception>>() {
        {

            put(AuthenticationException.class, RuntimeException.class);
            put(ClusterNameMismatchException.class, RuntimeException.class);
            put(CodecNotFoundException.class, RuntimeException.class);
            put(ComputationException.class, RuntimeException.class);



            // DriverException subtypes

            put(AllNodesFailedException.class, DriverException.class);
              put(NoNodeAvailableException.class, AllNodesFailedException.class);
            put(BusyConnectionException.class, DriverException.class);
            put(ClosedConnectionException.class, DriverException.class);
            put(ConnectionInitException.class, DriverException.class);
            put(CoordinatorException.class, DriverException.class);
              put(ProtocolError.class, CoordinatorException.class);
              put(QueryExecutionException.class, CoordinatorException.class);
                put(BootstrappingException.class, QueryExecutionException.class);
                put(FunctionFailureException.class, QueryExecutionException.class);
                put(OverloadedException.class, QueryExecutionException.class);
                put(QueryConsistencyException.class, QueryExecutionException.class);
                  put(ReadFailureException.class, QueryConsistencyException.class);
                  put(ReadTimeoutException.class, QueryConsistencyException.class);
                  put(WriteFailureException.class, QueryConsistencyException.class);
                  put(WriteTimeoutException.class, QueryConsistencyException.class);
                put(TruncateException.class, QueryExecutionException.class);
                put(UnavailableException.class, QueryExecutionException.class);
              put(QueryValidationException.class, CoordinatorException.class);
                put(AlreadyExistsException.class, QueryValidationException.class);
                put(InvalidQueryException.class, QueryValidationException.class);
                put(InvalidConfigurationInQueryException.class, QueryValidationException.class);
                put(SyntaxError.class, QueryValidationException.class);
                put(UnauthorizedException.class, QueryValidationException.class);
              put(ServerError.class,CoordinatorException.class);
            put(UnfitClientException.class, CoordinatorException.class);
            put(DriverExecutionException.class, DriverException.class);
            put(DriverTimeoutException.class, DriverException.class);
            put(FrameTooLongException.class, DriverException.class);
            put(HeartbeatException.class,DriverException.class);
            put(InvalidKeyspaceException.class,DriverException.class);
            put(RequestThrottlingException.class,DriverException.class);
            put(UnsupportedProtocolVersionException.class, DriverException.class);

            // package org.apache.tinkerpop.gremlin.driver.exception;
            put(ConnectionException.class, DriverException.class);

            put(ChangeUnappliedCycleException.class, CqlGenericCycleException.class);
            put(ResultSetVerificationException.class, CqlGenericCycleException.class);
            put(RowVerificationException.class, CqlGenericCycleException.class);
            put(UnexpectedPagingException.class, CqlGenericCycleException.class);
            put(CqlGenericCycleException.class, RuntimeException.class);
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
