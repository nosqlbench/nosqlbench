package io.nosqlbench.engine.clients.prometheus;

import java.util.ArrayList;

public class PValue extends ArrayList<Object> {
    public double getInstant() {
        return (double) get(0);
    }

    public String getValue() {
        return (String) get(1);
    }
}
