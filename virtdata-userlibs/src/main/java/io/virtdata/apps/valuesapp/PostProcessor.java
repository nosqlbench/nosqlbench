package io.virtdata.apps.valuesapp;

public interface PostProcessor extends AutoCloseable {
    void process(Object[] values);
}
