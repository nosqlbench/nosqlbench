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
import io.nosqlbench.adapter.jdbc.utils.JDBCPgVector;
import io.nosqlbench.engine.extensions.vectormath.PgvecUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCDMLReadOp extends JDBCDMLOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCDMLReadOp.class);

    public JDBCDMLReadOp(JDBCSpace jdbcSpace,
                         boolean isReadStmt,
                         String pStmtSqlStr,
                         List<Object> pStmtValList) {
        super(jdbcSpace, isReadStmt, pStmtSqlStr, pStmtValList);
    }

    @Override
    public Object apply(long value) {
        Statement stmt = super.createDMLStatement();
        if (isPreparedStmt) {
            stmt = super.setPrepStmtValues((PreparedStatement) stmt, this.pStmtValList);
        }

        try {
            int resultFetched = 0;
            List<ResultSet> resultSetList = new ArrayList<>();

            ResultSet rs;
            if (!isPreparedStmt) {
                rs = stmt.executeQuery(pStmtSqlStr);
                do {
                    resultSetList.add(rs);
                } while (rs.next());
                closeStatement(stmt);
            }
            else {
                boolean isResultSet = ((PreparedStatement)stmt).execute();
                super.processCommit();

                while(true) {
                    if(isResultSet) {
                        rs = stmt.getResultSet();
                        while(rs.next()) {
                            resultSetList.add(rs);
                            resultFetched++;
                        }
                        rs.close();
                    } else {
                        if(stmt.getUpdateCount() == -1) {
                            break;
                        }
                    }
                    isResultSet = stmt.getMoreResults();
                }

                closeStatement(stmt);
            }

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Total {} of results have been returned.", resultFetched);
            }

            return resultSetList;
        }
        catch (SQLException sqlException) {
            throw new JDBCAdapterUnexpectedException(
                "Failed to execute the prepared DDL stmt: \"" + pStmtSqlStr + "\", " +
                    "with values: \"" + pStmtValList + "\"");
        }
    }
}
