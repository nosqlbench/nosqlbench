package io.virtdata.libbasics.shared.conversions.from_long;

import io.virtdata.annotations.Categories;
import io.virtdata.annotations.Category;
import io.virtdata.annotations.ThreadSafeMapper;
import io.virtdata.libbasics.shared.from_long.to_byte.LongToByte;
import io.virtdata.libbasics.shared.from_long.to_short.LongToShort;

import java.util.function.*;

@ThreadSafeMapper
@Categories({Category.conversion})
public class ToString implements LongFunction<String> {
    private final LongFunction<Object> func;

    public ToString() {
        func=(i) -> i;
    }

    public ToString(LongUnaryOperator f) {
        func = f::applyAsLong;
    }

    public ToString(LongFunction<?> f) {
        func = f::apply;
    }

    public ToString(Function<Long,?> f) {
        func = f::apply;
    }

    public ToString(LongToIntFunction f) {
        func = f::applyAsInt;
    }

    public ToString(LongToDoubleFunction f) {
        func = f::applyAsDouble;
    }

    public ToString(LongToByte f) {
        func = f::apply;
    }

    public ToString(LongToShort f) {
        func = f::apply;
    }

    public String apply(long l) {
        Object o = func.apply(l);
        return String.valueOf(o);
    }
}
