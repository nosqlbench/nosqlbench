package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return the maximum of either the input value or the specified max.
 */
@ThreadSafeMapper
public class Max implements LongUnaryOperator {

    private final long max;

    @Example({"Max(42L)","take the value of 42L or the input, which ever is greater"})
    @Example({"Max(-42L)","take the value of -42L or the input, which ever is greater"})
    public Max(long max) {
        this.max = max;
    }

    @Override
    public long applyAsLong(long operand) {
        return Math.max(operand,max);
    }
}
