package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.errors.BasicError;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A central API for finding and accessing Paths which are either in
 * the classpath or in the file system, or both.
 */
public class PathFinder {

    private final static Logger logger = LogManager.getLogger(PathFinder.class);

    /**
     * Find exactly zero or one matching Paths, and return an {@link Optional} of {@link Path}.
     * Or, if more than one are found, throw a basic error.
     *
     * @param names The names of paths to find, with tailing slashes signifying directories
     * @return An optional Path
     * @throws BasicError if there is more than one matching path found.
     */
    public static Optional<Path> find(String... names) {
        List<Path> paths = findAll(names);
        if (paths.size() == 0) {
            return Optional.empty();
        }
        if (paths.size() > 1) {
            throw new BasicError("Found " + paths.size() + " paths, when only one is allowed:" +
                paths.stream()
                    .map(p -> p.toString() + " on " + p.getFileSystem())
                    .collect(Collectors.joining(","))
            );
        }
        return Optional.of(paths.get(0));
    }

    /**
     * Find one or more matching path, and return a list.
     *
     * @param pathspecs The names of paths to search for.
     * @return A list of matching paths, possibly empty.
     */
    static List<Path> findAll(String... pathspecs) {
        List<Path> founds = new ArrayList<>();

        for (String pathspec : pathspecs) {

            boolean wantsADirectory = pathspec.endsWith(FileSystems.getDefault().getSeparator());
            String candidatePath = wantsADirectory ? pathspec.substring(0, pathspec.length() - 1) : pathspec;
            Path candidate = Path.of(candidatePath);

            findPathOnRemoteURL(pathspec).ifPresent(founds::add);
            findPathOnFilesystem(candidatePath, wantsADirectory).ifPresent(founds::add);
            findPathOnClasspath(candidatePath, wantsADirectory).ifPresent(founds::add);
        }
        return founds;
    }

    private static Optional<Path> findPathOnRemoteURL(String pathspec) {
        if (pathspec.toLowerCase().startsWith("http:") ||
            pathspec.toLowerCase().startsWith("https:")) {
            Optional<InputStream> inputStreamForUrl = getInputStreamForUrl(pathspec);
            if (inputStreamForUrl.isPresent()) {
                Path found = Path.of(URI.create(pathspec));
                logger.debug("Found accessible remote file at " + found.toString());
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }

    private static Optional<Path> findPathOnClasspath(String candidatePath, boolean wantsADirectory) {

        try {
            URL url = ClassLoader.getSystemResource(candidatePath);
            if (url != null) {
                URI uri = URI.create(url.toExternalForm());

                FileSystem fileSystem = null;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException retried) {
                    try {
                        fileSystem = FileSystems.newFileSystem(uri, new HashMap<>());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                logger.debug("Found path in classpath: " + candidatePath + ": " + candidatePath);
                return Optional.of(Path.of(uri));
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.trace("Error while looking in classpath for " + candidatePath, e);
            return Optional.empty();
        }
    }

    private static Optional<Path> findPathOnFilesystem(String pathName, boolean wantsADirectory) {
        try {
            Path candidatePath = Path.of(pathName);
            FileSystemProvider provider = candidatePath.getFileSystem().provider();
            provider.checkAccess(candidatePath, AccessMode.READ);
            BasicFileAttributes attrs = provider.readAttributes(candidatePath, BasicFileAttributes.class);
            boolean foundADirectory = attrs.isDirectory();
            if (wantsADirectory != foundADirectory) {
                throw new RuntimeException("for path " + candidatePath + ", user wanted a " +
                    (wantsADirectory ? "directory" : "file") + ", but found a " +
                    (foundADirectory ? "directory" : "file") + " while searching for " +
                    pathName);
            }
            return Optional.of(candidatePath);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }


    private static Optional<InputStream> getInputStreamForUrl(String path) {
        URL url;
        try {
            url = new URL(path);
            InputStream inputStream = url.openStream();
            return Optional.ofNullable(inputStream);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}

