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
import io.nosqlbench.adapters.api.activityimpl.uniform.flowtypes.CycleOp;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Properties;
import java.util.Random;

public abstract class JDBCOp implements CycleOp {
    private static final Logger LOGGER = LogManager.getLogger(JDBCOp.class);
    protected static final String LOG_COMMIT_SUCCESS =
        "Executed the JDBC statement & committed the connection successfully";

    protected final JDBCSpace jdbcSpace;
    protected final Connection jdbcConnection;
    private final Random random = new Random();

    public JDBCOp(JDBCSpace jdbcSpace) {
        this.jdbcSpace = jdbcSpace;
        this.jdbcConnection = getConnection();
    }

    private Connection getConnection() {
        int rnd = random.nextInt(0, jdbcSpace.getMaxNumConn());
        final String connectionName = "jdbc-conn-" + rnd;

        return jdbcSpace.getConnection(
            new JDBCSpace.ConnectionCacheKey(connectionName), () -> {
            try {
                Connection connection;

                if (jdbcSpace.useHikariCP()) {
                    connection = jdbcSpace.getHikariDataSource().getConnection();
                }
                // Use DriverManager directly
                else {
                    String url = jdbcSpace.getConnConfig().getJdbcUrl();
                    Properties props = jdbcSpace.getConnConfig().getDataSourceProperties();
                    props.put("user", jdbcSpace.getConnConfig().getUsername());
                    props.put("password", jdbcSpace.getConnConfig().getPassword());
                    connection = DriverManager.getConnection(url, props);
                }

                // Register 'vector' type
                JDBCPgVector.addVectorType(connection);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("A new JDBC connection ({}) is successfully created: {}",
                        connectionName, connection);
                }

                return  connection;
            }
            catch (Exception ex) {
                String exp = "Exception occurred while attempting to create a connection (useHikariCP=" +
                    jdbcSpace.useHikariCP() + ")";
                LOGGER.error(exp, ex);
                throw new JDBCAdapterUnexpectedException(exp);
            }
        });
    }
}
