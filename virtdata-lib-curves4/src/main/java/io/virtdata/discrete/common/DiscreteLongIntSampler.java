package io.virtdata.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.LongToIntFunction;

public class DiscreteLongIntSampler implements LongToIntFunction {

    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;

    public DiscreteLongIntSampler(DoubleToIntFunction parentFunc, boolean hash) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
    }

    @Override
    public int applyAsInt(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        int sample =f.applyAsInt(unit);
        return sample;
    }
}
