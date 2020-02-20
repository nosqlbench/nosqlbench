package io.virtdata.libbasics.shared.from_long.to_unset;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.api.VALUE;

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