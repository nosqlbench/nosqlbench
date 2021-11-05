package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Provide the elapsed nano time since the process started.
 * CAUTION: This does not produce deterministic test data.
 */
@ThreadSafeMapper
public class ElapsedNanoTime implements LongUnaryOperator {
    @Override
    public long applyAsLong(long operand) {
        return System.nanoTime();
   }
}
