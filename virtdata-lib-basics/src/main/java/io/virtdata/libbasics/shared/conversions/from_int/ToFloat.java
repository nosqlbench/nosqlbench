package io.virtdata.libbasics.shared.conversions.from_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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
