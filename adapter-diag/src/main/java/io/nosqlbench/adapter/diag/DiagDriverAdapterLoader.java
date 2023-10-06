package io.nosqlbench.adapter.diag;

import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "diag")
public class DiagDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public DiagDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new DiagDriverAdapter(parent, childLabels);
    }
}
