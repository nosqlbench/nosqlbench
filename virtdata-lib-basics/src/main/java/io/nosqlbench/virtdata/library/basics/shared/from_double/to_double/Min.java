package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
public class Min implements DoubleUnaryOperator {

    private final double min;

    public Min(double min) {
        this.min = min;
    }

    @Override
    public double applyAsDouble(double operand) {
        return Double.min(min,operand);
    }
}
