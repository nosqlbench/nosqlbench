package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
