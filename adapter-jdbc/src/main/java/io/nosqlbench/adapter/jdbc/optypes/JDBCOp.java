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

import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.RunnableOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.Statement;

/**
 * References:
 * https://docs.oracle.com/javase/tutorial/jdbc/basics/gettingstarted.html
 * https://docs.oracle.com/javase/17/docs/api/java/sql/package-summary.html
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.sql/java/sql/package-summary.html
 * https://jdbc.postgresql.org/documentation/query/
 * https://www.cockroachlabs.com/docs/v22.2/connection-pooling.html
 * https://www.cockroachlabs.com/docs/v22.2/connection-parameters#supported-options-parameters
 * https://www.cockroachlabs.com/docs/v22.2/sql-statements.html#query-management-statements
 * https://docs.yugabyte.com/preview/drivers-orms/java/yugabyte-jdbc/
 *
 * @see <a href="https://github.com/brettwooldridge/HikariCP">HikariCP connection pooling</a> for details.
 */
public abstract class JDBCOp implements RunnableOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCOp.class);
    protected static final String LOG_COMMIT_SUCCESS = "Executed the JDBC statement & committed the connection successfully";

    protected final String LOG_GENERIC_ERROR;
    protected final Connection connection;
    protected final Statement statement;
    protected final String queryString;

    protected int finalResultCount;
    protected String LOG_ROWS_PROCESSED = "Total number of rows processed is [" + finalResultCount + "]";

    /**
     * @param connection
     * @param statement
     * @param queryString
     */
    public JDBCOp(Connection connection, Statement statement, String queryString) {
        this.connection = connection;
        this.statement = statement;
        this.queryString = queryString;
        LOG_GENERIC_ERROR = String.format("Exception while attempting to run the jdbc query %s", queryString);
        LOGGER.debug(() -> "Query to be executed: " + queryString);
    }
}
