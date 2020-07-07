package io.nosqlbench.virtdata.library.basics.shared.from_long.to_string;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
public class FixedValue implements LongFunction<String> {

    private final String value;

    public FixedValue(String value) {
        this.value = value;
    }

    @Override
    public String apply(long ignored) {
        return value;
    }
}
