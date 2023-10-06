package io.nosqlbench.adapter.tcpclient;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "tcpclient")
public class TcpClientDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public TcpClientDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new TcpClientDriverAdapter(parent, childLabels);
    }
}
