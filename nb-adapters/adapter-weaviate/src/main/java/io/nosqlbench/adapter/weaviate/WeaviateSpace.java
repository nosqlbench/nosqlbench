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

package io.nosqlbench.adapter.weaviate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import io.nosqlbench.nb.api.errors.OpConfigError;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.auth.exception.AuthException;

/**
 * The {@code WeaviateSpace} class is a context object which stores all stateful
 * contextual information needed to interact with the Weaviate database
 * instance.
 *
 * @see <a href="https://weaviate.io/developers/weaviate/quickstart">Weaviate
 *      quick start guide</a>
 * @see <a href="https://weaviate.io/developers/wcs/quickstart">Weaviate cloud
 *      quick start guide</a>
 * @see <a href="https://github.com/weaviate/java-client">Weaviate Java
 *      client</a>
 * @see <a href=
 *      "https://github.com/qdrant/qdrant/blob/master/docs/grpc/docs.md">Qdrant
 *      GRPC docs</a>
 */
public class WeaviateSpace implements AutoCloseable {
    private final static Logger logger = LogManager.getLogger(WeaviateSpace.class);
    private final String name;
    private final NBConfiguration cfg;

    protected WeaviateClient client;

    /**
	 * Create a new WeaviateSpace Object which stores all stateful contextual
	 * information needed to interact with the <b>Weaviate</b> database instance.
	 *
	 * @param name The name of this space
	 * @param cfg  The configuration ({@link NBConfiguration}) for this nb run
	 */
    public WeaviateSpace(String name, NBConfiguration cfg) {
        this.name = name;
        this.cfg = cfg;
    }

	public synchronized WeaviateClient getClient() throws AuthException {
        if (client == null) {
            client = createClient();
        }
        return client;
    }

	private WeaviateClient createClient() throws AuthException {
        String uri = cfg.get("uri");
		String scheme = cfg.getOptional("scheme").orElse("https");
//        int grpcPort = cfg.getOptional("grpc_port").map(Integer::parseInt).orElse(6334);
//        boolean useTls = cfg.getOptional("use_tls").map(Boolean::parseBoolean).orElse(true);

//        var builder = QdrantGrpcClient.newBuilder(uri, grpcPort, useTls);
		var requiredToken = cfg.getOptional("token_file")
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
										"configure a Weaviate client"))
            );
		if (requiredToken != null
				&& (cfg.getOptional("username").isPresent() || cfg.getOptional("password").isPresent())) {
			throw new OpConfigError("Username/Password combo cannot be used together with token/tokenFile");
		}
//        builder = builder.withApiKey(requiredToken);
//        builder = builder.withTimeout(
//            Duration.ofMillis(NumberUtils.toInt(cfg.getOptional("timeout_ms").orElse("3000")))
//        );

		logger.info("{}: Creating new Weaviate Client with (masked) token [{}], uri/endpoint [{}]",
				this.name, WeaviateAdapterUtils.maskDigits(requiredToken), uri);
		Config config = new Config(scheme, uri);
    	
		if (cfg.getOptional("username").isPresent() && cfg.getOptional("password").isPresent()) {
			return WeaviateAuthClient.clientPassword(config, cfg.getOptional("username").get(),
					cfg.getOptional("password").get(), null);
		} else {
			return WeaviateAuthClient.apiKey(config, requiredToken);
		}
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(WeaviateSpace.class)
            .add(
                Param.optional("token_file", String.class, "the file to load the api token from")
            )
            .add(
						Param.defaultTo("token", "weaviate")
                    .setDescription("the Weaviate api token to use to connect to the database")
            )
            .add(
						Param.defaultTo("scheme", "http")
								.setDescription("the scheme of the database. Defaults to http."))
				.add(Param.defaultTo("uri", "localhost:8080").setDescription(
						"the URI endpoint in which the database is running. Do not provide any suffix like https:// here.")
            )
				.add(Param.optional("username")
						.setDescription("Username to be used for non-WCD clusters. Need Password config too."))
				.add(Param.optional("password")
						.setDescription("Password to be used for non-WCD clusters. Need Username config too."))
            .add(
                Param.defaultTo("timeout_ms", 3000)
                    .setDescription("sets the timeout in milliseconds for all requests. Defaults to 3000ms.")
            )
            .asReadOnly();
    }

    @Override
    public void close() throws Exception {
		client = null;
    }
}
