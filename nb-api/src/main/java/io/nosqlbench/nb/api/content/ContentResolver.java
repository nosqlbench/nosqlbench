package io.nosqlbench.nb.api.content;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public interface ContentResolver {

    /**
     * Locate the content referenced by the specified name. Content is any
     * URL or file path which contains data to be loaded.
     * <p>
     * Implementors should take care to ensure the following conditions are met:
     *
     * <UL>
     * <LI>For URL style content, resolution is only successful if a stream to download the content
     * * is acquired.</LI>
     * <LI>For file paths, resolution is only successful if the filesystem does a standard access
     * * check for readability of a file that is present.</LI>
     * </UL>
     *
     * A content resolver may be given a path which is fundamentally the scheme. It is
     * required that the resolver return null for such URI values.
     *
     * @param uri The URI of a content location, like a file name or URL.
     * @return A content element which may then be used to access the content
     */
    Content<?> resolve(URI uri);

    default Content<?> resolve(String uri) {
        return resolve(URI.create(uri));
    }

    Optional<Path> resolveDirectory(URI uri);

    default Optional<Path> resolveDirectory(String uri) {
        return resolveDirectory(URI.create(uri));
    }

}
