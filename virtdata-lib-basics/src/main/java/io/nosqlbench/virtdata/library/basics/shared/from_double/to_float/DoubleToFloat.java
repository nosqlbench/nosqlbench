package io.nosqlbench.virtdata.library.basics.shared.from_double.to_float;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Convert the input double value to the closest float value.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class DoubleToFloat implements DoubleFunction<Float> {
    @Override
    public Float apply(double value) {
        return (float) value;
    }
}
