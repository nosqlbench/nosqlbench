package io.nosqlbench.virtdata.library.basics.shared.from_double.to_unset;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.VALUE;

import java.util.function.DoubleFunction;

@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfGe implements DoubleFunction<Object> {

    private final double compareto;

    public UnsetIfGe(double compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(double value) {
        if (value >= this.compareto) return VALUE.unset;
        return value;
    }
}