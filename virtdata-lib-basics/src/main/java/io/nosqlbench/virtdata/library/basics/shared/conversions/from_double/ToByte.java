package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

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
