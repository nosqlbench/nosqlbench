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
    public JDBCDMLOp apply(long cycle) {
        checkShutdownEntry(cycle);

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
