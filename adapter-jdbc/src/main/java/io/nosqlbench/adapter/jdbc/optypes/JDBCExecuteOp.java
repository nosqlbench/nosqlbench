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
    private static final Logger logger = LogManager.getLogger(JDBCExecuteOp.class);
    private static final String LOG_COMMIT_SUCCESS = "Executed the JDBC statement & committed the connection successfully";
    private final String LOG_QUERY_STRING;
    private final String LOG_SELECT_QUERY_STRING;
    private int finalResultCount;
    private String LOG_ROWS_PROCESSED = "Total number of rows processed is [" + finalResultCount + "]";

    public JDBCExecuteOp(Connection connection, Statement statement, String queryString) {
        super(connection, statement, queryString);
        LOG_QUERY_STRING = "JDBC Query is: " + this.getQueryString();
        LOG_SELECT_QUERY_STRING = "Executed a SELECT operation [" + LOG_QUERY_STRING + "]";
        if (logger.isDebugEnabled()) {
            logger.debug(() -> LOG_QUERY_STRING);
        }
    }

    @Override
    public void run() {
        try {
            boolean isResultSet = this.getStatement().execute(this.getQueryString());

            if (isResultSet) {
                logger.debug(() -> LOG_SELECT_QUERY_STRING);
            } else if (!isResultSet) {
                logger.debug(() -> {
                    try {
                        return "Executed a normal DDL/DML (non-SELECT) operation. Objects affected is [" + this.getStatement().getUpdateCount() + "]";
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

                finalResultCount = countResults;
                logger.debug(() -> LOG_ROWS_PROCESSED);
            }
            this.getConnection().commit();
            logger.debug(() -> LOG_COMMIT_SUCCESS);
        } catch (SQLException sqlException) {
            String exMsg = String.format("ERROR: [ state => %s, cause => %s, message => %s ]",
                sqlException.getSQLState(), sqlException.getCause(), sqlException.getMessage());
            logger.error(exMsg, sqlException);
            throw new RuntimeException(exMsg, sqlException);
        } catch (Exception ex) {
            String exMsg = String.format("Exception while attempting to run the jdbc statement %s", getStatement());
            logger.error(exMsg, ex);
            throw new RuntimeException(exMsg, ex);
        }
    }
}
