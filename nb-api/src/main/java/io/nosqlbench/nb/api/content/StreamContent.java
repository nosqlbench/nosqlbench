package io.nosqlbench.nb.api.content;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StreamContent is meant for short-lived use as an efficient way to
 * find a read URL content. If a caller has already acquired an
 * input stream, it can be passed to the stream content holder
 * to avoid double fetch or other unintuitive and inefficient
 * behavior.
 *
 * Subclasses must implement getURI()
 */
public abstract class StreamContent<T> implements Content<T> {
    private final T location;
    private final InputStream inputStream;

    public StreamContent(T location, InputStream inputStream) {
        this.location = location;
        this.inputStream = inputStream;
    }

    @Override
    public T getLocation() {
        return location;
    }

    @Override
    public CharBuffer getCharBuffer() {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        Stream<String> lines = bufferedReader.lines();
        String buffdata = lines.map(l -> l+"\n").collect(Collectors.joining());
        return CharBuffer.wrap(buffdata);
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public Path asPath() {
        return null;
    }
}
