package io.nosqlbench.nb.api.content;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ResolverForFilesystem implements ContentResolver {

    public static ResolverForFilesystem INSTANCE = new ResolverForFilesystem();

    private Path resolvePath(URI uri) {
        if (uri.getScheme()!=null&&!uri.getScheme().isEmpty()&&!uri.getScheme().equals("file")) {
            return null;
        }
        Path pathFromUri = Path.of(uri.getPath());

        if (Files.isReadable(pathFromUri)) {
            return pathFromUri;
        }
        return null;
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
