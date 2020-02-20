package io.virtdata.libbasics.shared.from_double.to_unset;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.api.VALUE;

import java.util.function.DoubleFunction;

/**
 * Yield VALUE.unset if the input value is close to the
 * specified value by the sigma threshold. Otherwise,
 * pass the input value along.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfCloseTo implements DoubleFunction<Object> {

    private final double compareto;
    private final double sigma;

    public UnsetIfCloseTo(double compareto, double sigma) {
        this.compareto = compareto;
        this.sigma = sigma;
    }

    @Override
    public Object apply(double value) {
        if (Math.abs(value - compareto) <= sigma) return VALUE.unset;
        return value;
    }
}