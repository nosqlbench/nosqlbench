package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
public class Clamp implements DoubleUnaryOperator {

    private final double min;
    private final double max;

    @Example({"Clamp(1.0D,9.0D)","clamp output values between the range [1.0D, 9.0D], inclusive"})
    public Clamp(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public double applyAsDouble(double operand) {
        return Double.min(max,Double.max(min,operand));
    }
}
