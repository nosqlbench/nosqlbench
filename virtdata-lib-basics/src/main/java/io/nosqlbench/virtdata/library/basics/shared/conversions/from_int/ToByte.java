package io.nosqlbench.virtdata.library.basics.shared.conversions.from_int;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.IntFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByte implements IntFunction<Byte> {

    private final int scale;
    public ToByte() {
        this.scale = Byte.MAX_VALUE;
    }
    public ToByte(int modulo) {
        this.scale = modulo;
    }

    @Override
    public Byte apply(int input) {
        return (byte)(input % scale);
    }
}
