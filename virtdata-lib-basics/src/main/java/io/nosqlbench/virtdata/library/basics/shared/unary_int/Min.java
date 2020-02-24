package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

@ThreadSafeMapper
public class Min implements IntUnaryOperator {

    private final int min;

    public Min(int min) {
        this.min = min;
    }

    @Override
    public int applyAsInt(int operand) {
        return Math.min(operand,min);
    }
}
