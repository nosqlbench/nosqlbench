package io.nosqlbench.virtdata.library.basics.shared.from_string.to_unset;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.Function;

/**
 * Yields a null if the input String is empty. Throws an error if the input
 * String is null.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class NullIfEmpty implements Function<String,String> {

    @Override
    public String apply(String s) {
        if (s!=null && s.isEmpty()) {
            return null;
        }
        if (s!=null) {
            return s;
        }
        throw new RuntimeException("This function is not able to take null values as input. If you need to do that, consider using NullIfNullOrEmpty()");
    }
}
