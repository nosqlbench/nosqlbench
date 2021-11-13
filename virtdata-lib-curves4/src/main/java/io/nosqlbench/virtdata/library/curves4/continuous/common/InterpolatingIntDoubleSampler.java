package io.nosqlbench.virtdata.library.curves4.continuous.common;

import io.nosqlbench.virtdata.library.curves4.discrete.common.ThreadSafeHash;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;

public class InterpolatingIntDoubleSampler implements IntToDoubleFunction{

    private final double[] lut;
    private final DoubleUnaryOperator f;
    private final int resolution;
    private final boolean clamp;
    private final double clampMin;
    private final double clampMax;
    private ThreadSafeHash hash;

    public InterpolatingIntDoubleSampler(DoubleUnaryOperator icdSource, int resolution, boolean hash, boolean clamp, double clampMin, double clampMax, boolean finite) {
        this.f = icdSource;
        this.clamp = clamp;
        this.clampMin = clampMin;
        this.clampMax = clampMax;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        double[] lut = precompute(resolution);
        if (finite) {
            while (lut.length>0 && Double.isInfinite(lut[0])) {
                lut = Arrays.copyOfRange(lut,1,lut.length-1);
            }
            while (lut.length>0 && Double.isInfinite(lut[lut.length-1])) {
                lut = Arrays.copyOfRange(lut,0,lut.length-2);
            }
        }
        this.lut = lut;
        this.resolution=lut.length-1;
    }

    private double[] precompute(int resolution) {
        double[] precomputed = new double[resolution+1];
        for (int s = 0; s <= resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            double sampleValue = clamp ? Double.max(clampMin,Double.min(clampMax,f.applyAsDouble(rangedToUnit))) : f.applyAsDouble(rangedToUnit);
            precomputed[s] =  sampleValue;
        }
        precomputed[precomputed.length-1]=precomputed[precomputed.length-2]; // only for right of max, when S==Max in the rare case
        return precomputed;
    }

    @Override
    public double applyAsDouble(int input) {
        long value = input;
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
