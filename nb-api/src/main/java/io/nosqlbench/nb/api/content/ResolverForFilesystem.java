package io.nosqlbench.nb.api.content;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResolverForFilesystem implements ContentResolver {

    public static ResolverForFilesystem INSTANCE = new ResolverForFilesystem();

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

    public String toString() {
        return this.getClass().getSimpleName();
    }

}
