package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class AddHashRange implements LongUnaryOperator {

    private final HashRange hashRange;

    public AddHashRange(long maxValue) {
        this(0, maxValue);
    }

    public AddHashRange(long minValue, long maxValue) {
        this.hashRange = new HashRange(minValue, maxValue);
    }

    @Override
    public long applyAsLong(long operand) {
        return operand + hashRange.applyAsLong(operand);
    }
}
