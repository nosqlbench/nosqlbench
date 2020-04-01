package io.nosqlbench.virtdata.library.basics.shared.from_long.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongToDoubleFunction;

/**
 * Convert the input value to a double.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToDouble implements LongToDoubleFunction {

    @Override
    public double applyAsDouble(long value) {
        return (double) value;
    }
}
