package io.virtdata.libbasics.shared.from_long.to_other;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;
/**
 * Yeilds a null if the input value is equal to the specified value.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfEq implements LongFunction<Long> {

    private final long compareto;

    public NullIfEq(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Long apply(long value) {
        if (value == compareto) return null;
        return value;
    }
}