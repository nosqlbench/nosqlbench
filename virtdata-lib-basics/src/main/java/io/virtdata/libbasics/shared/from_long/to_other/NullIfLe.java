package io.virtdata.libbasics.shared.from_long.to_other;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Yields a null if the input value is less than or equal to
 * the specified value.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfLe implements LongFunction<Long> {

    private final long compareto;

    public NullIfLe(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Long apply(long value) {
        if (value <= compareto) return null;
        return value;
    }
}