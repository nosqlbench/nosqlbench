package io.nosqlbench.virtdata.api;

public interface DataMapper<R> {
    R get(long input);
}
