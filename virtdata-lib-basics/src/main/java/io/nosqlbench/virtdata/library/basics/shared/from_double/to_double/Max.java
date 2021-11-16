package io.nosqlbench.virtdata.library.basics.shared.from_double.to_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleUnaryOperator;

@ThreadSafeMapper
@Categories({Category.general})
public class Max implements DoubleUnaryOperator {
    private final double max;

    public Max(double max) {
        this.max = max;
    }

    @Override
    public double applyAsDouble(double operand) {
        return Double.max(max,operand);
    }
}
