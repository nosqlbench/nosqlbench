package io.virtdata.libbasics.shared.diagnostics;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Yields the class of the resulting type in String form.
 */
@ThreadSafeMapper
@Categories({Category.diagnostics})
public class TypeOf implements Function<Object,String> {

    @Override
    public String apply(Object o) {
        return o.getClass().getCanonicalName().toString();
    }


}
