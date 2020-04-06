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
 */
public class ResolverForClasspath implements ContentResolver {

    public static final ContentResolver INSTANCE = new ResolverForClasspath();

    private Path resolvePath(URI uri) {
        if (uri.getScheme() != null && !uri.getScheme().isEmpty()) {
            return null;
        }
        Path fspath = Path.of(uri.getPath());
        URL systemResource = ClassLoader.getSystemResource(uri.getPath());
        if (systemResource == null) {
            return null;
        }
        if (fspath.getFileSystem() == null || fspath.getFileSystem() == FileSystems.getDefault()) {
            return fspath;
        }

        URI externalUri = URI.create(systemResource.toExternalForm());
        FileSystem fs;
        try {
            fs = FileSystems.getFileSystem(externalUri);
        } catch (FileSystemNotFoundException notfound) {
            try {
                fs = FileSystems.newFileSystem(externalUri, Collections.EMPTY_MAP);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return fspath;
    }

    @Override
    public Content<?> resolve(URI uri) {
        Path path = resolvePath(uri);
        if (path==null) {
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
