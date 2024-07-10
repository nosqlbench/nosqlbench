/*
 * Copyright (c) 2024 nosqlbench
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

package io.nosqlbench.adapter.neo4j;

import io.nosqlbench.nb.api.config.standard.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.*;
import org.neo4j.driver.async.AsyncSession;

import java.util.Optional;

public class Neo4JSpace implements AutoCloseable {

    private final static Logger logger = LogManager.getLogger(Neo4JSpace.class);
    private final String space;
    private Driver driver;
    private SessionConfig sessionConfig;

    public Neo4JSpace(String space, NBConfiguration cfg) {
        this.space = space;
        this.driver = initializeDriver(cfg);
        driver.verifyConnectivity();
    }

    private Driver initializeDriver(NBConfiguration cfg) {
        SessionConfig.Builder builder = SessionConfig.builder();
        cfg.getOptional("database").ifPresent(builder::withDatabase);
        this.sessionConfig = builder.build();

        String dbURI = cfg.get("db_uri");
        Optional<String> usernameOpt = cfg.getOptional("username");
        Optional<String> passwordOpt = cfg.getOptional("password");
        String username;
        String password;
        // user has supplied both username and password
        if (usernameOpt.isPresent() && passwordOpt.isPresent()) {
            username = usernameOpt.get();
            password = passwordOpt.get();
            logger.info(this.space + ": Creating new Neo4J driver with [" +
                "dbURI = " + dbURI +
                ", username = " + username +
                ", password = " + Neo4JAdapterUtils.maskDigits(password) +
                "]"
            );
            return GraphDatabase.driver(dbURI, AuthTokens.basic(username, password));
        }
        // user has only supplied username
        else if (usernameOpt.isPresent()) {
            String error = "username is present, but password is not defined.";
            logger.error(error);
            throw new RuntimeException(error);
        }
        // user has only supplied password
        else if (passwordOpt.isPresent()) {
            String error = "password is present, but username is not defined.";
            logger.error(error);
            throw new RuntimeException(error);
        }
        // user has supplied neither
        else {
            logger.info(this.space + ": Creating new Neo4J driver with [" +
                "dbURI = " + dbURI +
                "]"
            );
            return GraphDatabase.driver(dbURI);
        }
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(Neo4JSpace.class)
            .add(Param.required("db_uri", String.class))
            .add(Param.optional("username", String.class))
            .add(Param.optional("password", String.class))
            .add(Param.optional("database", String.class))
            .asReadOnly();
    }

    public Driver getDriver() {
        return driver;
    }

    public AsyncSession newAsyncSession() {
        return driver.session(AsyncSession.class,sessionConfig);
    }

    public Session newSession() {
        return driver.session(sessionConfig);
    }

    @Override
    public void close() throws Exception {
        if (driver != null){
            driver.close();
        }
    }
}
