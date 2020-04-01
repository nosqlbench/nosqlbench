package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Return the minimum of either the input value or the specified minimum.
 */
@ThreadSafeMapper
public class Min implements LongUnaryOperator {

    private final long min;

    public Min(long min) {
        this.min = min;
    }

    @Override
    public long applyAsLong(long operand) {
        return Math.min(operand, min);
    }
}
