package io.nosqlbench.nb.api.content;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.file.Path;

/**
 * A generic content wrapper for anything that can be given to a NoSQLBench runtime
 * using a specific type of locator.
 * @param <T>
 */
public interface Content<T> {

    T getLocation();
    URI getURI();
    CharBuffer getCharBuffer();
    public default String asString() {
        return getCharBuffer().toString();
    }
    Path asPath();
}
