package io.nosqlbench.virtdata.library.basics.shared.from_long.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.LongFunction;

/**
 * Yield VALUE.unset if the provided value is less than the specified value,
 * otherwise, pass the original value;
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfLt implements LongFunction<Object> {

    private final long compareto;

    public UnsetIfLt(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(long value) {
        if (value < compareto) return VALUE.unset;
        return value;
    }
}
