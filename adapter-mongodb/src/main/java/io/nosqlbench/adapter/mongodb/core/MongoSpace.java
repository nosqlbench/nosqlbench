/*
 * Copyright (c) 2022 nosqlbench
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
import com.mongodb.client.MongoDatabase;
import io.nosqlbench.api.config.NBNamedElement;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.bson.UuidRepresentation;
import org.bson.codecs.UuidCodec;
import org.bson.codecs.configuration.CodecRegistry;

import static org.bson.codecs.configuration.CodecRegistries.fromCodecs;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class MongoSpace implements NBNamedElement {
    private final String name;
    private final NBConfiguration cfg;
    private final String connection;
    private final MongoClient client;
    private MongoDatabase mongoDatabase;

    public MongoSpace(String name, String connection, NBConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;
        this.connection = connection;
        this.client = createMongoClient(this.connection);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(MongoSpace.class)
                .add(Param.required("connection", String.class)
                        .setDescription("The connection string for your MongoDB endpoint"))
                .add(Param.required("database", String.class)
                        .setDescription("The database name to connect to."));
    }


    @Override
    public String getName() {
        return name;
    }

    public MongoClient createMongoClient(String connection) {

        CodecRegistry codecRegistry = fromRegistries(
                fromCodecs(new UuidCodec(UuidRepresentation.STANDARD)),
                MongoClientSettings.getDefaultCodecRegistry()
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connection))
                .codecRegistry(codecRegistry)
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }

    protected MongoDatabase getDatabase() {
        return this.mongoDatabase;
    }

    protected String getConnection() {
        return this.connection;
    }

    public MongoClient getClient() {
        return this.client;
    }
}
