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
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

public class JDBCSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(JDBCSpace.class);
    private final String spaceName;
    private DataSource ds;
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
        PGSimpleDataSource ds = new PGSimpleDataSource();
        hikariConfig = new HikariConfig();

        Optional<String> url = cfg.getOptional("url");
        if(url.isEmpty()) {
            throw new OpConfigError("url option is required.");
        } else {
            ds.setURL(url.get());
            hikariConfig.setJdbcUrl(url.get());
        }

        Optional<String> serverNames = cfg.getOptional("serverName");
        if(serverNames.isPresent()) {
            ds.setServerNames(new String[]{serverNames.get()});
            //hds.setServerNames(new String[] {serverNames.get()});
            hikariConfig.addDataSourceProperty("serverName", serverNames.get());
        } else {
            throw new OpConfigError("Server name option is required.");
        }

        Optional<String> databaseName = cfg.getOptional("databaseName");
        if(databaseName.isPresent()) {
            ds.setDatabaseName(databaseName.get());
            hikariConfig.addDataSourceProperty("databaseName", databaseName.get());
        } else {
            throw new OpConfigError("Database name option is required.");
        }

        Optional<Integer> portNumber = cfg.getOptional(Integer.class, "portNumber");
        ds.setPortNumbers(new int[] { portNumber.orElse(26257) });
        hikariConfig.addDataSourceProperty("portNumber", portNumber.orElse(26257));

        Optional<String> user = cfg.getOptional("user");
        if(user.isPresent()) {
            ds.setUser(user.get());
            hikariConfig.setUsername(user.get());
        }

        Optional<String> password = cfg.getOptional("password");
        if(password.isPresent()) {
            if(user.isEmpty()) {
                throw new OpConfigError("Both user and password options are required. Only password is supplied in this case.");
            }
            ds.setPassword(password.get());
            hikariConfig.setPassword(password.get());
        } else {
            if(user.isPresent()) {
                throw new OpConfigError("Both user and password options are required. Only user is supplied in this case.");
            }
        }

        Optional<Boolean> ssl = cfg.getOptional(Boolean.class,"ssl");
        if(ssl.isPresent()) {
            ds.setSsl(ssl.get());
            hikariConfig.addDataSourceProperty("ssl", ssl.get());
        } else {
            ds.setSsl(false);
            hikariConfig.addDataSourceProperty("ssl", false);
        }

        Optional<String> sslMode = cfg.getOptional("sslmode");
        if(sslMode.isPresent()) {
            ds.setSslMode(sslMode.get());
            hikariConfig.addDataSourceProperty("sslmode", sslMode.get());
        } else {
            ds.setSslMode("verify-full");
            hikariConfig.addDataSourceProperty("sslmode", "verify-full");
        }

        Optional<String> sslCert = cfg.getOptional("sslcert");
        if(sslCert.isPresent()) {
            ds.setSslcert(sslCert.get());
            hikariConfig.addDataSourceProperty("sslcert", sslCert.get());
        } /*else if(sslMode.isPresent() && (!"disable".equalsIgnoreCase(sslMode.get()) || !"allow".equalsIgnoreCase(sslMode.get())) || !"prefer".equalsIgnoreCase(sslMode.get())) {
            throw new OpConfigError("When sslmode is true, sslcert should be provided.");
        }*/

        Optional<String> sslRootCert = cfg.getOptional("sslrootcert");
        if(sslRootCert.isPresent()) {
            ds.setSslRootCert(sslRootCert.get());
            hikariConfig.addDataSourceProperty("sslrootcert", sslRootCert.get());
        }

        Optional<String> applicationName = cfg.getOptional("applicationName");
        if(applicationName.isPresent()) {
            ds.setApplicationName(applicationName.get());
            hikariConfig.addDataSourceProperty("applicationName", applicationName.orElse("NoSQLBench CRDB"));
        } else {
            ds.setApplicationName("NoSQLBench CRDB");
            hikariConfig.addDataSourceProperty("applicationName", "NoSQLBench CRDB");
        }
        Optional<Boolean> rewriteBatchedInserts = cfg.getOptional(Boolean.class, "rewriteBatchedInserts");
        ds.setReWriteBatchedInserts(rewriteBatchedInserts.orElse(true));
        hikariConfig.addDataSourceProperty("rewriteBatchedInserts", rewriteBatchedInserts.orElse(true));

        Optional<Boolean> autoCommit = cfg.getOptional(Boolean.class, "autoCommit");
        hikariConfig.setAutoCommit(autoCommit.orElse(false));

        Optional<Integer> maximumPoolSize = cfg.getOptional(Integer.class,"maximumPoolSize");
        hikariConfig.setMaximumPoolSize(maximumPoolSize.orElse(40));

        Optional<Integer> keepaliveTime = cfg.getOptional(Integer.class,"keepaliveTime");
        hikariConfig.setKeepaliveTime(keepaliveTime.orElse(150000));

        HikariDataSource hds = new HikariDataSource(hikariConfig);
        try {
            this.connection = hds.getConnection();
        } catch (Exception ex) {
            String exp = "Exception occurred while attempting to create a connection using the Hikari Data Source";
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
        } catch (Exception e) {
            logger.warn("auto-closeable jdbc connection threw exception in jdbc space(" + this.spaceName + "): " + e);
            throw new RuntimeException(e);
        }
    }
}
