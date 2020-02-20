package io.virtdata.libbasics.shared.conversions.from_short;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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
