package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

/**
 * Convert the input value to a short.
 */
@ThreadSafeMapper
@Categories({Category.conversion})
public class ToShort implements LongFunction<Short> {

    private final int scale;

    public ToShort() {
        this.scale = Short.MAX_VALUE;
    }

    /**
     * This form allows for limiting the short values at a lower limit than Short.MAX_VALUE.
     * @param wrapat The maximum value to return.
     */
    public ToShort(int wrapat) {
        this.scale = wrapat;
    }

    @Override
    public Short apply(long input) {
        return (short) (input % scale);
    }
}
