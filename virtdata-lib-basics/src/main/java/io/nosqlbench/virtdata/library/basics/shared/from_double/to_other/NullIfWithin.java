package io.nosqlbench.virtdata.library.basics.shared.from_double.to_other;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Yields a null if the input value is within the specified range,
 * inclusive.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfWithin implements DoubleFunction<Double> {


    private final double min;
    private final double max;

    public NullIfWithin(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Double apply(double value) {
        if (value>=min && value <=max) { return null; }
        return value;
    }
}