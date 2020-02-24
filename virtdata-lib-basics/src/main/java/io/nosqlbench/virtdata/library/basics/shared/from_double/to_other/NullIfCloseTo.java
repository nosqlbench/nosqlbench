package io.nosqlbench.virtdata.library.basics.shared.from_double.to_other;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Returns null if the input value is within range of the specified value.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfCloseTo implements DoubleFunction<Double> {

    private final double compareto;
    private final double sigma;

    public NullIfCloseTo(double compareto, double sigma) {
        this.compareto = compareto;
        this.sigma = sigma;
    }

    @Override
    public Double apply(double value) {
        if (Math.abs(value - compareto) <= sigma) return null;
        return value;
    }
}