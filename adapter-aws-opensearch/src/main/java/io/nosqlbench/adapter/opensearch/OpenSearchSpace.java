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


import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.internal.BasicProfile;
import com.amazonaws.auth.profile.internal.ProfileKeyConstants;
import com.amazonaws.auth.profile.internal.ProfileStaticCredentialsProvider;
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
import java.util.Map;

public class OpenSearchSpace implements AutoCloseable {

    private final NBConfiguration cfg;
    protected OpenSearchClient client;

    public OpenSearchSpace(NBConfiguration cfg) {
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

        ProfileCredentialsProvider creds = ProfileCredentialsProvider.builder()
            .profileName("686157956141_ENG-TESTENG_AWS-ENG-PERF")
            .build();


        SdkAsyncHttpClient httpClient =
            AwsCrtAsyncHttpClient.builder()
                .build();

        AwsSdk2TransportOptions transportOptions =
            AwsSdk2TransportOptions.builder()
                .setCredentials(creds)
                .build();

        AwsSdk2Transport awsSdk2Transport =
            new AwsSdk2Transport(
                httpClient,
                host,
                "es",
                selectedRegion,
                transportOptions
            );

        OpenSearchClient client = new OpenSearchClient(awsSdk2Transport);

        try {
            InfoResponse info = client.info();
            System.out.println(info.version().distribution() + ": " + info.version().number());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return client;

//        // Create a new domain, update its configuration, and delete it.
//        createDomain(client, domainName);
//        //waitForDomainProcessing(client, domainName);
//        updateDomain(client, domainName);
//        //waitForDomainProcessing(client, domainName);
//        deleteDomain(client, domainName);

    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(OpenSearchSpace.class)
            .add(Param.required("region", String.class).setDescription("The region to connect to"))
            .add(Param.required("host", String.class).setDescription("The Open Search API endpoint host"))
            .asReadOnly();
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.shutdown();
        }
    }


//    /**
//     * Waits for the domain to finish processing changes. New domains typically take 15-30 minutes
//     * to initialize, but can take longer depending on the configuration. Most updates to existing domains
//     * take a similar amount of time. This method checks every 15 seconds and finishes only when
//     * the domain's processing status changes to false.
//     *
//     * @param client
//     *            The client to use for the requests to Amazon OpenSearch Service
//     * @param domainName
//     *            The name of the domain that you want to check
//     */
//
//    public static void waitForDomainProcessing(OpenSearchClient client, String domainName) {
//        // Create a new request to check the domain status.
//        DescribeDomainRequest describeRequest = DescribeDomainRequest.builder()
//            .domainName(domainName)
//            .build();
//
//        // Every 15 seconds, check whether the domain is processing.
//        DescribeDomainResponse describeResponse = client.describeDomain(describeRequest);
//        while (describeResponse.domainStatus().processing()) {
//            try {
//                System.out.println("Domain still processing...");
//                TimeUnit.SECONDS.sleep(15);
//                describeResponse = client.describeDomain(describeRequest);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // Once we exit that loop, the domain is available
//        System.out.println("Amazon OpenSearch Service has finished processing changes for your domain.");
//        System.out.println("Domain description: "+describeResponse.toString());
//    }


}
