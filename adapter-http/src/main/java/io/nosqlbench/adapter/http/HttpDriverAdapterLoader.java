package io.nosqlbench.adapter.http;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "http")
public class HttpDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public HttpDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new HttpDriverAdapter(parent, childLabels);
    }
}
