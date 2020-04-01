package io.nosqlbench.virtdata.library.basics.shared.conversions.from_double;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.DoubleFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToShort implements DoubleFunction<Short> {

    private final int scale;
    public ToShort() {
        this.scale = Short.MAX_VALUE;
    }
    public ToShort(int modulo) {
        this.scale = modulo;
    }

    @Override
    public Short apply(double input) {
        return (short)(((long) input) % scale);
    }
}
