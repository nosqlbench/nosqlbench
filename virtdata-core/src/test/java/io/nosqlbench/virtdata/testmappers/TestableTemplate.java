package io.nosqlbench.virtdata.testmappers;

import io.nosqlbench.virtdata.annotations.ThreadSafeMapper;

import java.util.function.LongFunction;

@ThreadSafeMapper
public class TestableTemplate implements LongFunction<String> {

    private LongFunction<?>[] funcs;
    private String separator;

    public TestableTemplate(String separator, LongFunction<?>... funcs) {
        this.funcs = funcs;
        this.separator = separator;
    }

    @Override
    public String apply(long value) {
        StringBuilder sb = new StringBuilder();
        for (LongFunction<?> func : funcs) {
            sb.append(func.apply(value).toString());
            sb.append(separator);
        }
        sb.setLength(sb.length()-separator.length());
        return sb.toString();
    }
}
