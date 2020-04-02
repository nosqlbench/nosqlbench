package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
public class Add implements DoubleUnaryOperator {

    private final double addend;

    public Add(double addend) {
        this.addend = addend;
    }

    @Override
    public double applyAsDouble(double operand) {
        return addend + operand;
    }
}
