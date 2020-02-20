package io.virtdata.libbasics.shared.conversions.from_float;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInt implements Function<Double,Integer> {

    private final int scale;

    public ToInt(int scale) {
        this.scale = scale;
    }

    public ToInt() {
        this.scale = Integer.MAX_VALUE;
    }

    @Override
    public Integer apply(Double input) {
        return (int) (input % scale);
    }
}
