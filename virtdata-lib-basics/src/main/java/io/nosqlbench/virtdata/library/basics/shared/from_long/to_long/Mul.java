package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return the result of multiplying the specified value with the input.
 */
@ThreadSafeMapper
public class Mul implements LongUnaryOperator {

    public Mul(long multiplicand) {
        this.multiplicand = multiplicand;
    }

    private long multiplicand;

    @Override
    public long applyAsLong(long operand) {
        return operand * multiplicand;
    }
}
