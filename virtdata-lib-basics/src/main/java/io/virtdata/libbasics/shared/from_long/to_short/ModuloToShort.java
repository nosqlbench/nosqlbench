package io.virtdata.libbasics.shared.from_long.to_short;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.DeprecatedFunction;
import io.virtdata.annotations.ThreadSafeMapper;

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
