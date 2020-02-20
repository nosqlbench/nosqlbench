package io.virtdata.libbasics.shared.unary_int;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToInt implements Function<Object,Integer> {

    @Override
    public Integer apply(Object o) {
        return Integer.valueOf(o.toString());
    }
}
