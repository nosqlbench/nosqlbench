package io.nosqlbench.adapter.kafka;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "kafka")
public class KafkaDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public KafkaDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new KafkaDriverAdapter(parent, childLabels);
    }
}
