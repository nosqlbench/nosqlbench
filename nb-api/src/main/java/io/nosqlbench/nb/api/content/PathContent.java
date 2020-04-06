package io.nosqlbench.nb.api.content;

import java.io.IOException;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * PathContent provides the Path-centric way of accessing
 * resolved content from the URIs API.
 */
public class PathContent implements Content<Path> {

    private final Path path;

    public PathContent(Path path) {
        this.path = path;
    }

    @Override
    public Path getLocation() {
        return path;
    }


    @Override
    public URI getURI() {
        return path.toUri();
    }

    @Override
    public CharBuffer getCharBuffer() {
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return CharBuffer.wrap(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Path asPath() {
        return this.path;
    }

}
