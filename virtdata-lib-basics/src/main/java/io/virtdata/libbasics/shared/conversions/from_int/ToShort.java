package io.virtdata.libbasics.shared.conversions.from_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToShort implements IntFunction<Short> {

    private final int scale;

    public ToShort() {
        this.scale = Short.MAX_VALUE;
    }

    public ToShort(int scale) {
        this.scale = scale;
    }

    @Override
    public Short apply(int input) {
        return (short) (input % scale);
    }
}
