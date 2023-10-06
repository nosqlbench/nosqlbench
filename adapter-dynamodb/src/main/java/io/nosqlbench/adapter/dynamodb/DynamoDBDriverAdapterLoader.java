package io.nosqlbench.adapter.dynamodb;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "dynamodb")
public class DynamoDBDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public DynamoDBDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new DynamoDBDriverAdapter(parent, childLabels);
    }
}
