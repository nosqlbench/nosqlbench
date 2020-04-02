package io.nosqlbench.virtdata.library.basics.shared.from_long.to_long;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Simply returns the input value. This function intentionally does nothing.
 */
@ThreadSafeMapper
public class Identity implements LongUnaryOperator {
    @Override
    public long applyAsLong(long operand) {
        return operand;
    }
}
