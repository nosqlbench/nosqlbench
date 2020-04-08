package io.nosqlbench.nb.api.content;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * A generic content wrapper for anything that can be given to a NoSQLBench runtime
 * using a specific type of locator.
 * @param <T>
 */
public interface Content<T> extends Supplier<CharSequence>, Comparable<Content<?>> {

    T getLocation();
    URI getURI();
    Path asPath();

    public default String asString() {
        return getCharBuffer().toString();
    }

    CharBuffer getCharBuffer();
    @Override
    default CharSequence get() {
        return getCharBuffer();
    }

    default int compareTo(Content<?> other) {
        return getURI().compareTo(other.getURI());
    }

}
