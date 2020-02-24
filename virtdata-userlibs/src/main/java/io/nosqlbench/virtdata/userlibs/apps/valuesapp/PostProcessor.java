package io.nosqlbench.virtdata.userlibs.apps.valuesapp;

public interface PostProcessor extends AutoCloseable {
    void process(Object[] values);
}
