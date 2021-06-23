package io.nosqlbench.virtdata.core.bindings;

import java.util.function.LongFunction;

public interface DataMapper<R> extends LongFunction<R> {
    R get(long input);

    @Override
    default R apply(long value) {
        return get(value);
    }
}
