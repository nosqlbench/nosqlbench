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

import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
import io.nosqlbench.nb.api.config.standard.*;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.*;
import org.neo4j.driver.async.AsyncSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Neo4JSpace extends BaseSpace<Neo4JSpace> {

    private final static Logger logger = LogManager.getLogger(Neo4JSpace.class);
    private Driver driver;
    private SessionConfig sessionConfig;

    public Neo4JSpace(Neo4JDriverAdapter adapter, long idx, NBConfiguration cfg) {
        super(adapter,idx);
        this.driver = initializeDriver(cfg);
        driver.verifyConnectivity();
    }

    private Driver initializeDriver(NBConfiguration cfg) {
        SessionConfig.Builder builder = SessionConfig.builder();
        cfg.getOptional("database").ifPresent(builder::withDatabase);
        this.sessionConfig = builder.build();

        String dbURI = cfg.get("db_uri");

        Optional<String> usernameOpt = cfg.getOptional("username");
        Optional<String> userfileOpt = cfg.getOptional("userfile");
        Optional<String> passwordOpt = cfg.getOptional("password");
        Optional<String> passfileOpt = cfg.getOptional("passfile");

        String username = null;
        if (usernameOpt.isPresent()) {
            username = usernameOpt.get();
        } else if (userfileOpt.isPresent()) {
            Path path = Paths.get(userfileOpt.get());
            try {
                username = Files.readAllLines(path).get(0);
            } catch (IOException e) {
                String error = "Error while reading username from file:" + path;
                logger.error(error, e);
                throw new RuntimeException(e);
            }
        }

        String password = null;
        if (username != null) {

            if (passwordOpt.isPresent()) {
                password = passwordOpt.get();
            } else if (passfileOpt.isPresent()) {
                Path path = Paths.get(passfileOpt.get());
                try {
                    password = Files.readAllLines(path).get(0);
                } catch (IOException e) {
                    String error = "Error while reading password from file:" + path;
                    logger.error(error, e);
                    throw new RuntimeException(e);
                }
            } else {
                String error = "username is present, but neither password nor passfile are defined.";
                logger.error(error);
                throw new RuntimeException(error);
            }
        }

        if ((username == null) != (password == null)) {
            throw new BasicError("You must provide both username and password, or neither, with either " +
                "username|userfile and password|passfile options");
        }
        if (username != null) {
            return GraphDatabase.driver(dbURI, AuthTokens.basic(username, password));
        } else {

        }

        // user has supplied both username and password
        return GraphDatabase.driver(dbURI);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(Neo4JSpace.class)
            .add(Param.required("db_uri", String.class))
            .add(Param.optional("username", String.class))
            .add(Param.optional("password", String.class))
            .add(Param.optional("database", String.class))
            .add(Param.optional("userfile", String.class))
            .add(Param.optional("passfile", String.class))
            .asReadOnly();
    }

    public Driver getDriver() {
        return driver;
    }

    public AsyncSession newAsyncSession() {
        return driver.session(AsyncSession.class, sessionConfig);
    }

    public Session newSession() {
        return driver.session(sessionConfig);
    }

    @Override
    public void close() throws Exception {
        if (driver != null) {
            driver.close();
        }
    }
}
