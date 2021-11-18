package io.nosqlbench.virtdata.library.curves4.discrete.common;

import java.util.function.DoubleToIntFunction;
import java.util.function.LongToIntFunction;

public class InterpolatingLongIntSampler implements LongToIntFunction {

    private final double[] lut;
    private final DoubleToIntFunction f;
    private ThreadSafeHash hash;
    private final double scaleToLong;

    public InterpolatingLongIntSampler(DoubleToIntFunction icdSource, int resolution, boolean hash) {
        this.f = icdSource;
        if (hash) {
            this.hash = new ThreadSafeHash();
        }
        double[] computed = precompute(resolution);
        double[] padded = new double[computed.length+1];
        System.arraycopy(computed,0,padded,0,computed.length);
        padded[padded.length-1] = padded[padded.length-2];
        scaleToLong=(1.0d/Long.MAX_VALUE) * (padded.length-2);
        this.lut=padded;
    }

    private double[] precompute(int resolution) {
        double[] precomputed = new double[resolution];
        for (int s = 0; s < resolution; s++) { // not a ranging error
            double rangedToUnit = (double) s / (double) resolution;
            int sampleValue = f.applyAsInt(rangedToUnit);
            precomputed[s] =  sampleValue;
        }
        return precomputed;
    }

    @Override
    public int applyAsInt(long input) {
        if (hash!=null) {
            input = hash.applyAsLong(input);
        }
        double samplePoint = scaleToLong * input;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - leftidx;
        double sample = (lut[leftidx]* (1.0d-fractional)) + (lut[leftidx+1] * fractional);
        return (int)sample;
    }
}
