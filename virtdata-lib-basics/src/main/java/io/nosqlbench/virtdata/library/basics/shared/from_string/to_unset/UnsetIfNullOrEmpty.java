package io.nosqlbench.virtdata.library.basics.shared.from_string.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.Function;

/**
 * Yields UNSET.value if the input value is null or empty.
 * Otherwise, passes the original value along.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class UnsetIfNullOrEmpty implements Function<String,Object> {

    @Override
    public Object apply(String s) {
        if (s==null || s.isEmpty()) {
            return VALUE.unset;
        }
        return s;
    }
}
