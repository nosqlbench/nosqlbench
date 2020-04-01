package io.nosqlbench.virtdata.library.basics.shared.from_long.to_short;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.DeprecatedFunction;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Return a boolean value as the result of modulo division with the specified divisor.
 */
@ThreadSafeMapper
@DeprecatedFunction("This function is being replaced by ToShort(modulo) for naming consistency.")
@Categories({Category.conversion})
public class ModuloToShort implements LongFunction<Short> {

    private final long modulo;

    public ModuloToShort(long modulo) {
        this.modulo = modulo;
    }

    @Override
    public Short apply(long value) {
        return (short) ((value % modulo) & Short.MAX_VALUE);
    }
}
