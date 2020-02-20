package io.virtdata.libbasics.shared.from_long.to_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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
