package io.virtdata.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.LongUnaryOperator;

public class DiscreteLongLongSampler implements LongUnaryOperator {

    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;

    public DiscreteLongLongSampler(DoubleToIntFunction parentFunc, boolean hash) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
    }

    @Override
    public long applyAsLong(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        int sample =f.applyAsInt(unit);
        return sample;
    }
}
