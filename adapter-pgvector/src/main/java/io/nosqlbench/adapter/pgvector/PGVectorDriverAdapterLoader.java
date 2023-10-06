package io.nosqlbench.adapter.pgvector;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "jdbc")
public class PGVectorDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public PGVectorDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new PGVectorDriverAdapter(parent, childLabels);
    }
}
