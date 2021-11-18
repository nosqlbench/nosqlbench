package io.nosqlbench.virtdata.library.curves4.discrete.common;

import io.nosqlbench.virtdata.library.basics.shared.unary_int.Hash;

import java.util.function.DoubleToIntFunction;
import java.util.function.IntUnaryOperator;

public class InterpolatingIntIntSampler implements IntUnaryOperator {

    private final double[] lut;
    private final DoubleToIntFunction f;
    private Hash hash;
    private final double scaleToIntRanged;

    public InterpolatingIntIntSampler(DoubleToIntFunction icdSource, int resolution, boolean hash) {
        this.f = icdSource;
        if (hash) {
            this.hash = new Hash();
        }
        double[] computed = precompute(resolution);
        double[] padded = new double[computed.length+1];
        System.arraycopy(computed,0,padded,0,computed.length);
        padded[padded.length-1] = padded[padded.length-2];
        scaleToIntRanged = (1.0d/Integer.MAX_VALUE)*(padded.length-2);
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
    public int applyAsInt(int input) {
        if (hash!=null) {
            input = hash.applyAsInt(input);
        }
        double samplePoint = scaleToIntRanged * input;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - leftidx;
        double sample = (lut[leftidx]* (1.0d-fractional)) + (lut[leftidx+1] * fractional);
        return (int) sample;
    }
}
