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

package io.nosqlbench.adapter.dataapi;

import com.datastax.astra.client.DataAPIClient;
import com.datastax.astra.client.DataAPIDestination;
import com.datastax.astra.client.databases.Database;
import com.datastax.astra.client.admin.AstraDBAdmin;
import com.datastax.astra.client.admin.DatabaseAdmin;
import com.datastax.astra.client.core.options.DataAPIClientOptions;

import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class DataApiSpace extends BaseSpace<DataApiSpace> {
    private final static Logger logger = LogManager.getLogger(DataApiSpace.class);
    private final NBConfiguration config;
    private String astraToken;
    private String astraApiEndpoint;
    private DataAPIDestination astraDestination;
    private DataAPIClient dataAPIClient;
    private Database database;
    private String keyspace;

    private String superUserToken;
    private AstraDBAdmin admin;
    private DatabaseAdmin databaseAdmin;

    private final String DEFAULT_KEYSPACE = "default_keyspace";

    public DataApiSpace(DataApiDriverAdapter adapter, long name, NBConfiguration cfg) {
        super(adapter,name);
        this.config = cfg;
        setToken();
        setSuperToken();
        setApiEndpoint();
        setKeyspace();
        setDestination();
        createClient();
    }

    public DataAPIClient getDataAPIClient() {
        return dataAPIClient;
    }

    public Database getDatabase() {
        return database;
    }

    public AstraDBAdmin getAdmin() {
        return admin;
    }

    public DatabaseAdmin getDatabaseAdmin() {
        return databaseAdmin;
    }

    private void createClient() {

	    /*
            TODO: deprecate op/dispensers and duplicate + test and document + ensure replication factor map is there
        */
        DataAPIClientOptions clientOptions = new DataAPIClientOptions();
        clientOptions = clientOptions.destination(astraDestination);

        this.dataAPIClient = new DataAPIClient(astraToken, clientOptions);
        if (keyspace != null) {
            this.database = dataAPIClient.getDatabase(astraApiEndpoint, keyspace);
        } else {
            this.database = dataAPIClient.getDatabase(astraApiEndpoint);
        }
        // There's no notion of "admin" unless we're on Astra:
        if (astraDestination != DataAPIDestination.ASTRA
                && astraDestination != DataAPIDestination.ASTRA_DEV
                && astraDestination != DataAPIDestination.ASTRA_TEST) {
            this.admin = null;
        } else {
            if (superUserToken != null) {
                this.admin = dataAPIClient.getAdmin(superUserToken);
            } else {
                this.admin = dataAPIClient.getAdmin();
            }
        }
        this.databaseAdmin = database.getDatabaseAdmin();
    }

    private void setApiEndpoint() {
        Optional<String> epConfig = config.getOptional("astraApiEndpoint");
        Optional<String> epFileConfig = config.getOptional("astraApiEndpointFile");
        if (epConfig.isPresent() && epFileConfig.isPresent()) {
            throw new BasicError("You can only configure one of astraApiEndpoint or astraApiEndpointFile");
        }
        if (epConfig.isEmpty() && epFileConfig.isEmpty()) {
            throw new BasicError("You must configure one of astraApiEndpoint or astraApiEndpointFile");
        }
        epFileConfig
            .map(Path::of)
            .map(p -> {
                try {
                    return Files.readString(p);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(String::trim)
            .ifPresent(ep -> this.astraApiEndpoint = ep);
        epConfig.ifPresent(ep -> this.astraApiEndpoint = ep);
    }

    private void setKeyspace() {
        String finalKeyspace = DEFAULT_KEYSPACE;
        Optional<String> maybeKeyspace = config.getOptional("keyspace");
        Optional<String> maybeNamespace = config.getOptional("namespace");
        if (maybeKeyspace.isPresent()) {
            if (maybeNamespace.isPresent()) {
                throw new BasicError("You can only configure one of 'keyspace' or (the deprecated alias) 'namespace'");
            } else {
                finalKeyspace = maybeKeyspace.get();
            }
        } else {
            // 'keyspace' not passed
            if (maybeNamespace.isPresent()) {
                logger.warn(() -> "Command-line parameter 'namespace' for Data API is deprecated. Please replace it with 'keyspace'.");
                finalKeyspace = maybeNamespace.get();
            }
            // (else: keep default)
        }

        keyspace = finalKeyspace;
    }

    private void setDestination() {
        Optional<String> maybeDestinationString = config.getOptional("astraDestination");
        if (maybeDestinationString.isPresent()) {
            astraDestination = DataAPIDestination.valueOf(maybeDestinationString.get().toUpperCase());
        } else {
            astraDestination = DataAPIDestination.ASTRA;
        }
    }

    private void setToken() {
        String tokenFileContents = null;
        Optional<String> tokenFilePath = config.getOptional("astraTokenFile");
        if (tokenFilePath.isPresent()) {
            tokenFileContents = getTokenFileContents(tokenFilePath.get());
        }
        this.astraToken = (tokenFileContents != null) ? tokenFileContents : config.get("astraToken");
    }

    private String getTokenFileContents(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.readAllLines(path).getFirst();
        } catch (IOException e) {
            String error = "Error while reading token from file:" + path;
            logger.error(error, e);
            throw new RuntimeException(e);
        }
    }

    private void setSuperToken() {
        String superTokenFileContents = null;
        Optional<String> superTokenFilePath = config.getOptional("superTokenFile");
        if (superTokenFilePath.isPresent()) {
            superTokenFileContents = getTokenFileContents(superTokenFilePath.get());
        }
        Optional<String> maybeSuperToken = config.getOptional("superToken");
        // It's fine if this is null
        this.superUserToken = maybeSuperToken.orElse(superTokenFileContents);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(DataApiSpace.class)
            .add(
                Param.optional("astraTokenFile", String.class)
                    .setDescription("file to load the Astra token from")
            )
            .add(
                Param.optional("astraToken", String.class)
                    .setDescription("the Astra token used to connect to the database")
            )
            .add(
                Param.optional("astraApiEndpoint", String.class)
                    .setDescription("the API endpoint for the Astra database")
            )
            .add(
                Param.optional("astraApiEndpointFile", String.class)
                    .setDescription("file to load the API endpoint for the Astra database")
            )
            .add(
                Param.optional("keyspace", String.class)
                    .setDescription("The Astra keyspace to use")
            )
            .add(
                Param.optional("namespace", String.class)
                    .setDescription("The Astra keyspace to use (deprecated alias for 'keyspace')")
            )
            .add(
                Param.optional("superTokenFile", String.class)
                    .setDescription("optional file to load Astra admin user token from")
            )
            .add(
                Param.optional("superToken", String.class)
                    .setDescription("optional Astra token used to connect as Admin user")
            )
            .add(
                Param.optional("astraDestination", String.class)
                    .setDescription(
                        "The 'destination' (ASTRA, ASTRA_DEV, ASTRA_TEST, HCD, DSE, CASSANDRA, OTHERS) of the target database"
                    )
            )
            .asReadOnly();
    }

}
