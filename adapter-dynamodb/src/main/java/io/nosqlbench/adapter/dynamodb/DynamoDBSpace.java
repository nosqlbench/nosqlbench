package io.nosqlbench.adapter.dynamodb;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.nosqlbench.nb.api.config.standard.*;
import io.nosqlbench.nb.api.errors.OpConfigError;

import java.util.Optional;

public class DynamoDBSpace {
    private final String name;
    DynamoDB dynamoDB;

    public DynamoDBSpace(String name, NBConfiguration cfg) {
        this.name = name;
        AmazonDynamoDB client = createClient(cfg);
        dynamoDB= new DynamoDB(client);
    }

    public DynamoDB getDynamoDB() {
        return dynamoDB;
    }

    private AmazonDynamoDB createClient(NBConfiguration cfg) {
        AmazonDynamoDBClientBuilder builder = AmazonDynamoDBClientBuilder.standard();
        Optional<String> region = cfg.getOptional("region");
        Optional<String> endpoint = cfg.getOptional("endpoint");
        Optional<String> signing_region = cfg.getOptional("signing_region");

        if (region.isPresent() && (endpoint.isPresent() || signing_region.isPresent())) {
            throw new OpConfigError("If you specify region, endpoint and signing_region option are ambiguous");
        }

        if (region.isPresent()) {
            builder.withRegion(region.get());
        } else if (endpoint.isPresent() && signing_region.isPresent()){
            AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint.get(), signing_region.get());
            builder = builder.withEndpointConfiguration(endpointConfiguration);
        } else {
            throw new OpConfigError("Either region or endpoint and signing_region options are required.");
        }


        ClientConfiguration ccfg = new ClientConfiguration();
        cfg.getOptional("client_socket_timeout").map(Integer::parseInt).ifPresent(ccfg::withSocketTimeout);
        cfg.getOptional("client_execution_timeout").map(Integer::parseInt).ifPresent(ccfg::withClientExecutionTimeout);
        cfg.getOptional("client_max_connections").map(Integer::parseInt).ifPresent(ccfg::withMaxConnections);
        cfg.getOptional("client_max_error_retry").map(Integer::parseInt).ifPresent(ccfg::withMaxErrorRetry);
        cfg.getOptional("client_user_agent_prefix").ifPresent(ccfg::withUserAgentPrefix);
//            ccfg.withHeader();
        cfg.getOptional("client_consecutive_retries_before_throttling").map(Integer::parseInt)
            .ifPresent(ccfg::withMaxConsecutiveRetriesBeforeThrottling);
        cfg.getOptional("client_gzip").map(Boolean::parseBoolean).ifPresent(ccfg::withGzip);
        cfg.getOptional("client_tcp_keepalive").map(Boolean::parseBoolean).ifPresent(ccfg::withTcpKeepAlive);
        cfg.getOptional("client_disable_socket_proxy").map(Boolean::parseBoolean).ifPresent(ccfg::withDisableSocketProxy);
//                ccfg.withProtocol()
//                    ccfg.withRetryMode();
//            ccfg.withRetryPolicy();

        ccfg.withSocketBufferSizeHints(
            cfg.getOptional("client_so_send_size_hint").map(Integer::parseInt).orElse(0),
            cfg.getOptional("client_so_recv_size_hint").map(Integer::parseInt).orElse(0)
        );

        builder.setClientConfiguration(ccfg);

        return builder.build();
    }

    public static NBConfigModel getConfigModel() {
        return ConfigModel.of(DynamoDBSpace.class)
            .add(Param.optional("endpoint"))
            .add(Param.optional("signing_region"))
            .add(Param.optional("region"))
            .asReadOnly();
    }

}
