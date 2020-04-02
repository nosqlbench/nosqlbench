package io.nosqlbench.virtdata.library.basics.shared.from_string.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Yield a null value if the input String is either null or empty.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class NullIfNullOrEmpty implements Function<String,String> {

    @Override
    public String apply(String s) {
        if (s==null || s.isEmpty()) {
            return null;
        }
        return s;
    }
}
