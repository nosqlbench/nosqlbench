package io.virtdata.continuous.common;

import io.virtdata.discrete.common.ThreadSafeHash;

import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;

public class RealLongDoubleSampler implements LongToDoubleFunction {

    private final DoubleUnaryOperator f;
    private final boolean clamp;
    private final double clampMax;
    private ThreadSafeHash hash;

    public RealLongDoubleSampler(DoubleUnaryOperator parentFunc, boolean hash, boolean clamp, double clampMax) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        this.clamp = clamp;
        this.clampMax=clampMax;
    }

    @Override
    public double applyAsDouble(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        double sample =clamp ? Double.min(clampMax,f.applyAsDouble(unit)) : f.applyAsDouble(unit);
        return sample;
    }
}
