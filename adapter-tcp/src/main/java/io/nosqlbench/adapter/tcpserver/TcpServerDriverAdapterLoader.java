package io.nosqlbench.adapter.tcpserver;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "tcpserver")
public class TcpServerDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public TcpServerDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new TcpServerDriverAdapter(parent, childLabels);
    }
}
