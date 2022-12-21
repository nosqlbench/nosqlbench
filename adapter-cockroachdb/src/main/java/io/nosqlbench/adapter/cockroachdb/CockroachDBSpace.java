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

package io.nosqlbench.adapter.cockroachdb;

import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.errors.OpConfigError;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Optional;

public class CockroachDBSpace {
    private final String name;
    private final DataSource ds = null;
//    private final HikariConfig hikariConfig = null;
//    private final HikariDataSource hikariDataSource = null;
    private Connection connection;

    public CockroachDBSpace(String name, NBConfiguration cfg) {
        this.name = name;
        PGSimpleDataSource client = createClient(cfg);
//        dynamoDB= new DynamoDB(client);
    }
    private PGSimpleDataSource createClient(NBConfiguration cfg) {
        PGSimpleDataSource ds = new PGSimpleDataSource();

        Optional<String> url = cfg.getOptional("url");
        if(url.isEmpty()) {
            throw new OpConfigError("url option is required.");
        } else {
            ds.setURL(url.get());
        }

        Optional<String> serverNames = cfg.getOptional("serverName");
        if(serverNames.isPresent()) {
            ds.setServerNames(new String[]{serverNames.get()});
        } else {
            throw new OpConfigError("Server name option is required.");
        }

        Optional<String> databaseName = cfg.getOptional("databaseName");
        if(databaseName.isPresent()) {
            ds.setDatabaseName(databaseName.get());
        } else {
            throw new OpConfigError("Database name option is required.");
        }

        Optional<Integer> portNumber = cfg.getOptional(Integer.class, "portNumber");
        ds.setPortNumbers(new int[] { portNumber.orElse(26257) });

        Optional<String> user = cfg.getOptional("user");
        if(user.isPresent()) {
            ds.setUser(user.get());
        }

        Optional<String> password = cfg.getOptional("password");
        if(password.isPresent()) {
            if(user.isEmpty()) {
                throw new OpConfigError("Both user and password options are required. Only password is supplied in this case.");
            }
            ds.setPassword(password.get());
        } else {
            if(user.isPresent()) {
                throw new OpConfigError("Both user and password options are required. Only user is supplied in this case.");
            }
        }

        Optional<String> sslMode = cfg.getOptional("sslMode");
        if(sslMode.isPresent()) {
            ds.setSslMode(sslMode.get());
        } else {
            ds.setSslMode("verify-full");
        }

        Optional<String> applicationName = cfg.getOptional("applicationName");
        if(applicationName.isPresent()) {
            ds.setApplicationName(applicationName.get());
        } else {
            ds.setApplicationName("NoSQLBench");
        }
        Optional<Boolean> rewriteBatchedInserts = cfg.getOptional(Boolean.class, "rewriteBatchedInserts");
        ds.setReWriteBatchedInserts(rewriteBatchedInserts.orElse(false));

        return ds;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(CockroachDBSpace.class)
            .add(Param.optional("url"))
            .add(Param.optional("serverName"))
            .add(Param.optional("databaseName"))
            //TODO remove these below
            .add(Param.optional("client_socket_timeout"))
            .add(Param.optional("client_execution_timeout"))
            .add(Param.optional("client_max_connections"))
            .add(Param.optional("client_max_error_retry"))
            .add(Param.optional("client_user_agent_prefix"))
            .add(Param.optional("client_consecutive_retries_before_throttling"))
            .add(Param.optional("client_gzip"))
            .add(Param.optional("client_tcp_keepalive"))
            .add(Param.optional("client_disable_socket_proxy"))
            .add(Param.optional("client_so_send_size_hint"))
            .add(Param.optional("client_so_recv_size_hint"))
            .asReadOnly();
    }
}
