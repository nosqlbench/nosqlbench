package io.nosqlbench.virtdata.library.basics.shared.conversions.from_short;

import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.Function;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByte implements Function<Short,Byte> {

    private final int scale;

    public ToByte(int scale) {
        this.scale = scale;
    }
    public ToByte() {
        this.scale = Byte.MAX_VALUE;
    }

    @Override
    public Byte apply(Short input) {
        return (byte) (input.intValue() % scale);
    }
}
