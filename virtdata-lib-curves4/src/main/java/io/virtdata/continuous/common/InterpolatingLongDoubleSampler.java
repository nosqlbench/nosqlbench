package io.virtdata.continuous.common;

import io.virtdata.discrete.common.ThreadSafeHash;

import java.util.function.DoubleUnaryOperator;
import java.util.function.LongToDoubleFunction;

public class InterpolatingLongDoubleSampler implements LongToDoubleFunction {

    private final double[] lut;
    private final DoubleUnaryOperator f;
    private int resolution;
    private final boolean clamp;
    private final double clampMax;
    private ThreadSafeHash hash;

    public InterpolatingLongDoubleSampler(DoubleUnaryOperator icdSource, int resolution, boolean hash, boolean clamp, double clampMax) {
        this.f = icdSource;
        this.resolution = resolution;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        this.clamp=clamp;
        this.clampMax=clampMax;
        this.lut = precompute();
    }

    private double[] precompute() {
        double[] precomputed = new double[resolution+2];
        for (int s = 0; s <= resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            double sampleValue = clamp ? Double.min(clampMax,f.applyAsDouble(rangedToUnit)) : f.applyAsDouble(rangedToUnit);
            precomputed[s] =  sampleValue;
        }
        precomputed[precomputed.length-1]=0.0D; // only for right of max, when S==Max in the rare case
        return precomputed;
    }

    @Override
    public double applyAsDouble(long value) {
        if (hash!=null) {
            value = hash.applyAsLong(value);
        }
        double unit = (double) value / (double) Long.MAX_VALUE;
        double samplePoint = unit * resolution;
        int leftidx = (int) samplePoint;
        double leftPartial = samplePoint - leftidx;

        double leftComponent=(lut[leftidx] * (1.0-leftPartial));
        double rightComponent = (lut[leftidx+1] * leftPartial);

        double sample = leftComponent + rightComponent;
        return sample;
    }
}
