/*
 * Copyright (c) nosqlbench
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


import io.nosqlbench.adapters.api.activityimpl.uniform.Space;
import io.nosqlbench.nb.api.config.standard.ConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfigModel;
import io.nosqlbench.nb.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.api.config.standard.Param;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.function.Factory;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.reactor.ssl.TlsDetails;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.InfoResponse;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.IOException;

public class OpenSearchSpace implements Space {

    private final NBConfiguration cfg;
    private final String spaceName;
    private final OpenSearchAdapter adapter;
    protected OpenSearchClient client;

    public OpenSearchSpace(OpenSearchAdapter adapter, long spaceId, NBConfiguration cfg) {
        this.adapter = adapter;
        this.cfg = cfg;
        this.spaceName = String.valueOf(spaceId);
    }

    @Override
    public String getName() {
        return spaceName;
    }

    public synchronized OpenSearchClient getClient() {
        if (client == null) {
            client = createClient();
        }
        return client;
    }

    private OpenSearchClient createClient() {
        // Check if AWS-specific config is present
        // Only consider it AWS if region or profile is explicitly provided
        boolean isAws = cfg.getOptional("region").isPresent() ||
                       cfg.getOptional("profile").isPresent();

        OpenSearchClient client;
        if (isAws) {
            client = createAwsClient();
        } else {
            client = createHttpClient();
        }

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

    private OpenSearchClient createAwsClient() {
        String region = cfg.get("region");
        Region selectedRegion = Region.of(region);
        String host = cfg.get("host");

        SdkAsyncHttpClient httpClient = AwsCrtAsyncHttpClient.builder().build();

        AwsSdk2TransportOptions.Builder transportOptionsBuilder = AwsSdk2TransportOptions.builder();

        cfg.getOptional("profile").map(
                        p -> ProfileCredentialsProvider.builder()
                                .profileName(p)
                                .build())
                .ifPresent(transportOptionsBuilder::setCredentials);

        AwsSdk2TransportOptions transportOptions = transportOptionsBuilder.build();
        OpenSearchServiceType svctype = OpenSearchServiceType.valueOf(cfg.get("svctype"));

        AwsSdk2Transport awsSdk2Transport = new AwsSdk2Transport(
                httpClient,
                host,
                svctype.name(),
                selectedRegion,
                transportOptions
        );

        return new OpenSearchClient(awsSdk2Transport);
    }

    private OpenSearchClient createHttpClient() {
        String host = cfg.get("host");
        int port = cfg.getOptional("port").map(Integer::parseInt).orElse(9200);
        boolean ssl = Boolean.parseBoolean(cfg.getOptional("ssl").orElse("false"));

        String scheme = ssl ? "https" : "http";
        HttpHost httpHost = new HttpHost(scheme, host, port);

        ApacheHttpClient5TransportBuilder builder = ApacheHttpClient5TransportBuilder.builder(httpHost);

        // Configure authentication and SSL if provided
        cfg.getOptional("username").ifPresent(username -> {
            String password = cfg.getOptional("password").orElse("");

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                    new AuthScope(httpHost),
                    new UsernamePasswordCredentials(username, password.toCharArray())
                );

                if (ssl) {
                    try {
                        // Configure SSL
                        String truststore = cfg.getOptional("truststore").orElse(null);
                        String truststorePassword = cfg.getOptional("truststore_password").orElse(null);

                        if (truststore != null && truststorePassword != null) {
                            System.setProperty("javax.net.ssl.trustStore", truststore);
                            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
                        }

                        SSLContext sslContext = SSLContextBuilder
                            .create()
                            .loadTrustMaterial(null, (chains, authType) -> true)
                            .build();

                        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                            .setSslContext(sslContext)
                            .setTlsDetailsFactory(new Factory<SSLEngine, TlsDetails>() {
                                @Override
                                public TlsDetails create(final SSLEngine sslEngine) {
                                    return new TlsDetails(sslEngine.getSession(), sslEngine.getApplicationProtocol());
                                }
                            })
                            .build();

                        PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder
                            .create()
                            .setTlsStrategy(tlsStrategy)
                            .build();

                        return httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider)
                            .setConnectionManager(connectionManager);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to configure SSL", e);
                    }
                } else {
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        });

        OpenSearchTransport transport = builder.build();
        return new OpenSearchClient(transport);
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(OpenSearchSpace.class)
                // Connection settings
                .add(Param.required("host", String.class).setDescription("OpenSearch host (hostname or full URL)"))

                // AWS-specific settings (optional)
                .add(Param.optional("region", String.class).setDescription("AWS region (for AWS OpenSearch Service)"))
                .add(Param.optional("profile").setDescription("AWS auth profile (for AWS OpenSearch Service)"))
                .add(Param.defaultTo("svctype", "es").setDescription("AWS service type: es or aoss (for AWS OpenSearch)"))

                // HTTP-specific settings (for open source OpenSearch)
                .add(Param.optional("port", Integer.class).setDescription("OpenSearch port (default: 9200)"))
                .add(Param.optional("username").setDescription("Basic auth username (for open source OpenSearch)"))
                .add(Param.optional("password").setDescription("Basic auth password (for open source OpenSearch)"))
                .add(Param.defaultTo("ssl", "false").setDescription("Enable SSL/TLS (true/false)"))
                .add(Param.optional("truststore").setDescription("Path to SSL truststore file"))
                .add(Param.optional("truststore_password").setDescription("SSL truststore password"))

                // Common settings
                .add(Param.defaultTo("getinfo", "false").setDescription("Call info API after connect (true/false)"))
                .add(Param.defaultTo("diag", "false").setDescription("Enable payload diagnostics (true/false)"))
                .asReadOnly();
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.shutdown();
        }
    }


}
