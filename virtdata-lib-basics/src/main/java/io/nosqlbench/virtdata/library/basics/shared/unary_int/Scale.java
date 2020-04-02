package io.nosqlbench.virtdata.library.basics.shared.unary_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntUnaryOperator;

/**
 * Scale the input to the
 */
@ThreadSafeMapper
public class Scale implements IntUnaryOperator {

    private final double scaleFactor;

    public Scale(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public int applyAsInt(int operand) {
        return (int) (scaleFactor * (double) operand);
    }
}
