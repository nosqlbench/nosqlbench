package io.nosqlbench.virtdata.library.curves4.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.IntToLongFunction;

public class InterpolatingIntLongSampler implements IntToLongFunction {

    private final double[] lut;
    private final DoubleToIntFunction f;
    private int resolution;
    private ThreadSafeHash hash;

    public InterpolatingIntLongSampler(DoubleToIntFunction icdSource, int resolution, boolean hash) {
        this.f = icdSource;
        this.resolution = resolution;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        this.lut = precompute();
    }

    private double[] precompute() {
        double[] precomputed = new double[resolution+2];
        for (int s = 0; s <= resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            int sampleValue = f.applyAsInt(rangedToUnit);
            precomputed[s] =  sampleValue;
        }
        precomputed[precomputed.length-1]=0.0D; // only for right of max, when S==Max in the rare case
        return precomputed;
    }

    @Override
    public long applyAsLong(int input) {
        int value = input;

        if (hash!=null) {
            value = (int) (hash.applyAsLong(input) % Integer.MAX_VALUE);
        }

        double unit = (double) value / (double) Integer.MAX_VALUE;
        double samplePoint = unit * resolution;
        int leftidx = (int) samplePoint;
        double leftPartial = samplePoint - leftidx;

        double leftComponent=(lut[leftidx] * (1.0-leftPartial));
        double rightComponent = (lut[leftidx+1] * leftPartial);

        double sample = leftComponent + rightComponent;
        return (long) sample;
    }
}
