package io.nosqlbench.virtdata.library.basics.shared.from_long.to_byte;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Return a byte value as the result of modulo division with the specified divisor.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ModuloToByte implements LongFunction<Byte> {

    private final long modulo;

    public ModuloToByte(long modulo) {
        this.modulo = modulo;
    }

    @Override
    public Byte apply(long value) {
        return (byte) ((value % modulo) % Byte.MAX_VALUE);
    }
}
