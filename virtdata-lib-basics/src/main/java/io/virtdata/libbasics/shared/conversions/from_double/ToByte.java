package io.virtdata.libbasics.shared.conversions.from_double;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

/**
 * Convert the input value to a {@code Byte}.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByte implements DoubleFunction<Byte> {

    private final int scale;
    public ToByte() {
        this.scale = Byte.MAX_VALUE;
    }
    public ToByte(int modulo) {
        this.scale = modulo;
    }

    @Override
    public Byte apply(double input) {
        return (byte)(((long) input) % scale);
    }
}
