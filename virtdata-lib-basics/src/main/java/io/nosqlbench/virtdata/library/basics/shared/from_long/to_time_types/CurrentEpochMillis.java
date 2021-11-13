package io.nosqlbench.virtdata.library.basics.shared.from_long.to_time_types;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongUnaryOperator;

/**
 * Provide the millisecond epoch time as given by <pre>{@code System.currentTimeMillis()}</pre>
 * CAUTION: This does not produce deterministic test data.
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class CurrentEpochMillis implements LongUnaryOperator {
    @Override
    public long applyAsLong(long operand) {
        return System.currentTimeMillis();
    }
}
