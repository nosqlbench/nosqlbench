package io.nosqlbench.virtdata.library.basics.shared.from_double.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.DoubleFunction;

/**
 * Yields UNSET.value if the input value is within the specified
 * range, inclusive. Otherwise, passes the original value along.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfWithin implements DoubleFunction<Object> {


    private final double min;
    private final double max;

    public UnsetIfWithin(double min, double max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public Object apply(double value) {
        if (value>=min && value <=max) { return VALUE.unset; }
        return value;
    }
}
