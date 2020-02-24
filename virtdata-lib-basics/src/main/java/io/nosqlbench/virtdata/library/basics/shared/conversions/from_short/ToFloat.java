package io.nosqlbench.virtdata.library.basics.shared.conversions.from_short;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToFloat implements Function<Short,Float> {

    @Override
    public Float apply(Short input) {
        return (float) input.intValue();
    }
}
