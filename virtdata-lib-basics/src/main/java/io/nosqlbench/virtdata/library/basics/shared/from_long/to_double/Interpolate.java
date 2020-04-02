package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;


import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongToDoubleFunction;

@ThreadSafeMapper
public class Interpolate implements LongToDoubleFunction {

    private final double scale;
    private final double[] lut;
    private final static double maxLongAsDouble = (double) Long.MAX_VALUE;


    @Example({"Interpolate(0.0d,100.0d)","return a uniform double value between 0.0d and 100.0d"})
    @Example({"Interpolate(0.0d,90.0d,95.0d,98.0d,100.0d)","return a weighted double value where the first second and third quartiles are 90.0D, 95.0D, and 98.0D"})
    public Interpolate(double... values) {
        double[] doubles = new double[values.length+1];
        for (int i = 0; i < values.length; i++) { // not a ranging error
            doubles[i]=values[i];
        }
        doubles[doubles.length-1]=doubles[doubles.length-2];
        this.scale=values.length-1;
        this.lut = doubles;
    }

    @Override
    public double applyAsDouble(long input) {
        long value = input;
        double samplePoint = ((double)input / maxLongAsDouble) * scale;
        int leftidx = (int)samplePoint;
        double fractional = samplePoint - (long)samplePoint;
        double leftComponent = lut[leftidx]* (1.0d-fractional);
        double rightComponent = lut[leftidx+1] * fractional;
        double sample = (leftComponent + rightComponent);
        return sample;
    }

}
