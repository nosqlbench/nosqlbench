package io.nosqlbench.virtdata.library.basics.shared.from_long.to_unset;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.VALUE;

import java.util.function.LongFunction;

/**
 * Yield VALUE.unset if the input value is less than
 * or equal to the specified value. Otherwise, pass
 * the value along.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfLe implements LongFunction<Object> {

    private final long compareto;

    public UnsetIfLe(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(long value) {
        if (value <= compareto) return VALUE.unset;
        return value;
    }
}