package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_long.CycleRange;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class AddCycleRange implements LongToIntFunction {

    private final CycleRange cycleRange;

    public AddCycleRange(int maxValue) {
        this(0, maxValue);
    }

    public AddCycleRange(int minValue, int maxValue) {
        this.cycleRange = new CycleRange(minValue,maxValue);
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((operand + cycleRange.applyAsLong(operand)) & Integer.MAX_VALUE);
    }
}
