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
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterInvalidParamException;
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterUnexpectedException;
import io.nosqlbench.adapter.jdbc.utils.JDBCPgVector;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public abstract class JDBCDMLOp extends JDBCOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCDMLOp.class);
    protected final boolean isReadStmt;
    // If the passed-in statement string has placeholder '?', it is a prepared statement.
    protected final boolean isPreparedStmt;
    protected final String pStmtSqlStr;
    protected final List<Object> pStmtValList;

    protected static ThreadLocal<Statement> jdbcStmtTL = ThreadLocal.withInitial(() -> null);

    public JDBCDMLOp(JDBCSpace jdbcSpace,
                     boolean isReadStmt,
                     String pStmtSqlStr,
                     List<Object> pStmtValList) {
        super(jdbcSpace);
        assert(StringUtils.isNotBlank(pStmtSqlStr));

        this.isReadStmt = isReadStmt;
        this.pStmtSqlStr = pStmtSqlStr;
        this.pStmtValList = pStmtValList;
        this.isPreparedStmt = StringUtils.contains(pStmtSqlStr, "?");

        // NOTE:
        // - The write DML statement MUST use prepared statement!
        // - The read DML statement can use either a prepared or a regular statement.
        if (!isReadStmt && !isPreparedStmt) {
            throw new JDBCAdapterInvalidParamException(
                "Write DML statement must use prepared statement format!");
        }

        if (isPreparedStmt) {
            int expectedFiledCnt = StringUtils.countMatches(pStmtSqlStr, "?");
            int actualFieldCnt = pStmtValList.size();
            if (expectedFiledCnt != actualFieldCnt) {
                throw new JDBCAdapterUnexpectedException(
                    "Provided value count (" + actualFieldCnt
                        + ") doesn't match the expected field count (" + expectedFiledCnt
                        + ") for the prepared statement: \"" + pStmtSqlStr + "\""
                );
            }
        }
    }

    // Only applicable to a prepared statement
    protected PreparedStatement setPrepStmtValues(PreparedStatement stmt) throws SQLException {
        assert (stmt != null);

        for (int i=0; i<pStmtValList.size(); i++) {
            int fieldIdx = i + 1;
            Object fieldValObj = pStmtValList.get(i);
            assert (fieldValObj != null);

            try {
                // Special processing for Vector
                if (fieldValObj instanceof String) {
                    String strObj = (String)fieldValObj;
                    if (StringUtils.isNotBlank(strObj)) {
                        strObj = strObj.trim();
                        // If the 'fieldVal' is a string like "[<float_num_1>, <float_num_2>, ... <float_num_n>]" format,
                        // convert it to the Vector object
                        if (strObj.startsWith("[") && strObj.endsWith("]")) {
                            JDBCPgVector vector = new JDBCPgVector();
                            vector.setValue(strObj);
                            fieldValObj = vector;
                        }
                    }
                }
                stmt.setObject(fieldIdx, fieldValObj);
            }
            catch ( SQLException e) {
                throw new SQLException(
                    "Failed to parse the prepared statement value for field[" + fieldIdx + "] " + fieldValObj);
            }
        }

        return stmt;
    }


    protected void processCommit() throws SQLException {
        if (!jdbcConnection.getAutoCommit()) {
            jdbcConnection.commit();
            LOGGER.debug(() -> LOG_COMMIT_SUCCESS);
        }
    }

    protected Statement createDMLStatement() throws SQLException {
        Statement stmt = jdbcStmtTL.get();
        if (stmt == null) {
            if (isPreparedStmt)
                stmt = jdbcConnection.prepareStatement(pStmtSqlStr);
            else
                stmt = jdbcConnection.createStatement();

            jdbcStmtTL.set(stmt);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("A statement is created -- prepared: {}, read/write: {}, stmt: {}",
                    isPreparedStmt,
                    isReadStmt ? "read" : "write",
                    stmt);
            }
        }
        return stmt;
    }
}
