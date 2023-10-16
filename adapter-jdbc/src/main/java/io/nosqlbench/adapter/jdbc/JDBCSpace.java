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

package io.nosqlbench.adapter.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterInvalidParamException;
import io.nosqlbench.adapter.jdbc.exceptions.JDBCAdapterUnexpectedException;
import io.nosqlbench.adapter.jdbc.utils.JDBCAdapterUtil;
import io.nosqlbench.adapter.jdbc.utils.JDBCPgVector;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.api.errors.OpConfigError;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class JDBCSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(JDBCSpace.class);
    private final String spaceName;

    // How many JDBC connections per NB JDBC execution
    // NOTE: Since JDBC connection is NOT thread-safe, the total NB threads MUST be less
    //       than or equal to this number. This is to make sure one thread per connection.
    private final static int DEFAULT_CONN_NUM = 5;
    private final int maxNumConn;

    // For DML statements, how many statements to put together in one batch
    // - 1 : no batch (default)
    // - positive number: using batch
    private final static int DEFAULT_DML_BATCH_NUM = 1;
    private final int dmlBatchNum;

    private final long totalCycleNum;
    private static boolean isShuttingDown = false;

    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;

    ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();

    public JDBCSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.totalCycleNum = NumberUtils.toLong(cfg.get("cycles"));

        int totalThreads = NumberUtils.toInt(cfg.getOptional("threads").orElse("1"));
        int numConnInput = NumberUtils.toInt(cfg.getOptional("num_conn").orElse("10"));
        this.maxNumConn = Math.min(totalThreads, numConnInput);
        if (this.maxNumConn < 1) {
            throw new JDBCAdapterInvalidParamException(
                "'num_conn' NB CLI parameter must be a positive number!"
            );
        }

        // Must be after the 'maxNumConn' statements and before the rest of the remaining statements!
        this.initializeSpace(cfg);

        this.dmlBatchNum = NumberUtils.toInt(cfg.getOptional("dml_batch").orElse("1"));
        if (this.dmlBatchNum < 1) {
            throw new JDBCAdapterInvalidParamException(
                "'dml_batch' NB CLI parameter must be a positive number!"
            );
        }
        // According to JDBC spec,
        // - The commit behavior of executeBatch is always implementation-defined
        //   when an error occurs and auto-commit is true.
        //
        // In this adapter, we treat it as an error if 'autoCommit' is ON and using batch at the same time.
        if ( (this.dmlBatchNum > 1) && (hikariConfig.isAutoCommit()) ) {
            throw new JDBCAdapterInvalidParamException(
                "Using batch, 'dml_batch'(" + this.dmlBatchNum + ") > 1, along with 'autoCommit' ON is not supported!"
            );
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} JDBC connections will be created [max(threads/{}, num_conn/{}]; " +
                    "dml_batch: {}, autoCommit: {}",
                maxNumConn, totalThreads, numConnInput, dmlBatchNum, hikariConfig.isAutoCommit());
        }
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public int getMaxNumConn() { return this.maxNumConn; }

    public int getDmlBatchNum() { return this.dmlBatchNum; }

    public long getTotalCycleNum() { return this.totalCycleNum; }

    public boolean isShuttingDown() { return isShuttingDown; }
    public void enterShutdownStage() { isShuttingDown = true; }


    public HikariDataSource getHikariDataSource() {
        return this.hikariDataSource;
    }

    public Connection getConnection(String connectionName) {
        Connection connection = connections.get(connectionName);
        if (connection == null) {
            try {
                connection = hikariDataSource.getConnection();
                if (logger.isDebugEnabled()) {
                    logger.debug("JDBC connection ({}) is successfully created: {}",
                        connectionName, connection);
                }

                // Register 'vector' type
                JDBCPgVector.addVectorType(connection);

                connections.put(connectionName, connection);
            }
            catch (Exception ex) {
                String exp = "Exception occurred while attempting to create a connection using the HikariDataSource";
                logger.error(exp, ex);
                throw new JDBCAdapterUnexpectedException(exp);
            }
        }

        return connection;
    }

    private void initializeSpace(NBConfiguration cfg) {
        hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(cfg.get("url"));
        hikariConfig.addDataSourceProperty("serverName", cfg.get("serverName"));

        Optional<String> databaseName = cfg.getOptional("databaseName");
        if (databaseName.isPresent()) {
            hikariConfig.addDataSourceProperty("databaseName", databaseName.get());
        }

        int portNumber = Integer.parseInt(cfg.get("portNumber"));
        hikariConfig.addDataSourceProperty("portNumber", portNumber);

        Optional<String> user = cfg.getOptional("user");
        if (user.isPresent()) {
            hikariConfig.setUsername(user.get());
        }

        Optional<String> password = cfg.getOptional("password");
        if (password.isPresent()) {
            if (user.isEmpty()) {
                throw new OpConfigError("Both user and password options are required. Only password is supplied in this case.");
            }
            hikariConfig.setPassword(password.get());
        } else {
            if (user.isPresent()) {
                throw new OpConfigError("Both user and password options are required. Only user is supplied in this case.");
            }
        }

        Optional<Boolean> ssl = cfg.getOptional(Boolean.class, "ssl");
        hikariConfig.addDataSourceProperty("ssl", ssl.orElse(false));

        Optional<String> sslMode = cfg.getOptional("sslmode");
        if (sslMode.isPresent()) {
            hikariConfig.addDataSourceProperty("sslmode", sslMode.get());
        } else {
            hikariConfig.addDataSourceProperty("sslmode", "prefer");
        }

        Optional<String> sslCert = cfg.getOptional("sslcert");
        if (sslCert.isPresent()) {
            hikariConfig.addDataSourceProperty("sslcert", sslCert.get());
        } /*else if(sslMode.isPresent() && (!"disable".equalsIgnoreCase(sslMode.get()) || !"allow".equalsIgnoreCase(sslMode.get())) || !"prefer".equalsIgnoreCase(sslMode.get())) {
            throw new OpConfigError("When sslmode is true, sslcert should be provided.");
        }*/

        Optional<String> sslRootCert = cfg.getOptional("sslrootcert");
        if (sslRootCert.isPresent()) {
            hikariConfig.addDataSourceProperty("sslrootcert", sslRootCert.get());
        }

        hikariConfig.addDataSourceProperty("applicationName", cfg.get("applicationName"));
        hikariConfig.addDataSourceProperty("rewriteBatchedInserts", cfg.getOrDefault("rewriteBatchedInserts", true));

        // We're managing the auto-commit behavior of connections ourselves and hence disabling the auto-commit.
        Optional<String> autoCommitOpt = cfg.getOptional("autoCommit");
        boolean autoCommit = false;
        if (autoCommitOpt.isPresent()) autoCommit = BooleanUtils.toBoolean(autoCommitOpt.get());
        hikariConfig.setAutoCommit(autoCommit);

        hikariConfig.setKeepaliveTime(Integer.parseInt(cfg.get("keepaliveTime")));

        // HikariCP "maximumPoolSize" parameter is ignored.
        // Use the NB "num_conn" parameter instead, wth 20% extra capacity
        hikariConfig.setMaximumPoolSize((int)Math.ceil(1.2*maxNumConn));

        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    private void shutdownSpace() {
        isShuttingDown = true;

        try {
            waitUntilAllOpFinished(System.currentTimeMillis());

            for (Connection connection : connections.values()) {
                connection.close();
            }
        } catch (Exception e) {
            throw new JDBCAdapterUnexpectedException("Unexpected error when trying to close the JDBC connection!");
        }

        hikariDataSource.close();
    }

    private void waitUntilAllOpFinished(long shutdownStartTimeMills) {
        final int timeToWaitInSec = 5;
        long timeElapsedMills;
        boolean continueChk;

        do {
            JDBCAdapterUtil.pauseCurThreadExec(1);

            long curTimeMills = System.currentTimeMillis();
            timeElapsedMills = curTimeMills - shutdownStartTimeMills;
            continueChk = (timeElapsedMills <= (timeToWaitInSec*1000));
        } while (continueChk);

        logger.info(
            "shutdownSpace::waitUntilAllOpFinished -- " +
            "shutdown time elapsed: " + timeElapsedMills + "ms.");
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(JDBCSpace.class)
            .add(Param.defaultTo("num_conn", DEFAULT_CONN_NUM)
                .setDescription("The number of JDBC connections to establish. Defaults to '" + DEFAULT_CONN_NUM + "'"))
            .add(Param.defaultTo("dml_batch", DEFAULT_DML_BATCH_NUM)
                .setDescription("The number of DML write statements in a batch. Defaults to 1. Ignored by DML read statements!" +
                    DEFAULT_DML_BATCH_NUM + "' (no batch)"))
            .add(Param.defaultTo("url", "jdbc:postgresql:/")
                .setDescription("The connection URL used to connect to the DBMS. Defaults to 'jdbc:postgresql:/'"))
            .add(Param.defaultTo("url", "jdbc:postgresql:/")
                .setDescription("The connection URL used to connect to the DBMS. Defaults to 'jdbc:postgresql:/'"))
            .add(Param.defaultTo("serverName", "localhost")
                .setDescription("The host name of the server. Defaults to 'localhost'"))
            .add(Param.optional("databaseName")
                .setDescription("The database name. The default is to connect to a database with the same name as the user name used to connect to the server."))
            // See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby & https://jdbc.postgresql.org/documentation/use/
            .add(Param.defaultTo("portNumber", "5432")
                .setDescription("The port number the server is listening on. Defaults to the PostgreSQL® standard port number (5432)."))
            .add(Param.optional("user")
                .setDescription("The database user on whose behalf the connection is being made."))
            .add(Param.optional("password")
                .setDescription("The database user’s password."))
            .add(Param.optional("ssl")
                .setDescription("Whether to connect using SSL. Default is false."))
            .add(Param.optional("sslmode")
                .setDescription("Possible values include disable , allow , prefer , require , verify-ca and verify-full." +
                    " require , allow and prefer all default to a non-validating SSL factory and do not check the validity of the certificate or the host name." +
                    " verify-ca validates the certificate, but does not verify the hostname." +
                    " verify-full will validate that the certificate is correct and verify the host connected to has the same hostname as the certificate." +
                    " Default is prefer."))
            .add(Param.optional("sslcert")
                .setDescription("Provide the full path for the certificate file. Defaults to defaultdir/postgresql.crt, where defaultdir is ${user.home}/.postgresql/ in *nix systems and %appdata%/postgresql/ on windows."))
            .add(Param.optional("sslrootcert")
                .setDescription("File name of the SSL root certificate."))
            .add(Param.defaultTo("applicationName", "NoSQLBench")
                .setDescription("The application name to be used. Default is 'NoSQLBench'."))
            .add(Param.optional("rewriteBatchedInserts")
                .setDescription("This will change batch inserts from insert into foo (col1, col2, col3) values (1, 2, 3) into insert into foo (col1, col2, col3) values (1, 2, 3), (4, 5, 6) this provides 2-3x performance improvement. " +
                    "Default is true"))
            .add(Param.optional("autoCommit")
                .setDescription("This property controls the default auto-commit behavior of connections returned from the pool. " +
                    "It is a boolean value. Default: false. This cannot be changed."))
            .add(Param.optional("connectionTimeout")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("idleTimeout")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.defaultTo("keepaliveTime", "150000")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("maxLifetime")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("connectionTestQuery")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("minimumIdle")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.defaultTo("maximumPoolSize", "40")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. Default value is 40 and cannot be changed."))
            .add(Param.optional("metricRegistry")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("healthCheckRegistry")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("poolName")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("initializationFailTimeout")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("isolateInternalQueries")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("allowPoolSuspension")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("readOnly")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("registerMbeans")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("catalog")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("connectionInitSql")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("driverClassName")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("transactionIsolation")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("validationTimeout")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("leakDetectionThreshold")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("dataSource")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("schema")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("threadFactory")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .add(Param.optional("scheduledExecutor")
                .setDescription("See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. " +
                    "This property is not exposed and hence cannot be changed."))
            .asReadOnly();
    }
}
