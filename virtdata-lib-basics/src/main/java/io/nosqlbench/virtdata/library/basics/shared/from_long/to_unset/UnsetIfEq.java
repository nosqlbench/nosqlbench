package io.nosqlbench.virtdata.library.basics.shared.from_long.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.api.bindings.VALUE;

import java.util.function.LongFunction;

/**
 * Yield UNSET.vale if the input value is equal to the
 * specified value. Otherwise, pass the input value along.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfEq implements LongFunction<Object> {

    private final long compareto;

    public UnsetIfEq(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(long value) {
        if (value == compareto) return VALUE.unset;
        return value;
    }
}
