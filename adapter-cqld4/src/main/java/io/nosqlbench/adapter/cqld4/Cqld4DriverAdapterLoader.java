package io.nosqlbench.adapter.cqld4;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "cqld4")
public class Cqld4DriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public Cqld4DriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new Cqld4DriverAdapter(parent, childLabels);
    }
}
