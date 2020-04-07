package io.nosqlbench.nb.api.content;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.Optional;

/**
 * Resolves resources which can be found via the class loader.
 * <p>
 * If a stream for a named resource is found, this resolver will
 * provide a Path to it. If the classpath resolves a stream which
 * contains a {@code file:} URI, then it is returned as a simple
 * Path uri using the provided path component.
 * If it is any other scheme, then filesystems are instantiated
 * as needed to fully-qualify the resulting path, and then it
 * is provided in external form.
 */
public class ResolverForClasspath implements ContentResolver {

    public static final ContentResolver INSTANCE = new ResolverForClasspath();

    private Path resolvePath(URI uri) {
        if (uri.getScheme() != null && !uri.getScheme().isEmpty()) {
            return null;
        }
        URL systemResource = ClassLoader.getSystemResource(uri.getPath());
        if (systemResource == null) {
            return null;
        }
        URI resolved = URI.create(systemResource.toExternalForm());
        if (resolved.getScheme().equals("file")) {
            Path current = Paths.get("").toAbsolutePath();
            Path logical = Path.of(resolved.getPath());
            Path relativePath = current.relativize(logical);
            return relativePath;
        }

        FileSystem fs;
        try {
            fs = FileSystems.getFileSystem(resolved);
        } catch (FileSystemNotFoundException notfound) {
            try {
                fs = FileSystems.newFileSystem(resolved, Collections.EMPTY_MAP);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path fspath = Path.of(resolved);
        return fspath;
    }

    @Override
    public Content<?> resolve(URI uri) {
        Path path = resolvePath(uri);
        if (path == null) {
            return null;
        }
        return new PathContent(path);
    }

    @Override
    public Optional<Path> resolveDirectory(URI uri) {
        Path path = resolvePath(uri);
        if (path == null) {
            return Optional.empty();
        }
        if (Files.isDirectory(path)) {
            return Optional.of(path);
        } else {
            return Optional.empty();
        }
    }

}
