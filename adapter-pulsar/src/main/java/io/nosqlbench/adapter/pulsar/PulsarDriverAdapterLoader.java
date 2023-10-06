package io.nosqlbench.adapter.pulsar;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "pulsar")
public class PulsarDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public PulsarDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new PulsarDriverAdapter(parent, childLabels);
    }
}
