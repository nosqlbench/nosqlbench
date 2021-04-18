package io.nosqlbench.nb.api.content;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResolverForFilesystem implements ContentResolver {

    public static ResolverForFilesystem INSTANCE = new ResolverForFilesystem();
    private final static Logger logger = LogManager.getLogger(ResolverForFilesystem.class);

    @Override
    public List<Content<?>> resolve(URI uri) {
        List<Content<?>> contents = new ArrayList<>();
        Path path = resolvePath(uri);

        if (path != null) {
            contents.add(new PathContent(path));
        }
        return contents;
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        List<Path> dirs = new ArrayList<>();

        Path path = resolvePath(uri);
        if (path!=null && Files.isDirectory(path)) {
            dirs.add(path);
        }
        return dirs;
    }

    private Path resolvePath(URI uri) {
        if (uri.getScheme() != null && !uri.getScheme().isEmpty() && !uri.getScheme().equals("file")) {
            return null;
        }
        Path pathFromUri = Path.of(uri.getPath());

        if (Files.isReadable(pathFromUri)) {
            return pathFromUri;
        }
        return null;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

}
