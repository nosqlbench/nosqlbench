package io.virtdata.libbasics.shared.from_long.to_long;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

@ThreadSafeMapper
public class Add implements LongUnaryOperator {

    private final long addend;

    public Add(long addend) {
        this.addend = addend;
    }

    @Override
    public long applyAsLong(long operand) {
        return addend + operand;
    }
}
