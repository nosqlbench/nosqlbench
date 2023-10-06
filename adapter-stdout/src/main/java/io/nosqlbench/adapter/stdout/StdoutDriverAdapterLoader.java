package io.nosqlbench.adapter.stdout;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "stdout")
public class StdoutDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public StdoutDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new StdoutDriverAdapter(parent, childLabels);
    }
}
