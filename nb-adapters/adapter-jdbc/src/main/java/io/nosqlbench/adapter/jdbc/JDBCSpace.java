/*
 * Copyright (c) nosqlbench
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
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
import io.nosqlbench.adapters.api.activityimpl.uniform.ConcurrentIndexCache;
import io.nosqlbench.engine.core.lifecycle.session.ClasspathExtender;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.nb.api.errors.OpConfigError;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class JDBCSpace extends BaseSpace<JDBCSpace> {
  private final static Logger logger = LogManager.getLogger(JDBCSpace.class);

  // How many JDBC connections per NB JDBC execution
  // NOTE: Since JDBC connection is NOT thread-safe, the total NB threads MUST be less
  //       than or equal to this number. This is to make sure one thread per connection.
  private final static int DEFAULT_CONN_NUM = 5;
  private int maxNumConn = DEFAULT_CONN_NUM;

  // For DML write statements, how many statements to put together in one batch
  // - 1 : no batch (default)
  // - positive number: using batch
  private final static int DEFAULT_DML_BATCH_NUM = 1;
  private int dmlBatchNum = DEFAULT_DML_BATCH_NUM;

  private long totalCycleNum;
  private int totalThreadNum;
  private boolean autoCommitCLI;

  private boolean useHikariCP;
  private final HikariConfig connConfig = new HikariConfig();
  private HikariDataSource hikariDataSource;

  // Maintain a client-side pooling just to make sure the allocated connections can
  // be reclaimed quickly, instead of waiting for Hikari pooling to reclaim it eventually
  public record ConnectionCacheKey(String connName) {
  }

  private final ConcurrentHashMap<ConnectionCacheKey, Connection> connections =
      new ConcurrentHashMap<>();

  // Prepared statement cache using refKey for each operation template
  private final ConcurrentIndexCache<PreparedStatement> preparedStmtCache =
      new ConcurrentIndexCache<>("jdbc_pstmts");

  public JDBCSpace(JDBCDriverAdapter adapter, long spaceidx, NBConfiguration cfg) {
    super(adapter, spaceidx);
    this.initializeSpace(cfg);
//    new ClasspathExtender().extend();

    // In this adapter, we treat it as an error if 'autoCommit' is ON and using batch at the same time.
    if ((this.dmlBatchNum > 1) && isAutoCommit()) {
      throw new JDBCAdapterInvalidParamException("Using batch, 'dml_batch'(" + this.dmlBatchNum
                                                 + ") > 1, along with 'autoCommit' ON is not supported!");
    }
  }

  @Override
  public void close() {
    try {
      preparedStmtCache.close();
    } catch (Exception e) {
      logger.warn("Error closing prepared statement cache", e);
    }
    shutdownSpace();
  }

  public int getMaxNumConn() {
    return maxNumConn;
  }

  public void setMaxNumConn(int i) {
    maxNumConn = i;
  }

  public int getDmlBatchNum() {
    return dmlBatchNum;
  }

  public long getTotalCycleNum() {
    return totalCycleNum;
  }

  public int getTotalThreadNum() {
    return totalThreadNum;
  }

  public boolean isAutoCommit() {
    if (useHikariCP)
      return connConfig.isAutoCommit();
    else
      return this.autoCommitCLI;
  }

  public boolean useHikariCP() {
    return useHikariCP;
  }

  public HikariConfig getConnConfig() {
    return connConfig;
  }


  public HikariDataSource getHikariDataSource() {
    return hikariDataSource;
  }

  public Connection getConnection(ConnectionCacheKey key, Supplier<Connection> connectionSupplier) {
    return connections.computeIfAbsent(key, __ -> connectionSupplier.get());
  }

  public PreparedStatement getOrCreatePreparedStatement(
      int refkey,
      LongFunction<PreparedStatement> psF
  ) {
    return preparedStmtCache.get(refkey, psF);
  }

  private void initializeSpace(NBConfiguration cfg) {
    //
    // NOTE: Although it looks like a good idea to use Hikari Connection Pooling
    //       But in my testing, it shows some strange behaviors such as
    //       1) failed to allocate connection while the target server is completely working fine
    //          e.g. it failed consistently on a m5d.4xlarge testing bed but not on my mac.
    //       2) doesn't really respect the 'max_connections' setting
    //       3) it also appears to me that Hikari connection is slow
    //
    // Therefore, use `use_hikaricp` option to control whether to use Hikari connection pooling. When
    // setting to 'false' (as default), it uses JDBC adapter's own (simple) connection management, with
    // JDBC driver's `DriverManager` to create connection directly.
    //

//    new ClasspathExtender().extend();

    this.useHikariCP = BooleanUtils.toBoolean(cfg.getOptional("use_hikaricp").orElse("true"));
    this.autoCommitCLI = BooleanUtils.toBoolean(cfg.getOptional("autoCommit").orElse("true"));
    this.dmlBatchNum = NumberUtils.toInt(cfg.getOptional("dml_batch").orElse("1"));
    if (this.dmlBatchNum < 0)
      dmlBatchNum = 1;
    logger.info(
        "CLI input parameters -- useHikariCP:{}, autoCommitCLI:{}, dmlBatchNum:{}",
        useHikariCP,
        autoCommitCLI,
        dmlBatchNum
    );

    this.totalCycleNum = NumberUtils.toLong(cfg.getOptional("cycles").orElse("1"));
    this.totalThreadNum = NumberUtils.toInt(cfg.getOptional("threads").orElse("1"));

    connConfig.setJdbcUrl(cfg.get("url"));

    cfg.getOptional("serverName")
        .ifPresent(serverName -> connConfig.addDataSourceProperty("serverName", serverName));

    cfg.getOptional("databaseName")
        .ifPresent(dbn -> connConfig.addDataSourceProperty("databaseName", dbn));

    cfg.getOptional("portNumber").map(Integer::parseInt)
        .ifPresent(pn -> connConfig.addDataSourceProperty("portNumber", String.valueOf(pn)));

    cfg.getOptional("user").ifPresent(user -> connConfig.setUsername(user));

    cfg.getOptional("password").ifPresent(password -> {
      if (connConfig.getUsername() == null || connConfig.getUsername().isEmpty()) {
        throw new OpConfigError(
            "Both user and password options are required. Only password is supplied in this case.");
      }
      connConfig.setPassword(password);
    });

    cfg.getOptional(Boolean.class, "ssl")
        .ifPresent(ssl -> connConfig.addDataSourceProperty("ssl", String.valueOf(ssl)));

    cfg.getOptional("sslmode")
        .ifPresent(sslmode -> connConfig.addDataSourceProperty("sslmode", sslmode));

    cfg.getOptional("sslcert")
        .ifPresent(sslCert -> connConfig.addDataSourceProperty("sslcert", sslCert));

    cfg.getOptional("sslrootcert")
        .ifPresent(src -> connConfig.addDataSourceProperty("sslrootcert", src));

    cfg.getOptional("applicationName")
        .ifPresent(an -> connConfig.addDataSourceProperty("applicationName", an));

    cfg.getOptional("rewriteBatchedInserts")
        .ifPresent(rbi -> connConfig.addDataSourceProperty("rewriteBatchedInserts", rbi));

    cfg.getOptional("keepaliveTime")
        .ifPresent(kat -> connConfig.setKeepaliveTime(Integer.parseInt(kat)));

    // Use the NB "num_conn" parameter instead, wth 20% extra capacity
    connConfig.setMaximumPoolSize((int) Math.ceil(1.2 * maxNumConn));

    if (useHikariCP) {
      this.hikariDataSource = new HikariDataSource(connConfig);
      logger.info("hikariDataSource is created : {}", hikariDataSource);
    }
  }

  private void shutdownSpace() {
    try {
      logger.info(
          "Shutting down JDBCSpace -- total {} of connections is being closed ...",
          connections.size()
      );
      for (Connection connection : connections.values()) {
        if (logger.isDebugEnabled()) {
          logger.debug("Close connection : {}", connection);
        }
        connection.close();
      }
    } catch (Exception e) {
      throw new JDBCAdapterUnexpectedException(
          "Unexpected error when trying to close the JDBC connection!");
    }

    if (hikariDataSource != null) {
      hikariDataSource.close();
    }
  }

  public static NBConfigModel getConfigModel() {
    return ConfigModel.of(JDBCSpace.class).add(Param.defaultTo("num_conn", DEFAULT_CONN_NUM)
            .setDescription(
                "The number of JDBC connections to establish. Defaults to '" + DEFAULT_CONN_NUM + "'"))
        .add(Param.optional("dml_batch").setDescription(
            "The number of DML write statements in a batch. Defaults to 1. Ignored by DML read statements!"
            + DEFAULT_DML_BATCH_NUM + "' (no batch)")).add(Param.defaultTo("use_hikaricp", "true")
            .setDescription("Whether to use Hikari connection pooling (default: true)!"))
        .add(Param.defaultTo("url", "jdbc:postgresql:/").setDescription(
            "The connection URL used to connect to the DBMS. Defaults to 'jdbc:postgresql:/'"))
        .add(Param.optional("serverName")
            .setDescription("The host name of the server. Defaults to 'localhost'"))
        .add(Param.optional("databaseName").setDescription(
            "The database name. The default is to connect to a database with the same name as the user name used to connect to the server."))
        // See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby & https://jdbc.postgresql.org/documentation/use/
        .add(Param.optional("portNumber").setDescription(
            "The port number the server is listening on. Defaults to the PostgreSQL® standard port number (5432)."))
        .add(Param.optional("user")
            .setDescription("The database user on whose behalf the connection is being made."))
        .add(Param.optional("password").setDescription("The database user’s password."))
        .add(Param.optional("ssl")
            .setDescription("Whether to connect using SSL. Default is false."))
        .add(Param.optional("sslmode").setDescription(
            "Possible values include disable , allow , prefer , require , verify-ca and verify-full."
            + " require , allow and prefer all default to a non-validating SSL factory and do not check the validity of the certificate or the host name."
            + " verify-ca validates the certificate, but does not verify the hostname."
            + " verify-full will validate that the certificate is correct and verify the host connected to has the same hostname as the certificate."
            + " Default is prefer.")).add(Param.optional("sslcert").setDescription(
            "Provide the full path for the certificate file. Defaults to defaultdir/postgresql.crt, where defaultdir is ${user.home}/.postgresql/ in *nix systems and %appdata%/postgresql/ on windows."))
        .add(Param.optional("sslrootcert").setDescription("File name of the SSL root certificate."))
        .add(Param.optional("applicationName")
            .setDescription("The application name to be used. Default is 'NoSQLBench'."))
        .add(Param.optional("rewriteBatchedInserts").setDescription(
            "This will change batch inserts from insert into foo (col1, col2, col3) values (1, 2, 3) into insert into foo (col1, col2, col3) values (1, 2, 3), (4, 5, 6) this provides 2-3x performance improvement. "
            + "Default is true")).add(Param.optional("autoCommit").setDescription(
            "This property controls the default auto-commit behavior of connections returned from the pool. "
            + "It is a boolean value. Default: false. This cannot be changed."))
        .add(Param.optional("connectionTimeout").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("idleTimeout").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.defaultTo("keepaliveTime", "150000").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("maxLifetime").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("connectionTestQuery").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("minimumIdle").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("maximumPoolSize").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. Default value is 40 and cannot be changed."))
        .add(Param.optional("metricRegistry").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("healthCheckRegistry").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("poolName").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("initializationFailTimeout").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("isolateInternalQueries").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("allowPoolSuspension").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("readOnly").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("registerMbeans").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("catalog").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("connectionInitSql").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("driverClassName").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("transactionIsolation").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("validationTimeout").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("leakDetectionThreshold").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("dataSource").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("schema").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("threadFactory").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed."))
        .add(Param.optional("scheduledExecutor").setDescription(
            "See https://github.com/brettwooldridge/HikariCP/tree/dev#gear-configuration-knobs-baby for details. "
            + "This property is not exposed and hence cannot be changed.")).asReadOnly();
  }
}
