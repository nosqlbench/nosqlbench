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
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCDDLOp extends JDBCOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCDDLOp.class);
    private final String ddlStmtStr;

    public JDBCDDLOp(JDBCSpace jdbcSpace, String ddlStmtStr) {
        super(jdbcSpace);
        this.ddlStmtStr = ddlStmtStr;
    }

    private Statement createDDLStatement() {
        try {
            return jdbcConnection.createStatement();
        } catch (SQLException e) {
            throw new JDBCAdapterUnexpectedException(
                "Unable to create a regular (non-prepared) JDBC statement");
        }
    }
    @Override
    public Object apply(long value) {
        try {
            Statement stmt = createDDLStatement();
            stmt.execute(ddlStmtStr);
            closeStatement(stmt);
            return true;
        } catch (SQLException sqlException) {
            throw new JDBCAdapterUnexpectedException(
                "Failed to execute the DDL statement: \"" + ddlStmtStr + "\"");
        }
    }
}
