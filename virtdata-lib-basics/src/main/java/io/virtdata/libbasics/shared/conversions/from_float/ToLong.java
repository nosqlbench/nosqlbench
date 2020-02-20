package io.virtdata.libbasics.shared.conversions.from_float;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToLong implements Function<Float,Long> {

    private final long scale;

    public ToLong(long scale) {
        this.scale = scale;
    }

    public ToLong() {
        this.scale = Long.MAX_VALUE;
    }

    @Override
    public Long apply(Float input) {
        return input.longValue() % scale;
    }
}
