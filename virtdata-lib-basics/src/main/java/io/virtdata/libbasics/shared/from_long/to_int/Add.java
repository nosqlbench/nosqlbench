package io.virtdata.libbasics.shared.from_long.to_int;

import io.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
public class Add implements LongToIntFunction {

    private final long addend;

    public Add(int addend) {
        this.addend = addend;
    }

    @Override
    public int applyAsInt(long value) {
        return (int) ((value + addend) % Integer.MAX_VALUE);
    }
}
