package io.virtdata.libbasics.shared.from_double.to_other;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfLe implements DoubleFunction<Double> {

    private final double compareto;

    public NullIfLe(double compareto) {
        this.compareto = compareto;
    }

    @Override
    public Double apply(double value) {
        if (value <= compareto) return null;
        return value;
    }
}