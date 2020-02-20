package io.virtdata.libbasics.shared.conversions.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;

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
