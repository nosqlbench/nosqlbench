package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
