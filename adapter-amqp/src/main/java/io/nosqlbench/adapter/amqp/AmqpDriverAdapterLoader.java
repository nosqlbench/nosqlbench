package io.nosqlbench.adapter.amqp;

import io.nosqlbench.adapter.diag.DriverAdapterLoader;
import io.nosqlbench.api.labels.NBLabels;
import io.nosqlbench.components.NBComponent;
import io.nosqlbench.nb.annotations.Service;

@Service(value = DriverAdapterLoader.class, selector = "amqp")
public class AmqpDriverAdapterLoader implements DriverAdapterLoader {
    @Override
    public AmqpDriverAdapter load(NBComponent parent, NBLabels childLabels) {
        return new AmqpDriverAdapter(parent, childLabels);
    }
}
