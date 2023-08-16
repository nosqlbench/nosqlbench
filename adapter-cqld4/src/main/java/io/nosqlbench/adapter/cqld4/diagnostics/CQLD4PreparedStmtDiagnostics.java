/*
 * Copyright (c) 2022-2023 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.adapter.cqld4.diagnostics;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ColumnDefinition;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.data.CqlDuration;
import com.datastax.oss.driver.api.core.data.CqlVector;
import com.datastax.oss.driver.api.core.data.TupleValue;
import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.VectorType;
import com.datastax.oss.driver.api.core.type.codec.ExtraTypeCodecs;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import io.nosqlbench.adapter.cqld4.optypes.Cqld4CqlOp;
import io.nosqlbench.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;

import static com.datastax.oss.protocol.internal.ProtocolConstants.DataType.*;


/**
 * This should only be used when there is an exception thrown by some higher level logic.
 * The purpose of this class is to do a more thorough job of checking each step of binding
 * values to a prepared statement, and to provide useful feedback to the user
 * explaining more specifically what the problem was that caused the original error to be thrown.
 */
public class CQLD4PreparedStmtDiagnostics {
    private static final Logger logger = LogManager.getLogger(CQLD4PreparedStmtDiagnostics.class);
    private static final ConcurrentHashMap<VectorType, TypeCodec<float[]>> vectorCodecs = new ConcurrentHashMap<>();

    public static BoundStatement bindStatement(
        BoundStatement bound,
        CqlIdentifier colname,
        Object colval,
        DataType coltype
    ) {
        return switch (coltype.getProtocolCode()) {
            case CUSTOM -> {
                if (coltype instanceof VectorType vt) {
                    if (colval instanceof CqlVector cv) {
                        yield bound.setVector(colname, cv, cv.get(0).getClass());
                    } else if (colval instanceof float[] floatAry) {
                        TypeCodec<float[]> codec = vectorCodecs.computeIfAbsent(vt, v -> ExtraTypeCodecs.floatVectorToArray(v.getDimensions()));
                        yield bound.set(colname, floatAry, codec);
                    } else {
                        throw new RuntimeException("Unrecognized vector Java type to bind to " + coltype.asCql(true, true) + " value=" +colval.getClass().getSimpleName());
                    }
                } else {
                    throw new RuntimeException("Unrecognized custom type for diagnostics: " + coltype.asCql(true, true) + " value= " +colval.getClass().getSimpleName());
                }
            }
            case ASCII, VARCHAR -> bound.setString(colname, (String) colval);
            case BIGINT, COUNTER -> bound.setLong(colname, (long) colval);
            case BLOB -> bound.setByteBuffer(colname, (ByteBuffer) colval);
            case BOOLEAN -> bound.setBoolean(colname, (boolean) colval);
            case DECIMAL -> bound.setBigDecimal(colname, (BigDecimal) colval);
            case DOUBLE -> bound.setDouble(colname, (double) colval);
            case FLOAT -> bound.setFloat(colname, (float) colval);
            case INT -> bound.setInt(colname, (int) colval);
            case SMALLINT -> bound.setShort(colname, (short) colval);
            case TINYINT -> bound.setByte(colname, (byte) colval);
            case TIMESTAMP -> bound.setInstant(colname, (Instant) colval);
            case TIMEUUID, UUID -> bound.setUuid(colname, (UUID) colval);
            case VARINT -> bound.setBigInteger(colname, (BigInteger) colval);
            case INET -> bound.setInetAddress(colname, (InetAddress) colval);
            case DATE -> bound.setLocalDate(colname, (LocalDate) colval);
            case TIME -> bound.setLocalTime(colname, (LocalTime) colval);
            case DURATION -> bound.setCqlDuration(colname, (CqlDuration) colval);
            case LIST -> bound.setList(colname, (List) colval, ((List) colval).get(0).getClass());
            case MAP -> {
                Map map = (Map) colval;
                Set<Map.Entry> entries = map.entrySet();
                Optional<Map.Entry> first = entries.stream().findFirst();
                if (first.isPresent()) {
                    yield bound.setMap(colname, map, first.get().getKey().getClass(), first.get().getValue().getClass());
                } else {
                    yield bound.setMap(colname, map, Object.class, Object.class);
                }
            }
            case SET -> {
                Set set = (Set) colval;
                Optional first = set.stream().findFirst();
                if (first.isPresent()) {
                    yield bound.setSet(colname, set, first.get().getClass());
                } else {
                    yield bound.setSet(colname, Set.of(), Object.class);
                }
            }
            case UDT -> {
                UdtValue udt = (UdtValue) colval;
                yield bound.setUdtValue(colname, udt);
            }
            case TUPLE -> {
                TupleValue tuple = (TupleValue) colval;
                yield bound.setTupleValue(colname, tuple);
            }
            default -> throw new RuntimeException("Unknown CQL type for diagnostic " +
                "(type:'" + coltype + "',code:'" + coltype.getProtocolCode() + "'");
        };
    }

    public static Cqld4CqlOp rebindWithDiagnostics(
        PreparedStatement preparedStmt,
        LongFunction<Object[]> fieldsF,
        long cycle,
        Exception exception
    ) {
        logger.error(exception);
        ColumnDefinitions defs = preparedStmt.getVariableDefinitions();
        Object[] values = fieldsF.apply(cycle);
        if (defs.size() != values.length) {
            throw new OpConfigError("There are " + defs.size() + " anchors in statement '" + preparedStmt.getQuery() + "'" +
                "but " + values.length + " values were provided. These must match.");
        }

        BoundStatement bound = preparedStmt.bind();
        int idx = 0;
        for (int i = 0; i < defs.size(); i++) {
            Object value = values[i];
            ColumnDefinition def = defs.get(i);
            CqlIdentifier defname = def.getName();
            DataType type = def.getType();
            try {
                bound = CQLD4PreparedStmtDiagnostics.bindStatement(bound, defname, value, type);
            } catch (Exception e) {
                String fullValue = value.toString();
                String valueToPrint = fullValue.length() > 100 ? fullValue.substring(0, 100) + " ... (abbreviated for console, since the size is " + fullValue.length() + ")" : fullValue;
                String errormsg = String.format(
                    "Unable to bind column '%s' to cql type '%s' with value '%s' (class '%s')",
                    defname,
                    type.asCql(false, false),
                    valueToPrint,
                    value.getClass().getCanonicalName()
                );
                logger.error(errormsg);
                throw new OpConfigError(errormsg, e);

            }
        }

        // If we got here, then either someone used the diagnostic binder where they shouldn't (It's SLOW,
        // and there was no exception which prompted a retry with this diagnostic) OR
        // There was an error detected in the caller and it was not seen here where it should have been
        // reproduced.
        throw new OpConfigError("The diagnostic binder was called but no error was found. This is a logic error.");
    }

}
