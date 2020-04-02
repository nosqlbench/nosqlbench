package io.nosqlbench.virtdata.library.basics.shared.from_long.to_other;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Yields a null if the input value is greater than the
 * specified value.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class NullIfGt implements LongFunction<Long> {

    private final long compareto;

    public NullIfGt(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Long apply(long value) {
        if (value > compareto) return null;
        return value;
    }
}
