package io.nosqlbench.virtdata.library.curves4.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.IntToLongFunction;

public class DiscreteIntLongSampler implements IntToLongFunction {

    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;

    public DiscreteIntLongSampler(DoubleToIntFunction parentFunc, boolean hash) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
    }

    @Override
    public long applyAsLong(int input) {
        int value = input;

        if (hash!=null) {
            value = (int) (hash.applyAsLong(value) % Integer.MAX_VALUE);
        }
        double unit = (double) value / (double) Integer.MAX_VALUE;
        int sample =f.applyAsInt(unit);
        return sample;
    }
}
