package io.nosqlbench.virtdata.library.basics.shared.conversions.from_float;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
