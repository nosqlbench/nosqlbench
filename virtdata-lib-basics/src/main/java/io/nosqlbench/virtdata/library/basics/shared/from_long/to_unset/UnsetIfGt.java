package io.nosqlbench.virtdata.library.basics.shared.from_long.to_unset;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.core.bindings.VALUE;

import java.util.function.LongFunction;

/**
 * Yield UNSET.value if the input value is greater
 * than the specified value. Otherwise, pass the input value along.
 */
@ThreadSafeMapper
@Categories(Category.nulls)
public class UnsetIfGt implements LongFunction<Object> {

    private final long compareto;

    public UnsetIfGt(long compareto) {
        this.compareto = compareto;
    }

    @Override
    public Object apply(long value) {
        if (value > compareto) return VALUE.unset;
        return value;
    }
}
