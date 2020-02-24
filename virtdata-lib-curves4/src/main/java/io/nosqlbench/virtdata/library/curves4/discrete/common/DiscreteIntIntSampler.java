package io.nosqlbench.virtdata.library.curves4.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.IntUnaryOperator;

public class DiscreteIntIntSampler implements IntUnaryOperator {

    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;

    public DiscreteIntIntSampler(DoubleToIntFunction parentFunc, boolean hash) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
    }

    @Override
    public int applyAsInt(int value) {
        if (hash!=null) {
            value = (int) (hash.applyAsLong(value));
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        int sample =f.applyAsInt(unit);
        return sample;
    }
}
