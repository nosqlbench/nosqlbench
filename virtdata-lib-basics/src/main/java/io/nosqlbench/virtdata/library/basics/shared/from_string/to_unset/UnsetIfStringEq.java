package io.nosqlbench.virtdata.library.basics.shared.from_string.to_unset;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.VALUE;

import java.util.function.Function;

/**
 * Yields UNSET.value if the input value is equal to the
 * specified value. Throws an error if the input value
 * is null. Otherwise, passes the original value along.
 */
@Categories(Category.nulls)
@ThreadSafeMapper
public class UnsetIfStringEq implements Function<String,Object> {

    private String compareto;

    public UnsetIfStringEq(String compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(String s) {
        if (s!=null && s.equals(compareto)) {
            return VALUE.unset;
        }
        if (s!=null) {
            return s;
        }
        throw new RuntimeException("This function is not able to take null values as input. If you need to do that, consider using NullIfNullOrEmpty()");
    }
}
