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

package io.nosqlbench.adapter.milvus;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The MilvusSpace class is a context object which stores all stateful contextual information needed to interact
 * with the Milvus/Zilliz database instance.
 * https://milvus.io/docs/install-java.md
 * https://docs.zilliz.com/docs/connect-to-cluster
 */
public class MilvusSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(MilvusSpace.class);
    private final String name;
    private final NBConfiguration cfg;

    protected MilvusServiceClient client;

//    private final Map<String, ConnectParam> connections = new HashMap<>();

    /**
     * Create a new MilvusSpace Object which stores all stateful contextual information needed to interact
     * with the Milvus/Zilliz database instance.
     *
     * @param name
     *     The name of this space
     * @param cfg
     *     The configuration ({@link NBConfiguration}) for this nb run
     */
    public MilvusSpace(String name, NBConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;
    }

    public synchronized MilvusServiceClient getClient() {
        if (client == null) {
            client = createClient();
        }
        return client;
    }

    private MilvusServiceClient createClient() {
        var builder = ConnectParam.newBuilder();
        builder = builder.withUri(cfg.get("uri"));
        cfg.getOptional("database_name").ifPresent(builder::withDatabaseName);
        cfg.getOptional("database").ifPresent(builder::withDatabaseName);

        cfg.getOptional("token_file")
            .map(Paths::get)
            .map(
                tokenFilePath -> {
                    try {
                        return Files.readAllLines(tokenFilePath).getFirst();
                    } catch (IOException e) {
                        String error = "Error while reading token from file:" + tokenFilePath;
                        logger.error(error, e);
                        throw new RuntimeException(e);
                    }
                }
            ).or(() -> cfg.getOptional("token"))
            .ifPresent(builder::withToken);
//        builder = builder.withToken(optionalToken);

        ConnectParam connectParams = builder.build();
        String tokenSummary = Optional.ofNullable(builder.getToken())
                .map(MilvusAdapterUtils::maskDigits).orElse("[none]");
        logger.info("{}: Creating new Milvus/Zilliz Client with (masked) token [{}], uri/endpoint [{}]",
            this.name, tokenSummary, builder.getUri());
        return new MilvusServiceClient(connectParams);
    }

    public static NBConfigModel getConfigModel() {

        return ConfigModel.of(MilvusSpace.class)
            .add(
                Param.optional("token_file", String.class, "the file to load the token from  (try root:Milvus within)")
            )
            .add(
                Param.optional("token")
                    .setDescription("the Milvus/Zilliz token to use to connect to the database (try root:Milvus)")
            )
            .add(
                Param.defaultTo("uri", "127.0.0.1:19530")
                    .setDescription("the URI endpoint in which the database is running.")
            )
            .add(
                Param.optional(List.of("database_name", "database"))
                    .setDescription("the name of the database to use.")
            )
            .asReadOnly();
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }

}
