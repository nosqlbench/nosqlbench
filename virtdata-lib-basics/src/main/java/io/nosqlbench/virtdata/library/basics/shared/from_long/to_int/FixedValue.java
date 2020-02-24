package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.annotations.Example;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

/**
 * Yield a fixed value.
 */
@ThreadSafeMapper
public class FixedValue implements LongToIntFunction {

    private final int fixedValue;

    @Example({"FixedValue(42)","always return 42"})
    public FixedValue(int value) {
        this.fixedValue = value;
    }

    @Override
    public int applyAsInt(long value) {
        return fixedValue;
    }
}
