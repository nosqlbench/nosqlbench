package io.nosqlbench.virtdata.library.basics.shared.diagnostics;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
