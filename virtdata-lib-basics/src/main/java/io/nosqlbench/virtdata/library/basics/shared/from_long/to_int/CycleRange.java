package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class CycleRange implements LongToIntFunction {

    private final long minValue;
    private final long width;

    public CycleRange(int maxValue) {
        this(0,maxValue);
    }

    public CycleRange(int minValue, int maxValue) {
        this.minValue = minValue;

        if (maxValue<minValue) {
            throw new RuntimeException("CycleRange must have min and max value in that order.");
        }
        this.width = maxValue - minValue;
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((minValue + (operand % width)) & Integer.MAX_VALUE);
    }
}
