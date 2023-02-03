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

public class JDBCExecuteQueryOp extends JDBCOp {
    private final static Logger logger = LogManager.getLogger(JDBCExecuteQueryOp.class);

    public JDBCExecuteQueryOp(Connection connection, Statement statement, String queryString) {
        super(connection, statement, queryString);
    }

    @Override
    public void run() {
        logger.debug(() -> "Executing JDBCExecuteQueryOp for the given cycle.");

        try {
            this.getConnection().setAutoCommit(false);
            logger.debug(() -> "JDBC Query is: " + this.getQueryString());
            ResultSet resultSet = this.getStatement().executeQuery(this.getQueryString());
            this.getConnection().commit();
            logger.debug(() -> "Executed the JDBC statement & committed the connection successfully fetching a total of "+ resultSet.toString() + " records.");
        } catch (SQLException sqlException) {
            logger.error("JDBCExecuteQueryOp ERROR: { state => {}, cause => {}, message => {} }\n",
                sqlException.getSQLState(), sqlException.getCause(), sqlException.getMessage(), sqlException);
            throw new RuntimeException(sqlException);
        } catch (Exception ex) {
            String exMsg = String.format("Exception while attempting to run the jdbc statement %s", getStatement());
            logger.error(exMsg, ex);
            throw new RuntimeException(exMsg, ex);
        }
    }
}
