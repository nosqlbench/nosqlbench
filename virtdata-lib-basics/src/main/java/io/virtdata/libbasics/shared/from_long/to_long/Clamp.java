package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Clamp the output values to be at least the minimum value and
 * at most the maximum value.
 */
@ThreadSafeMapper
public class Clamp implements LongUnaryOperator {

    private final long min;
    private final long max;

    @Example({"Clamp(4L,400L)","clamp the output values in the range [4L,400L], inclusive"})
    public Clamp(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public long applyAsLong(long operand) {
        return Long.min(max,Long.max(min,operand));
    }
}
