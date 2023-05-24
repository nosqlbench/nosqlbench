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

package io.nosqlbench.adapter.venice;

import com.linkedin.venice.authentication.jwt.ClientAuthenticationProviderToken;
import com.linkedin.venice.client.store.AvroGenericStoreClient;
import com.linkedin.venice.client.store.ClientConfig;
import com.linkedin.venice.client.store.ClientFactory;
import com.linkedin.venice.producer.online.OnlineProducerFactory;
import com.linkedin.venice.producer.online.OnlineVeniceProducer;
import com.linkedin.venice.utils.VeniceProperties;
import io.nosqlbench.api.config.standard.ConfigModel;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.Param;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class VeniceSpace implements  AutoCloseable {

    private final static Logger logger = LogManager.getLogger(VeniceSpace.class);

    private final String spaceName;
    private final NBConfiguration cfg;

    private final String routerUrl;
    private final String storeName;

    private long veniceActivityStartTimeMills;
    private final String token;
    private ClientConfig clientConfig;

    private AvroGenericStoreClient<Object, Object> client;

    private OnlineVeniceProducer<Object, Object> producer;


    public VeniceSpace(String spaceName, NBConfiguration cfg) {
        this.spaceName = spaceName;
        this.cfg = cfg;

        this.routerUrl = cfg.get("router_url");
        this.storeName = cfg.get("store_name");
        this.token = cfg.get("token");

        this.veniceActivityStartTimeMills = System.currentTimeMillis();
        this.initializeSpace();
    }

    @Override
    public void close() {
        shutdownSpace();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(VeniceSpace.class)
            .add(Param.defaultTo("router_url", "http://localhost:7777")
                .setDescription("Venice Router URL."))
            .add(Param.defaultTo("store_name", "store1")
                .setDescription("Name of the Venice store"))
            .add(Param.defaultTo("token", "")
                .setDescription("JWT Token Authentication"))
            .asReadOnly();
    }

    public synchronized AvroGenericStoreClient<Object, Object> getClient() {
        if (client == null) {
            client = ClientFactory.getAndStartGenericAvroClient(clientConfig);
        }
        return client;
    }

    public synchronized OnlineVeniceProducer<Object, Object> getProducer() {
        if (producer == null) {
            VeniceProperties properties = VeniceProperties.empty();
            producer = OnlineProducerFactory.createProducer(clientConfig, properties,null);
        }
        return producer;
    }

    public void initializeSpace() {
        this.clientConfig = ClientConfig.defaultGenericClientConfig(storeName);
        clientConfig.setVeniceURL(routerUrl);
        clientConfig.setForceClusterDiscoveryAtStartTime(true);
        if (token != null && !token.isEmpty()) {
            clientConfig.setAuthenticationProvider(ClientAuthenticationProviderToken.TOKEN(token));
        }
    }

    public void shutdownSpace() {
        try {
            if (client != null) {
                client.close();
            }
        }
        catch (Exception e) {
           logger.error("Unexpected error when shutting down NB S4J space.", e);
        }
        try {
            if (producer != null) {
                producer.close();
            }
        }
        catch (Exception e) {
            logger.error("Unexpected error when shutting down NB S4J space.", e);
        }
    }

    public long getVeniceActivityStartTimeMills() {
        return veniceActivityStartTimeMills;
    }
}
