/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.adapter.jdbc.opdispensers;

import io.nosqlbench.adapter.jdbc.JDBCSpace;
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterInvalidParamException;
import io.nosqlbench.adapter.jdbc.optypes.JDBCDMLOp;
import io.nosqlbench.adapter.jdbc.optypes.JDBCDMLReadOp;
import io.nosqlbench.adapter.jdbc.optypes.JDBCDMLWriteOp;
import io.nosqlbench.adapter.jdbc.optypes.JDBCOp;
import io.nosqlbench.adapters.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.adapters.api.templating.ParsedOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.function.LongFunction;

public class JDBCDMLOpDispenser extends JDBCBaseOpDispenser {

    private static final Logger logger = LogManager.getLogger(JDBCDMLOpDispenser.class);

    private final boolean isReadStatement;
    private final LongFunction<String> pStmtSqlStrFunc;
    private final LongFunction<List<Object>> pStmtValListFunc;
    private final LongFunction<PreparedStatement> cachedPreparedStmtFunc;

    public JDBCDMLOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter,
        LongFunction<JDBCSpace> spaceF,
                              ParsedOp op,
                              boolean isReadStmt,
                              LongFunction<String> pStmtSqlStrFunc) {
        super(adapter, spaceF, op);
        this.isDdlStatement = false;
        this.isReadStatement = isReadStmt;

        int numConnInput = Integer.parseInt(op.getStaticConfig("num_conn", String.class));

        // Only apply 'one-thread-per-connection' limit to the WRITE workload
        //    due to the fact that the PostgreSQL connection is not thread safe
        // For the READ workload, Do NOT apply this limitation.

//        int threadNum = jdbcSpace.getTotalThreadNum();
//        int maxNumConnFinal = numConnInput;
//
//        // For write workload, avoid thread-safety issue by using a constrained connection number
//        // For read workload, it is ok to use more threads than available connections
//        if (!isReadStmt)  {
//            if (threadNum > numConnInput) {
//                throw new JDBCAdapterInvalidParamException(
//                    "JDBC connection is NOT thread safe. For write workload, the total NB thread number (" + threadNum +
//                        ") can NOT be greater than the maximum connection number 'num_conn' (" + numConnInput + ")"
//                );
//            }
//        }
//        maxNumConnFinal = Math.min(threadNum, maxNumConnFinal);
//        if (maxNumConnFinal < 1) {
//            throw new JDBCAdapterInvalidParamException(
//                "'num_conn' NB CLI parameter must be a positive number!"
//            );
//        }
//        jdbcSpace.setMaxNumConn(maxNumConnFinal);
//
//        logger.info("Total {} JDBC connections will be created [isReadStmt:{}, threads/{}, num_conn/{}]; " +
//                "dml_batch: {}, autoCommit: {}",
//            maxNumConnFinal, isReadStmt, threadNum, numConnInput,
//            jdbcSpace.getDmlBatchNum(), jdbcSpace.isAutoCommit());

        // TODO: this is a current limitation applied by this adapter
        //       improve this behavior by allowing the user to choose
        if (!isPreparedStatement && !isReadStatement) {
            throw new JDBCAdapterInvalidParamException("DML write statements MUST be prepared!");
        }

        this.pStmtSqlStrFunc = pStmtSqlStrFunc;
        Optional<LongFunction<String>> pStmtValListStrFuncOpt =
            op.getAsOptionalFunction("prep_stmt_val_arr", String.class);

        pStmtValListFunc = (l) -> {
            List<Object> pStmtValListObj = new ArrayList<>();
            if (pStmtValListStrFuncOpt.isPresent()) {
                String pStmtValListStr = pStmtValListStrFuncOpt.get().apply(l);
                List<String> valList = parseParameterValues(pStmtValListStr);
                pStmtValListObj.addAll(valList);
            }
            return pStmtValListObj;
        };

        if (isPreparedStatement) {
            int refKey = op.getRefKey();
            cachedPreparedStmtFunc = (long l) -> {
                JDBCSpace space = spaceF.apply(l);
                String sql = pStmtSqlStrFunc.apply(l);
                return space.getOrCreatePreparedStatement(refKey,
                    (long key) -> {
                        try {
                            return space.getConnection(
                                new JDBCSpace.ConnectionCacheKey("jdbc-conn-" + (key % space.getMaxNumConn())),
                                () -> {
                                    try {
                                        if (space.useHikariCP()) {
                                            return space.getHikariDataSource().getConnection();
                                        } else {
                                            throw new RuntimeException("Non-HikariCP connections not supported in cached prepared statements");
                                        }
                                    } catch (Exception e) {
                                        throw new RuntimeException("Failed to get connection for prepared statement", e);
                                    }
                                }
                            ).prepareStatement(sql);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to prepare statement: " + sql, e);
                        }
                    }
                );
            };
        } else {
            cachedPreparedStmtFunc = null;
        }
    }

    /**
     * Parse parameter values from a comma-separated string, handling commas within the values themselves.
     * This method splits on commas but respects the structure of NoSQLBench binding expressions.
     */
    private List<String> parseParameterValues(String paramStr) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int braceDepth = 0;
        boolean inQuotes = false;
        char quoteChar = 0;

        for (int i = 0; i < paramStr.length(); i++) {
            char c = paramStr.charAt(i);

            if (!inQuotes) {
                if (c == '"' || c == '\'') {
                    inQuotes = true;
                    quoteChar = c;
                    current.append(c);
                } else if (c == '{') {
                    braceDepth++;
                    current.append(c);
                } else if (c == '}') {
                    braceDepth--;
                    current.append(c);
                } else if (c == ',' && braceDepth == 0) {
                    // Found a parameter separator
                    result.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            } else {
                current.append(c);
                if (c == quoteChar) {
                    inQuotes = false;
                    quoteChar = 0;
                }
            }
        }

        // Add the last parameter
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    @Override
    public JDBCDMLOp getOp(long cycle) {
        JDBCSpace space = spaceF.apply(cycle);
        if (isReadStatement) {
            return new JDBCDMLReadOp(
                space,
                true,
                pStmtSqlStrFunc.apply(cycle),
                pStmtValListFunc.apply(cycle),
                this.verifierKeyName,
                cachedPreparedStmtFunc);
        }
        else {
            int ddlStmtBatchNum = space.getDmlBatchNum();
            return new JDBCDMLWriteOp(
                space,
                false,
                pStmtSqlStrFunc.apply(cycle),
                pStmtValListFunc.apply(cycle),
                ddlStmtBatchNum,
                cachedPreparedStmtFunc);
        }
    }
}
