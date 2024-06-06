/*
 * Copyright (c) 2020-2024 nosqlbench
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

package io.nosqlbench.adapter.qdrant;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * The {@code QdrantSpace} class is a context object which stores all stateful contextual information needed to interact
 * with the Qdrant database instance.
 *
 * @see <a href="https://qdrant.tech/documentation/cloud/quickstart-cloud/">Qdrant cloud quick start guide</a>
 * @see <a href="https://qdrant.tech/documentation/quick-start/">Qdrant quick start guide</a>
 * @see <a href="https://github.com/qdrant/java-client">Qdrant Java client</a>
 * @see <a href="https://github.com/qdrant/qdrant/blob/master/docs/grpc/docs.md">Qdrant GRPC docs</a>
 */
public class QdrantSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(QdrantSpace.class);
    private final String name;
    private final NBConfiguration cfg;

    protected QdrantClient client;

    /**
     * Create a new QdrantSpace Object which stores all stateful contextual information needed to interact
     * with the Qdrant database instance.
     *
     * @param name The name of this space
     * @param cfg  The configuration ({@link NBConfiguration}) for this nb run
     */
    public QdrantSpace(String name, NBConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;
    }

    public synchronized QdrantClient getClient() {
        if (client == null) {
            client = createClient();
        }
        return client;
    }

    private QdrantClient createClient() {
        String uri = cfg.get("uri");
        int grpcPort = cfg.getOptional("grpc_port").map(Integer::parseInt).orElse(6334);
        boolean useTls = cfg.getOptional("use_tls").map(Boolean::parseBoolean).orElse(true);

        var builder = QdrantGrpcClient.newBuilder(uri, grpcPort, useTls);
        var Optional<requiredToken> = cfg.getOptional("token_file")
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
            ).orElseGet(
                () -> cfg.getOptional("token")
                    .orElseThrow(() -> new RuntimeException("You must provide either a token_file or a token to " +
                        "configure a Qdrant client"))
            );
        builder = builder.withApiKey(requiredToken);
        builder = builder.withTimeout(
            Duration.ofMillis(NumberUtils.toInt(cfg.getOptional("timeout_ms").orElse("3000")))
        );

        logger.info("{}: Creating new Qdrant Client with (masked) token [{}], uri/endpoint [{}]",
            this.name, QdrantAdapterUtils.maskDigits(requiredToken), cfg.get("uri").toString());
        return new QdrantClient(builder.build());
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(QdrantSpace.class)
            .add(
                Param.optional("token_file", String.class, "the file to load the api token from")
            )
            .add(
                Param.defaultTo("token", "qdrant")
                    .setDescription("the Qdrant api token to use to connect to the database")
            )
            .add(
                Param.defaultTo("uri", "localhost")
                    .setDescription("the URI endpoint in which the database is running. Do not provide any suffix like https:// here.")
            )
            .add(
                Param.defaultTo("use_tls", true)
                    .setDescription("whether to use TLS for the connection. Defaults to true.")
            )
            .add(
                Param.defaultTo("timeout_ms", 3000)
                    .setDescription("sets the timeout in milliseconds for all requests. Defaults to 3000ms.")
            )
            .add(
                Param.defaultTo("grpc_port", 6443)
                    .setDescription("the port to use for the gRPC connection. Defaults to 6334.")
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
