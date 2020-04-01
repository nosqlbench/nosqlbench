package io.nosqlbench.virtdata.library.basics.shared.conversions.from_int;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.IntFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToFloat implements IntFunction<Float> {
    private final int scale;

    public ToFloat(int scale) {
        this.scale = scale;
    }

    public ToFloat() {
        this.scale = Integer.MAX_VALUE;
    }

    @Override
    public Float apply(int input) {
        return (float) (input % scale);
    }
}
