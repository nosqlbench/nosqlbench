package io.nosqlbench.virtdata.library.basics.shared.from_double.to_other;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfGe implements DoubleFunction<Double> {

    private final double compareto;

    public NullIfGe(double compareto) {
        this.compareto = compareto;
    }

    @Override
    public Double apply(double value) {
        if (value >= this.compareto) return null;
        return value;
    }
}