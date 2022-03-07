package io.nosqlbench.virtdata.core.templates;

import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
public class TestValue implements LongFunction<Object> {

    private final Object value;

    public TestValue(Object value) {
        this.value = value;
    }
    @Override
    public Object apply(long value) {
        return this.value;
    }
}
