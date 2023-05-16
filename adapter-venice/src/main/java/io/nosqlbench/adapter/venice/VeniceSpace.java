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

import com.linkedin.venice.client.store.AvroGenericStoreClient;
import com.linkedin.venice.client.store.ClientConfig;
import com.linkedin.venice.client.store.ClientFactory;
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

    private AvroGenericStoreClient<Object, Object> client;


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

    public AvroGenericStoreClient<Object, Object> getClient() {
        return client;
    }

    public void initializeSpace() {
        ClientConfig clientConfig = ClientConfig.defaultGenericClientConfig(storeName);
        clientConfig.setVeniceURL(routerUrl);
        clientConfig.setForceClusterDiscoveryAtStartTime(true);
        // clientConfig.setToken(token);
        client = ClientFactory.getAndStartGenericAvroClient(clientConfig);
    }

    public void shutdownSpace() {
        try {
            client.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error when shutting down NB S4J space.");
        }
    }

    public long getVeniceActivityStartTimeMills() {
        return veniceActivityStartTimeMills;
    }
}
