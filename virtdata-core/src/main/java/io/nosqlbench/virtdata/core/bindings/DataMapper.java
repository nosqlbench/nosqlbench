package io.nosqlbench.virtdata.core.bindings;

public interface DataMapper<R> {
    R get(long input);
}
