package io.virtdata.libbasics.shared.conversions.from_short;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToFloat implements Function<Short,Float> {

    @Override
    public Float apply(Short input) {
        return (float) input.intValue();
    }
}
