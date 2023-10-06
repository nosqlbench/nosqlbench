package io.nosqlbench.adapter.s4j;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "s4j")
public class S4JDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public S4JDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new S4JDriverAdapter(parent, childLabels);
    }
}
