package io.nosqlbench.adapter.pinecone;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "pinecone")
public class PineconeDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public PineconeDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new PineconeDriverAdapter(parent, childLabels);
    }
}
