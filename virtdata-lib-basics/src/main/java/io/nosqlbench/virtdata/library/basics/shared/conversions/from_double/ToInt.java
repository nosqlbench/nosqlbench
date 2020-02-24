package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleToIntFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInt implements DoubleToIntFunction {

    private final int scale;

    public ToInt(int scale) {
        this.scale = scale;
    }

    public ToInt() {
        this.scale = Integer.MAX_VALUE;
    }

    @Override
    public int applyAsInt(double input) {
        return (int) (input % scale);
    }
}
