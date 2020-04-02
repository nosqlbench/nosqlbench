package io.nosqlbench.virtdata.library.basics.shared.from_double.to_other;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
