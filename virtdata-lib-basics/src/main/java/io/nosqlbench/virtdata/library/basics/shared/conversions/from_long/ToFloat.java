package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

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
