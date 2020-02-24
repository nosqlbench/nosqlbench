package io.nosqlbench.virtdata.library.basics.shared.from_long.to_other;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

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