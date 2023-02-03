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
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.RunnableOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
 * @see <a href="https://github.com/brettwooldridge/HikariCP">HikariCP connection pooling</a> for details.
 */
public /*abstract*/ class JDBCOp implements RunnableOp/*CycleOp<Object>*/ {
    private final static Logger logger = LogManager.getLogger(JDBCOp.class);
    protected DataSource dataSource;
    private final JDBCSpace jdbcSpace;

    public String getTargetStatement() {
        return targetStatement;
    }

    private final String targetStatement;

    public DataSource getDataSource() {
        return dataSource;
    }

    public JDBCSpace getJdbcSpace() {
        return jdbcSpace;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }

    private final Connection connection;
    private final Statement statement;

    /**
     * Unused.
     * @param jdbcSpace
     * @param targetStatement
     */
    public JDBCOp(JDBCSpace jdbcSpace, String targetStatement) {
        //TODO - implement code
        //this.dataSource = new HikariDataSource();
        this.jdbcSpace = jdbcSpace;
        this.targetStatement = targetStatement;
        this.connection = null;
        this.statement = null;
    }

    /**
     *
     * @param connection
     * @param statement
     * @param targetStatement
     */
    public JDBCOp(Connection connection, Statement statement, String targetStatement) {
        this.connection = connection;
        this.statement = statement;
        this.targetStatement = targetStatement;
        this.jdbcSpace = null;
    }

    @Override
    public void run() {
        logger.info(() -> "Executing JDBCOp for the given cycle.");
        try {
            this.getConnection().setAutoCommit(false);
            if(logger.isDebugEnabled()) {
                logger.debug(() -> "JDBC Query is: " + this.getTargetStatement());
            }
            this.getStatement().execute(this.getTargetStatement());
            this.getConnection().commit();
            logger.info(() -> "Executed the JDBC statement & committed the connection successfully");
        } catch (SQLException sqlException) {
            logger.error("JDBCOp ERROR: { state => %s, cause => %s, message => %s }\n",
                sqlException.getSQLState(), sqlException.getCause(), sqlException.getMessage(), sqlException);
            throw new RuntimeException(sqlException);
        } catch (Exception ex) {
            String exMsg = String.format("Exception while attempting to run the jdbc statement %s", getStatement());
            logger.error(exMsg, ex);
            throw new RuntimeException(exMsg, ex);
        }
    }
}
