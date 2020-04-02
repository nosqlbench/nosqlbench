package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleToLongFunction;

/**
 * Convert the input value to a long.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToLong implements DoubleToLongFunction {

    private final long scale;

    public ToLong(long scale) {
        this.scale = scale;
    }

    public ToLong() {
        this.scale = Long.MAX_VALUE;
    }

    @Override
    public long applyAsLong(double input) {
        return (long) (input % scale);
    }
}
