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
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import io.nosqlbench.api.errors.OpConfigError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.Optional;

public class JDBCSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(JDBCSpace.class);
    private final String spaceName;
    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;
    private Connection connection;

    public JDBCSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.hikariDataSource = createClient(cfg);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public HikariDataSource getHikariDataSource() {
        return this.hikariDataSource;
    }

    private HikariDataSource createClient(NBConfiguration cfg) {
        hikariConfig = new HikariConfig();

        Optional<String> url = cfg.getOptional("url");
        if (url.isEmpty()) {
            throw new OpConfigError("url option is required.");
        } else {
            hikariConfig.setJdbcUrl(url.get());
        }

        Optional<String> serverNames = cfg.getOptional("serverName");
        if (serverNames.isPresent()) {
            hikariConfig.addDataSourceProperty("serverName", serverNames.get());
        } else {
            throw new OpConfigError("Server name option is required.");
        }

        Optional<String> databaseName = cfg.getOptional("databaseName");
        if (databaseName.isPresent()) {
            hikariConfig.addDataSourceProperty("databaseName", databaseName.get());
        } else {
            throw new OpConfigError("Database name option is required.");
        }

        int portNumber = Integer.parseInt(cfg.getOptional("portNumber").orElse("26257"));
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
                // Maybe simply ignore user and move on as opposed to throwing an error?
                //logger.warn(() -> "Both user and password options are required. Only user is supplied in this case and it will be ignored.");
            }
        }

        Optional<Boolean> ssl = cfg.getOptional(Boolean.class, "ssl");
        if (ssl.isPresent()) {
            hikariConfig.addDataSourceProperty("ssl", ssl.get());
        } else {
            hikariConfig.addDataSourceProperty("ssl", false);
        }

        Optional<String> sslMode = cfg.getOptional("sslmode");
        if (sslMode.isPresent()) {
            hikariConfig.addDataSourceProperty("sslmode", sslMode.get());
        } else {
            hikariConfig.addDataSourceProperty("sslmode", "verify-full");
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

        Optional<String> applicationName = cfg.getOptional("applicationName");
        hikariConfig.addDataSourceProperty("applicationName", applicationName.orElse("NoSQLBench"));

        Optional<Boolean> rewriteBatchedInserts = cfg.getOptional(Boolean.class, "rewriteBatchedInserts");
        hikariConfig.addDataSourceProperty("rewriteBatchedInserts", rewriteBatchedInserts.orElse(true));

        //Maybe always disable auto-commit since we manage ourselves?
        Optional<Boolean> autoCommit = cfg.getOptional(Boolean.class, "autoCommit");
        hikariConfig.setAutoCommit(autoCommit.orElse(false));

        int maximumPoolSize = Integer.parseInt(cfg.getOptional("maximumPoolSize").orElse("40"));
        hikariConfig.setMaximumPoolSize(maximumPoolSize);

        int keepaliveTime = Integer.parseInt(cfg.getOptional("keepaliveTime").orElse("150000"));
        hikariConfig.setKeepaliveTime(keepaliveTime);

        HikariDataSource hds = new HikariDataSource(hikariConfig);
        try {
            this.connection = hds.getConnection();
        } catch (Exception ex) {
            String exp = "Exception occurred while attempting to create a connection using the HikariDataSource";
            logger.error(exp, ex);
            throw new RuntimeException(exp, ex);
        }

        return hds;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(JDBCSpace.class)
            .add(Param.defaultTo("url", "jdbc:postgresql:/").setDescription("The connection URL used to connect to the DBMS. Defaults to 'jdbc:postgresql:/'"))
            .add(Param.defaultTo("serverName", "localhost").setDescription("The host name of the server. Defaults to 'localhost'"))
            .add(Param.optional("databaseName").setDescription("The database name. The default is to connect to a database with the same name as the user name used to connect to the server."))
            // See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby & https://jdbc.postgresql.org/documentation/use/
            .add(Param.defaultTo("portNumber", 5432).setDescription("The port number the server is listening on. Defaults to the PostgreSQL® standard port number (5432)"))
            .add(Param.optional("user"))
            .add(Param.optional("password"))
            .add(Param.optional("ssl"))
            .add(Param.optional("sslmode"))
            .add(Param.optional("sslcert"))
            .add(Param.optional("sslrootcert"))
            .add(Param.optional("applicationName"))
            .add(Param.optional("rewriteBatchedInserts"))
            .add(Param.optional("autoCommit"))
            .add(Param.optional("connectionTimeout"))
            .add(Param.optional("idleTimeout"))
            .add(Param.optional("keepaliveTime"))
            .add(Param.optional("maxLifetime"))
            .add(Param.optional("connectionTestQuery"))
            .add(Param.optional("minimumIdle"))
            .add(Param.optional("maximumPoolSize"))
            .add(Param.optional("metricRegistry"))
            .add(Param.optional("healthCheckRegistry"))
            .add(Param.optional("poolName"))
            .add(Param.optional("initializationFailTimeout"))
            .add(Param.optional("isolateInternalQueries"))
            .add(Param.optional("allowPoolSuspension"))
            .add(Param.optional("readOnly"))
            .add(Param.optional("registerMbeans"))
            .add(Param.optional("catalog"))
            .add(Param.optional("connectionInitSql"))
            .add(Param.optional("driverClassName"))
            .add(Param.optional("transactionIsolation"))
            .add(Param.optional("validationTimeout"))
            .add(Param.optional("leakDetectionThreshold"))
            .add(Param.optional("dataSource"))
            .add(Param.optional("schema"))
            .add(Param.optional("threadFactory"))
            .add(Param.optional("scheduledExecutor"))
            .asReadOnly();
    }

    @Override
    public void close() {
        try {
            this.getConnection().close();
            this.getHikariDataSource().close();
        } catch (Exception e) {
            logger.error("auto-closeable jdbc connection threw exception in jdbc space(" + this.spaceName + "): " + e);
            throw new RuntimeException(e);
        }
    }
}
