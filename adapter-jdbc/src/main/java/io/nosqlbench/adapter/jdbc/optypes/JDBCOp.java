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
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class JDBCOp implements CycleOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCOp.class);
    protected static final String LOG_COMMIT_SUCCESS =
        "Executed the JDBC statement & committed the connection successfully";

    protected final JDBCSpace jdbcSpace;
    protected final Connection jdbcConnection;

    public JDBCOp(JDBCSpace jdbcSpace) {
        this.jdbcSpace = jdbcSpace;
        String curThreadName = Thread.currentThread().getName();
        this.jdbcConnection = this.jdbcSpace.getConnection(curThreadName);
    }

    protected void closeStatement(Statement stmt) throws SQLException {
        /*
         * NO-op for now
         * ------------------
        if (! (stmt instanceof  PreparedStatement)) {
            stmt.close();
        } else if (jdbcSpace.isShuttingDown()) {
            stmt.close();
        }*/
    }
}
