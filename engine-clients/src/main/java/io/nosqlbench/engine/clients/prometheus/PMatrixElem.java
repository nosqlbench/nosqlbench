package io.nosqlbench.engine.clients.prometheus;

public class PMatrixElem {
    PMetric metric;
    PValues values;

    @Override
    public String toString() {
        return "PMatrixElem{" +
                "metric=" + metric +
                ", values=" + values +
                '}';
    }
}
