package io.nosqlbench.adapter.mongodb.core;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "mongodb")
public class KafkaDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public MongodbDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new MongodbDriverAdapter(parent, childLabels);
    }
}
