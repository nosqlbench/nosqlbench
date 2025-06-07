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
package io.nosqlbench.adapter.jdbc.optypes;

import io.nosqlbench.adapter.jdbc.JDBCSpace;
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterUnexpectedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.LongFunction;

public class JDBCDMLReadOp extends JDBCDMLOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCDMLReadOp.class);

    private String verifierKeyName;

    public JDBCDMLReadOp(JDBCSpace jdbcSpace,
                         boolean isReadStmt,
                         String pStmtSqlStr,
                         List<Object> pStmtValList,
                         String verifierKeyName) {
        super(jdbcSpace, isReadStmt, pStmtSqlStr, pStmtValList);
        this.verifierKeyName = verifierKeyName;
    }

    public JDBCDMLReadOp(JDBCSpace jdbcSpace,
                         boolean isReadStmt,
                         String pStmtSqlStr,
                         List<Object> pStmtValList,
                         String verifierKeyName,
                         LongFunction<PreparedStatement> cachedPreparedStmtFunc) {
        super(jdbcSpace, isReadStmt, pStmtSqlStr, pStmtValList, cachedPreparedStmtFunc);
        this.verifierKeyName = verifierKeyName;
    }

    @Override
    public Object apply(long value) {
        try  {
            Statement stmt = super.createDMLStatement(value);
            if (isPreparedStmt) {
                stmt = setPrepStmtValues((PreparedStatement) stmt);
            }

            // key string list to be used in the "Vector" relevancy score verification
            List<String> verifierValueList = new ArrayList<>();

            ResultSet rs;
            if (!isPreparedStmt) {
                rs = stmt.executeQuery(pStmtSqlStr);
                while (rs.next()) {
                    String keyVal = rs.getString(this.verifierKeyName);
                    if (StringUtils.isNotBlank(keyVal)) {
                        verifierValueList.add(keyVal);
                    }
                }
            }
            else {
                boolean isResultSet = ((PreparedStatement)stmt).execute();
                super.processCommit();

                while(true) {
                    if(isResultSet) {
                        rs = stmt.getResultSet();
                        while(rs.next()) {
                            String keyVal = rs.getString(this.verifierKeyName);
                            if (StringUtils.isNotBlank(keyVal)) {
                                verifierValueList.add(keyVal);
                            }
                        }
                        rs.close();
                    } else {
                        if(stmt.getUpdateCount() == -1) {
                            break;
                        }
                    }
                    isResultSet = stmt.getMoreResults();
                }
            }

            return verifierValueList;
        }
        catch (SQLException sqlException) {
            throw new JDBCAdapterUnexpectedException(
                "Failed to execute the prepared DDL stmt: '" + pStmtSqlStr + "', " +
                    "with values: [" + pStmtValList + "]: " + sqlException.getMessage(), sqlException);
        }
    }
}
