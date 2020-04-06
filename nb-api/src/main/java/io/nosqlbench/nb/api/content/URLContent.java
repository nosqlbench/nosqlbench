package io.nosqlbench.nb.api.content;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
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
 */
public class URLContent implements Content<URL> {

    private final URL url;
    private CharBuffer buffer;
    private InputStream inputStream;

    public URLContent(URL url, InputStream inputStream) {
        this.url = url;
        this.inputStream = inputStream;
    }

    @Override
    public URL getLocation() {
        return url;
    }

    @Override
    public URI getURI() {
        try {
            return url.toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CharBuffer getCharBuffer() {
        if (buffer==null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Stream<String> lines = bufferedReader.lines();
            String buffdata = lines.collect(Collectors.joining());
            this.buffer = ByteBuffer.wrap(buffdata.getBytes(StandardCharsets.UTF_8)).asCharBuffer().asReadOnlyBuffer();
        }

        return buffer;
    }

    @Override
    public Path asPath() {
        return null;
    }
}
