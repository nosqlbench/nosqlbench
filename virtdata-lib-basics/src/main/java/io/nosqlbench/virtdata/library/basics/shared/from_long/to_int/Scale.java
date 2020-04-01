package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

/**
 * Scale the input to the
 */
@ThreadSafeMapper
public class Scale implements LongToIntFunction {

    private final double scaleFactor;

    public Scale(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    @Override
    public int applyAsInt(long value) {
        return (int) (scaleFactor * (double) value);
    }
}
