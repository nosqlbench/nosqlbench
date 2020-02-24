package io.nosqlbench.virtdata.library.basics.shared.conversions.from_long;

import io.nosqlbench.virtdata.annotations.Categories;
import io.nosqlbench.virtdata.annotations.Category;
import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToByte implements LongFunction<Byte> {

    private final int mod;
    public ToByte() {
        this.mod = Byte.MAX_VALUE;
    }
    public ToByte(int modulo) {
        this.mod = modulo;
    }

    @Override
    public Byte apply(long input) {
        return (byte)(input % mod);
    }
}
