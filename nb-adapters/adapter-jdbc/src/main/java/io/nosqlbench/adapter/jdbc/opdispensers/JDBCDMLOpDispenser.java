/*
 * Copyright (c) 2023 nosqlbench
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

    public JDBCDMLOpDispenser(DriverAdapter<JDBCOp, JDBCSpace> adapter,
                              JDBCSpace jdbcSpace,
                              ParsedOp op,
                              boolean isReadStmt,
                              LongFunction<String> pStmtSqlStrFunc) {
        super(adapter, jdbcSpace, op);
        this.isDdlStatement = false;
        this.isReadStatement = isReadStmt;

        int numConnInput = Integer.parseInt(op.getStaticConfig("num_conn", String.class));

        // Only apply 'one-thread-per-connection' limit to the WRITE workload
        //    due to the fact that the PostgreSQL connection is not thread safe
        // For the READ workload, Do NOT apply this limitation.
        int threadNum = jdbcSpace.getTotalThreadNum();
        int maxNumConnFinal = numConnInput;

        // For write workload, avoid thread-safety issue by using a constrained connection number
        // For read workload, it is ok to use more threads than available connections
        if (!isReadStmt)  {
            if (threadNum > numConnInput) {
                throw new JDBCAdapterInvalidParamException(
                    "JDBC connection is NOT thread safe. For write workload, the total NB thread number (" + threadNum +
                        ") can NOT be greater than the maximum connection number 'num_conn' (" + numConnInput + ")"
                );
            }
        }
        maxNumConnFinal = Math.min(threadNum, maxNumConnFinal);
        if (maxNumConnFinal < 1) {
            throw new JDBCAdapterInvalidParamException(
                "'num_conn' NB CLI parameter must be a positive number!"
            );
        }
        jdbcSpace.setMaxNumConn(maxNumConnFinal);

        logger.info("Total {} JDBC connections will be created [isReadStmt:{}, threads/{}, num_conn/{}]; " +
                "dml_batch: {}, autoCommit: {}",
            maxNumConnFinal, isReadStmt, threadNum, numConnInput,
            jdbcSpace.getDmlBatchNum(), jdbcSpace.isAutoCommit());

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
                String[] valList = pStmtValListStr.split(",\\s*(?![^\\(\\[]*[\\]\\)])");
                pStmtValListObj.addAll(Arrays.asList(valList));
            }
            return pStmtValListObj;
        };
    }

    @Override
    public JDBCDMLOp getOp(long cycle) {
        if (isReadStatement) {
            return new JDBCDMLReadOp(
                jdbcSpace,
                true,
                pStmtSqlStrFunc.apply(cycle),
                pStmtValListFunc.apply(cycle),
                this.verifierKeyName);
        }
        else {
            int ddlStmtBatchNum = jdbcSpace.getDmlBatchNum();
            return new JDBCDMLWriteOp(
                jdbcSpace,
                false,
                pStmtSqlStrFunc.apply(cycle),
                pStmtValListFunc.apply(cycle),
                ddlStmtBatchNum);
        }
    }
}
