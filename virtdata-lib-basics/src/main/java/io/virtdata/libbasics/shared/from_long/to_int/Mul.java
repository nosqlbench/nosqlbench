package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class Mul implements LongToIntFunction {

    public Mul(int multiplicand) {
        this.multiplicand = multiplicand;
    }

    private long multiplicand;

    @Override
    public int applyAsInt(long operand) {
        return (int) ((operand * multiplicand) % Integer.MAX_VALUE);
    }
}
