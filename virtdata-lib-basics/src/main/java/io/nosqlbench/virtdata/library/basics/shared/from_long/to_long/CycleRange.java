package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class CycleRange implements LongUnaryOperator {

    private final long minValue;
    private final long width;

    public CycleRange(long maxValue) {
        this(0,maxValue);
    }

    public CycleRange(long minValue, long maxValue) {
        this.minValue = minValue;

        if (maxValue<minValue) {
            throw new RuntimeException("CycleRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public long applyAsLong(long operand) {
        return minValue + (operand % width);
    }
}
