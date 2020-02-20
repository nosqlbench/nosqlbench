package io.virtdata.libbasics.shared.from_double.to_double;

import io.virtdata.annotations.ThreadSafeMapper;

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
