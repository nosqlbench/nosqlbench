/*
 * Copyright (c) 2022-2023 nosqlbench
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
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import io.nosqlbench.components.NBNamedElement;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoSpace implements NBNamedElement, AutoCloseable {
    private final static Logger logger = LogManager.getLogger(MongoSpace.class);
    private final String spaceName;
    private final NBConfiguration mongoConfig;
    private MongoClient mongoClient;

    public MongoSpace(String name, NBConfiguration cfg) {
        this.spaceName = name;
        this.mongoConfig = cfg;
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
    public String getName() {
        return spaceName;
    }

    @Override
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            logger.error(() -> "auto-closeable mongodb connection threw exception in " +
                    "mongodb space(" + this.spaceName + "): " + e);
            throw new RuntimeException(e);
        }
    }

    public void createMongoClient(String connectionURL) {

        CodecRegistry codecRegistry = fromRegistries(
                fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionURL))
                .codecRegistry(codecRegistry)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        this.mongoClient = MongoClients.create(settings);
    }

    public MongoClient getClient() {
        return this.mongoClient;
    }

}
