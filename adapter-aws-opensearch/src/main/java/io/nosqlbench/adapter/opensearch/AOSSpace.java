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

package io.nosqlbench.adapter.opensearch;


import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.InfoResponse;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;

public class AOSSpace implements AutoCloseable {

    private final NBConfiguration cfg;
    protected OpenSearchClient client;

    public AOSSpace(NBConfiguration cfg) {
        this.cfg = cfg;
    }

    public synchronized OpenSearchClient getClient() {
        if (client == null) {
            client = createClient();
        }
        return client;
    }

    private OpenSearchClient createClient() {
        String region = cfg.get("region");
        Region selectedRegion = Region.of(region);

        String host = cfg.get("host");


        SdkAsyncHttpClient httpClient =
                AwsCrtAsyncHttpClient.builder()
                        .build();

        AwsSdk2TransportOptions.Builder transportOptionsBuilder
                = AwsSdk2TransportOptions.builder();

        cfg.getOptional("profile").map(
                        p -> ProfileCredentialsProvider.builder()
                                .profileName(p)
                                .build())
                .ifPresent(transportOptionsBuilder::setCredentials);

        AwsSdk2TransportOptions transportOptions = transportOptionsBuilder.build();

        AOSServiceType svctype = AOSServiceType.valueOf(cfg.get("svctype"));

        AwsSdk2Transport awsSdk2Transport =
                new AwsSdk2Transport(
                        httpClient,
                        host,
                        svctype.name(),
                        selectedRegion,
                        transportOptions
                );

        OpenSearchClient client = new OpenSearchClient(awsSdk2Transport);

        if (cfg.get("getinfo").equals("true")) {
            try {
                InfoResponse info = client.info();
                System.out.println(info.version().distribution() + ": " + info.version().number());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return client;
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(AOSSpace.class)
                .add(Param.required("region", String.class).setDescription("The region to connect to"))
                .add(Param.required("host", String.class).setDescription("The Open Search API endpoint host"))
                .add(Param.optional("profile")
                        .setDescription("The AWS auth profile to use. Required to activate profile based auth"))
                .add(Param.defaultTo("getinfo", "false").setDescription("whether to call info after connect or " +
                        "not, true|false"))
                .add(Param.defaultTo("svctype", "es")
                        .setDescription("one of es or aoss, defaults to es for OpenSearch domains"))
                .add(Param.defaultTo("diag", "false")
                        .setDescription("enable payload diagnostics or not"))
                .asReadOnly();
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.shutdown();
        }
    }


}
