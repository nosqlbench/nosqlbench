package io.virtdata.libbasics.shared.conversions.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToFloat implements LongFunction<Float> {
    private final long scale;

    public ToFloat(long scale) {
        this.scale = scale;
    }
    public ToFloat() {
        this.scale = Long.MAX_VALUE;
    }

    @Override
    public Float apply(long input) {
        return (float)(input % scale);
    }
}
