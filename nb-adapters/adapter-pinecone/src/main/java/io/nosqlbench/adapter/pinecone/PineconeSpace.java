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

package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.pinecone.PineconeClient;
import io.pinecone.PineconeClientConfig;
import io.pinecone.PineconeConnection;
import io.pinecone.PineconeConnectionConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PineconeSpace {

    private final static Logger logger = LogManager.getLogger(PineconeSpace.class);
    private final String apiKey;
    private final String apiKeyFile;
    private final String environment;
    private final String projectName;
    private final String name;

    private final PineconeClient client;
    private final PineconeClientConfig config;

    private final Map<String,PineconeConnection> connections = new HashMap<>();

    /**
     * Create a new PineconeSpace Object which stores all stateful contextual information needed to interact
     * with the Pinecone database instance.
     *
     * @param name  The name of this space
     * @param cfg   The configuration ({@link NBConfiguration}) for this nb run
     */
    public PineconeSpace(String name, NBConfiguration cfg) {
        String apiKeyFromFile = null;
        this.apiKeyFile = cfg.get("apiKeyFile");
        if(null != this.apiKeyFile && this.apiKeyFile.length() > 0) {
            Optional<String> apiKeyFileOpt = cfg.getOptional("apiKeyFile");
            if(apiKeyFileOpt.isPresent()) {
                Path path = Paths.get(apiKeyFileOpt.get());
                try {
                    apiKeyFromFile = Files.readAllLines(path).getFirst();
                } catch (IOException e) {
                    String error = "Error while reading api key from file:" + path;
                    logger.error(error, e);
                    throw new RuntimeException(e);
                }
            }
        }

        this.apiKey = (apiKeyFromFile != null) ? apiKeyFromFile : cfg.get("apiKey");
        this.environment = cfg.get("environment");
        this.projectName = cfg.get("projectName");
        this.name = name;
        config = new PineconeClientConfig()
            .withApiKey(apiKey)
            .withEnvironment(environment)
            .withProjectName(projectName);
        logger.info(this.name + ": Creating new Pinecone Client with api key " + maskDigits(apiKey) + ", environment "
            + environment + " and project name " + projectName);
        this.client = new PineconeClient(config);
    }

    /**
     * Connections are index-specific, so we need to allow for multiple connection management across indices.
     * However, note that a single connection object is thread safe and can be used by multiple clients.
     *
     * @param index     The database index for which a connection is being requested
     * @return          The {@link PineconeConnection} for this database index
     */
    public synchronized PineconeConnection getConnection(String index) {
        PineconeConnection connection = connections.get(index);
        if (connection == null) {
            logger.info("Creating new Pinecone Connection to Index " + index);
            PineconeConnectionConfig connectionConfig = new PineconeConnectionConfig().withIndexName(index);
            connection = client.connect(connectionConfig);
            connections.put(index, connection);
        }
        return connection;
    }

    public static NBConfigModel getConfigModel() {

        return ConfigModel.of(PineconeSpace.class)
            .add(
                Param.optional("apiKeyFile", String.class, "file to load the api key from")
            )
            .add(
                Param.optional("apiKey",String.class)
                    .setDescription("the Pinecone API key to use to connect to the database")
            )
            .add(
                Param.defaultTo("environment","us-east-1-aws")
                    .setDescription("the environment in which the desired index is running.")
            )
            .add(
                Param.defaultTo("projectName","default")
                    .setDescription("the project name associated with the desired index")
            )
            .asReadOnly();
    }

    private static String maskDigits(String unmasked) {
        int inputLength = (null == unmasked) ? 0 : unmasked.length();
        StringBuilder masked = new StringBuilder(inputLength);
        for(char ch : unmasked.toCharArray()) {
            if (Character.isDigit(ch)) {
                masked.append("*");
            } else {
                masked.append(ch);
            }
        }
        return masked.toString();
    }

}
