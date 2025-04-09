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

import io.nosqlbench.adapters.api.activityimpl.uniform.BaseSpace;
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
 */
public class WeaviateSpace extends BaseSpace<WeaviateSpace> {
    private final static Logger logger = LogManager.getLogger(WeaviateSpace.class);
    private final NBConfiguration cfg;

    protected WeaviateClient client;

    /**
     * Create a new WeaviateSpace Object which stores all stateful contextual
     * information needed to interact with the <b>Weaviate</b> database instance.
     *
     * @param cfg  The configuration ({@link NBConfiguration}) for this nb run
     */
    public WeaviateSpace(WeaviateDriverAdapter adapter, long idx, NBConfiguration cfg) {
        super(adapter, String.valueOf(idx));
        this.cfg = cfg;
    }

    public synchronized WeaviateClient getClient() {
        if (client == null) {
            try {
                client = createClient();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return client;
    }

    private WeaviateClient createClient() throws AuthException {
        String uri = cfg.get("uri");
        String scheme = cfg.getOptional("scheme").orElse("https");

        var requiredToken = cfg.getOptional("token_file").map(Paths::get).map(tokenFilePath -> {
            try {
                return Files.readAllLines(tokenFilePath).getFirst();
            } catch (IOException e) {
                String error = "Error while reading token from file:" + tokenFilePath;
                logger.error(error, e);
                throw new RuntimeException(e);
            }
        }).orElseGet(() -> cfg.getOptional("token").orElse(null)
//            orElseThrow(() -> new RuntimeException("You must provide either a token_file or a token to " + "configure a Weaviate client"))
        );
        if (requiredToken != null && (cfg.getOptional("username").isPresent() || cfg.getOptional("password").isPresent())) {
            throw new OpConfigError("Username/Password combo cannot be used together with token/tokenFile");
        }

        Config config = new Config(scheme, uri);

        String username = cfg.getOptional("username").orElse(null);
        String password = cfg.getOptional("password").orElse(null);

        if (username != null & password != null) {
            logger.info("{}: Creating new Weaviate Client with username [{}], and (masked) password [{}]", this.getName(), username, WeaviateAdapterUtils.maskDigits(password));
            return WeaviateAuthClient.clientPassword(config, username, password, null);
        } else if (cfg.getOptional("token").isPresent()) {
            logger.info("{}: Creating new Weaviate Client with (masked) token [{}], uri/endpoint [{}]", this.getName(), WeaviateAdapterUtils.maskDigits(requiredToken), uri);
            return WeaviateAuthClient.apiKey(config, requiredToken);
        } else {
            logger.info("{}: Creating new Weaviate Client without credentials.", this.getName());
            return new WeaviateClient(config);
        }
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(WeaviateSpace.class).add(Param.optional("token_file", String.class, "the file to load the api token from")).add(Param.optional("token", String.class).setDescription("the Weaviate api token to use to connect to " + "the " + "database")).add(Param.defaultTo("scheme", "http").setDescription("the scheme of the database. Defaults to http.")).add(Param.defaultTo("uri", "localhost:8080").setDescription("the URI endpoint in which the database is running. Do not provide any suffix like https:// here.")).add(Param.optional("username").setDescription("Username to be used for non-WCD clusters. Need Password config too.")).add(Param.optional("password").setDescription("Password to be used for non-WCD clusters. Need Username config too.")).add(Param.defaultTo("timeout_ms", 3000).setDescription("sets the timeout in milliseconds for all requests. Defaults to 3000ms.")).asReadOnly();
    }

    @Override
    public void close() throws Exception {
        client = null;
    }
}
