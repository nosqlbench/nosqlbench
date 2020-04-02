package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.HashRange;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class AddHashRange implements LongToIntFunction {

    private final HashRange hashRange;

    public AddHashRange(int maxValue) {
        this(0, maxValue);
    }

    public AddHashRange(int minValue, int maxValue) {
        this.hashRange = new HashRange(minValue, maxValue);
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((operand + hashRange.applyAsLong(operand)) & Integer.MAX_VALUE);
    }
}
