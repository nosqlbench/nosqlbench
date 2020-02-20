package io.virtdata.libbasics.shared.from_double.to_other;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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