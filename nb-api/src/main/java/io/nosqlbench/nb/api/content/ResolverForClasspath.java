package io.nosqlbench.nb.api.content;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

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

    private List<Path> resolvePaths(URI uri) {
        List<Path> paths = new ArrayList<>();

        if (uri.getScheme() != null && !uri.getScheme().isEmpty()) {
            return null;
        }
//        URL systemResource = ClassLoader.getSystemResource(uri.getPath());
        try {
            Enumeration<URL> systemResources = ClassLoader.getSystemResources(uri.getPath());
            while (systemResources.hasMoreElements()) {
                URL url = systemResources.nextElement();
                Path p = normalize(url);
                paths.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return paths;
    }

    private Path normalize(URL url) {
        if (url == null) {
            return null;
        }
        URI resolved = URI.create(url.toExternalForm());
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
    public List<Content<?>> resolve(URI uri) {
        List<Path> paths = resolvePaths(uri);
        List<Content<?>> contents = paths.stream().map(PathContent::new).collect(Collectors.toList());
        return contents;

    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        List<Path> path = resolvePaths(uri);
        List<Path> dirs = new ArrayList<>();
        for (Path dirpath : path) {
            if (Files.isDirectory(dirpath)) {
                dirs.add(dirpath);
            }
        }
        return dirs;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

}
