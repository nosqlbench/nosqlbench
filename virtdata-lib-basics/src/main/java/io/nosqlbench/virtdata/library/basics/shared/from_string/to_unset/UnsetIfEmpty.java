package io.nosqlbench.virtdata.library.basics.shared.from_string.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.Function;

/**
 * Yield VALUE.unset if the input String is empty. Throws
 * an error if the input value is null. Otherwise,
 * passes the original value along.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class UnsetIfEmpty implements Function<String,Object> {

    @Override
    public Object apply(String s) {
        if (s!=null && s.isEmpty()) {
            return VALUE.unset;
        }
        if (s!=null) {
            return s;
        }
        throw new RuntimeException("This function is not able to take null values as input. If you need to do that, consider using NullIfNullOrEmpty()");
    }
}
