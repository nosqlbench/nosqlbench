package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
public class TestIdentity implements LongFunction<Object> {

    @Override
    public Object apply(long value) {
        return value;
    }
}
