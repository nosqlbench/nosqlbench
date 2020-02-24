package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Convert the input value into a float.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToFloat implements DoubleFunction<Float> {
    private final double scale;

    public ToFloat(double scale) {
        this.scale = scale;
    }
    public ToFloat() {
        this.scale = Float.MAX_VALUE;
    }

    @Override
    public Float apply(double input) {
        return (float) (input % scale);
    }
}
