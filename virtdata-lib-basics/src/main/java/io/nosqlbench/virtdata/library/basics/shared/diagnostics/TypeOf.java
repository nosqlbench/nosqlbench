package io.nosqlbench.virtdata.library.basics.shared.diagnostics;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

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
