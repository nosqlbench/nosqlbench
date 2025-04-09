/*
 * Copyright (c) 2022-2024 nosqlbench
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

package io.nosqlbench.adapter.mongodb.core;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
import com.mongodb.client.MongoDatabase;
import io.nosqlbench.nb.api.components.core.NBNamedElement;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoSpace extends BaseSpace<MongoSpace> {
    private final static Logger logger = LogManager.getLogger(MongoSpace.class);
    private MongoClient mongoClient;
    private final NBConfiguration cfg;

    public MongoSpace(MongodbDriverAdapter adapter, long idx, NBConfiguration cfg) {
        super(adapter,String.valueOf(idx));
        this.cfg = cfg;
        logger.debug(() -> "Creating MongoSpace with config: " + cfg);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(MongoSpace.class)
                .add(Param.required("connection", String.class)
                        .setDescription("The connection string for your MongoDB endpoint"))
                .add(Param.required("database", String.class)
                        .setDescription("The database name to connect to."))
                .asReadOnly();
    }

    @Override
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            logger.error(() -> "auto-closeable mongodb connection threw exception in " +
                    "mongodb space(" + getName() + "): " + e);
            throw new RuntimeException(e);
        }
    }

    private void createMongoClient(String connectionURL) {

        CodecRegistry codecRegistry = fromRegistries(
                fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        );

        // https://www.mongodb.com/docs/manual/reference/stable-api
        ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .deprecationErrors(false)
            .strict(false) // Needed because createSearchIndexes is not in stable API (yet)
            .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionURL))
                .codecRegistry(codecRegistry)
                .serverApi(serverApi)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applicationName("NoSQLBench")
                .build();
        this.mongoClient = MongoClients.create(settings);

        // Send a ping to confirm a successful connection
        MongoDatabase mdb = this.mongoClient.getDatabase("admin");
        mdb.runCommand(new Document("ping", 1));
        logger.info(() -> "Connection ping test to the cluster successful.");
    }

    public synchronized MongoClient getClient() {
        if (this.mongoClient == null) {
            createMongoClient(cfg.get("connection"));
        }
        return this.mongoClient;
    }

}
