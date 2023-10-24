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
package io.nosqlbench.adapter.jdbc.optypes;

import io.nosqlbench.adapter.jdbc.JDBCSpace;
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterUnexpectedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

public class JDBCDMLWriteOp extends JDBCDMLOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCDMLWriteOp.class);

    private final int ddlStmtBatchNum;

    protected final static ThreadLocal<Integer> threadBatchTrackingCntTL = ThreadLocal.withInitial(() -> 0);

    public JDBCDMLWriteOp(JDBCSpace jdbcSpace,
                          boolean isReadStmt,
                          String pStmtSqlStr,
                          List<Object> pStmtValList,
                          int ddlStmtBatchNum) {
        super(jdbcSpace, isReadStmt, pStmtSqlStr, pStmtValList);
        this.ddlStmtBatchNum = ddlStmtBatchNum;
    }

    @Override
    public Object apply(long value) {
        int trackingCnt = threadBatchTrackingCntTL.get();
        trackingCnt++;
        threadBatchTrackingCntTL.set(trackingCnt);

        try {
            assert (isPreparedStmt);
            PreparedStatement stmt = (PreparedStatement) super.createDMLStatement();
            stmt = super.setPrepStmtValues(stmt);

            // No batch
            if (ddlStmtBatchNum == 1) {
                int result_cnt = stmt.executeUpdate();
                super.processCommit();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[single ddl - execution] cycle:{}, result_cnt: {}, stmt: {}",
                        value, result_cnt, stmt);
                }
                return result_cnt;
            }
            // Use batch
            else {
                stmt.addBatch();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[batch ddl - adding to batch] cycle:{},  stmt: {}",
                        value, stmt);
                }
                // NOTE: if the total number of cycles is not the multiple of the batch number,
                //       some records in a batch may not be written to the database
                //       To avoid this, make sure the total cycle number is the multiple of the batch number
                if (trackingCnt % ddlStmtBatchNum == 0) {
                    int[] counts = stmt.executeBatch();
                    processCommit();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[batch ddl - execution] cycle:{}, total_batch_res_cnt:{}, stmt: {}",
                            value, counts, stmt);
                    }
                    return IntStream.of(counts).sum();
                } else {
                    return 0;
                }
            }
        }
        catch (SQLException sqlException) {
            throw new JDBCAdapterUnexpectedException(
                "Failed to execute the prepared DDL statement: \"" + pStmtSqlStr + "\", " +
                    "with values: \"" + pStmtValList + "\"");
        }
    }
}
