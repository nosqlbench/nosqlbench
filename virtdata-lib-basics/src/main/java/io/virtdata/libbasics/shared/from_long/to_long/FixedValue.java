package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class FixedValue implements LongUnaryOperator {

    private final long fixedValue;

    public FixedValue(long fixedValue) {
        this.fixedValue = fixedValue;
    }

    @Override
    public long applyAsLong(long operand) {
        return fixedValue;
    }
}
