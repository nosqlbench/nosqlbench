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

package io.nosqlbench.adapter.dataapi;

import com.datastax.astra.client.Collection;
import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.Database;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DataApiSpace {
    private final static Logger logger = LogManager.getLogger(DataApiSpace.class);
    private final NBConfiguration config;
    private final String name;
    private String astraToken;
    private String astraApiEndpoint;
    private DataAPIClient dataAPIClient;
    private Database database;
    public DataApiSpace(String name, NBConfiguration cfg) {
        this.config = cfg;
        this.name = name;
        setToken();
        setApiEndpoint();
        createClient();
    }

    public DataAPIClient getDataAPIClient() {
        return dataAPIClient;
    }

    public Database getDatabase() {
        return database;
    }

    private void createClient() {
        this.dataAPIClient = new DataAPIClient(astraToken);
        this.database = dataAPIClient.getDatabase(astraApiEndpoint);
//        database.getCollection("test");
//        database.listCollections().forEach(System.out::println);
//        Collection collection = database.getCollection("test");
//        collection.deleteMany()
    }

    private void setApiEndpoint() {
        this.astraApiEndpoint = config.get("astraApiEndpoint");
    }

    private void setToken() {
        String tokenFileContents = null;
        Optional<String> tokenFilePath = config.getOptional("astraTokenFile");
        if(tokenFilePath.isPresent()) {
            Path path = Paths.get(tokenFilePath.get());
            try {
                tokenFileContents = Files.readAllLines(path).getFirst();
            } catch (IOException e) {
                String error = "Error while reading token from file:" + path;
                logger.error(error, e);
                throw new RuntimeException(e);
            }
        }
        this.astraToken = (tokenFileContents != null) ? tokenFileContents : config.get("astraToken");
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(DataApiSpace.class)
            .add(
                Param.optional("astraTokenFile", String.class, "file to load the Astra token from")
            )
            .add(
                Param.optional("astraToken",String.class)
                    .setDescription("the Astra token used to connect to the database")
            )
            .add(
                Param.defaultTo("astraApiEndpoint", String.class)
                    .setDescription("the API endpoint for the Astra database")
            )
            .asReadOnly();
    }

}
