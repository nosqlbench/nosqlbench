package io.nosqlbench.virtdata.library.basics.shared.from_long.to_other;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

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
