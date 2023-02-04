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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCExecuteOp extends JDBCOp {
    private final static Logger logger = LogManager.getLogger(JDBCExecuteOp.class);

    public JDBCExecuteOp(Connection connection, Statement statement, String queryString) {
        super(connection, statement, queryString);
    }

    @Override
    public void run() {
        logger.debug(() -> "Executing JDBCExecuteOp for the given cycle.");
        try {
            this.getConnection().setAutoCommit(false);
            if (logger.isDebugEnabled()) {
                logger.debug(() -> "JDBC Query is: " + this.getQueryString());
            }
            boolean isResultSet = this.getStatement().execute(this.getQueryString());
            if (isResultSet) {
                logger.debug(() -> ">>>>>>>>>>Executed a SELECT operation [" + this.getQueryString() + "]<<<<<<<<<<");
            } else if (!isResultSet) {
                logger.debug(() -> {
                    try {
                        return ">>>>>>>>>>Executed a normal DDL/DML (non-SELECT) operation. Objects affected is [" + this.getStatement().getUpdateCount() + "]<<<<<<<<<<";
                    } catch (SQLException e) {
                        String err_msg = "Exception occurred while attempting to fetch the update count of the query operation";
                        logger.error(err_msg, e);
                        throw new RuntimeException(err_msg, e);
                    }
                });
            } else if (logger.isDebugEnabled()) {
                int countResults = 0;
                ResultSet rs = this.getStatement().getResultSet();
                countResults += rs.getRow();
                while (!this.getStatement().getMoreResults() && 0 < this.getStatement().getUpdateCount()) {
                    rs = this.getStatement().getResultSet();
                    countResults += rs.getRow();
                    //rs.close(); Optional as getMoreResults() will already close it.
                }
                int finalCountResults = countResults;
                logger.debug(() -> ">>>>>>>>>>Total number of rows processed is [" + finalCountResults + "]<<<<<<<<<<");
            }
            this.getConnection().commit();
            logger.debug(() -> "Executed the JDBC statement & committed the connection successfully");
        } catch (SQLException sqlException) {
            logger.error("ERROR: { state => {}, cause => {}, message => {} }\n",
                sqlException.getSQLState(), sqlException.getCause(), sqlException.getMessage(), sqlException);
            throw new RuntimeException(sqlException);
        } catch (Exception ex) {
            String exMsg = String.format("Exception while attempting to run the jdbc statement %s", getStatement());
            logger.error(exMsg, ex);
            throw new RuntimeException(exMsg, ex);
        }
    }
}
