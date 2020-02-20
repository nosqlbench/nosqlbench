package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.Example;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Yield one of the specified values, rotating through them as the input value
 * increases.
 */
@ThreadSafeMapper
public class FixedValues implements LongUnaryOperator {

    private final long[] fixedValues;

    @Example({"FixedValues(3L,53L,73L)","Yield 3L, 53L, 73L, 3L, 53L, 73L, 3L, ..."})
    public FixedValues(long... values) {
        this.fixedValues = values;
    }

    @Override
    public long applyAsLong(long value) {
        int index = (int)(value % Integer.MAX_VALUE) % fixedValues.length;
        return fixedValues[index];
    }
}
