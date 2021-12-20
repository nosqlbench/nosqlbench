package io.nosqlbench.adapter.dynamodb;

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
