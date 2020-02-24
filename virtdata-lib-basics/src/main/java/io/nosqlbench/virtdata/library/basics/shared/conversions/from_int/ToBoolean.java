package io.nosqlbench.virtdata.library.basics.shared.conversions.from_int;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToBoolean implements Function<Integer,Boolean> {

    @Override
    public Boolean apply(Integer input) {
        return ((input & 1) == 1);
    }
}
