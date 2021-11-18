package io.nosqlbench.virtdata.library.curves4.continuous.common;

import io.nosqlbench.virtdata.library.curves4.discrete.common.ThreadSafeHash;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

public class RealIntDoubleSampler implements IntToDoubleFunction {

    private final DoubleUnaryOperator f;
    private final boolean clamp;
    private final double clampMax;
    private final double clampMin;
    private ThreadSafeHash hash;

    public RealIntDoubleSampler(DoubleUnaryOperator parentFunc, boolean hash, boolean clamp, double clampMin, double clampMax, boolean finite) {
        this.f = parentFunc;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        this.clamp = clamp | finite;
        this.clampMin = Double.max(clampMin,Double.MIN_VALUE);
        this.clampMax = Double.min(clampMax,Double.MAX_VALUE);
    }

    @Override
    public double applyAsDouble(int input) {
        long value = input;
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        double sample =clamp ? Double.max(Double.min(clampMax,f.applyAsDouble(unit)),clampMin): f.applyAsDouble(unit);
        return sample;
    }
}
