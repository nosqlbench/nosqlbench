package io.virtdata.docsys.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Doc Path Info represents a readable Path which is part of a well-known
 * namespace.
 *
 * <pre>{@code
 * docpathinfo = {
 *     'namespace': 'example-namespace',
 *     'paths': ['path1', 'path2']
 * }
 *
 * }</pre>
 *
 * When callers are using doc paths from this interface, it is an error for
 * there to be multiple instance of a doc path of the same name in the same
 * namespace. As long as the root path of a doc tree is unique within the
 * given namespaces, they may be merged. This is to allow multiple contributors
 * of detailed docs to a single known namespace.
 *
 */
public interface DocsNameSpace extends Iterable<Path> {

    /**
     * A simple name which can be used to nest the enclosed path within a larger
     * namespace. Users of this interface should never host content from the path
     * at a root level separate from the namespace.
     *
     * @return A canonical namespace identifier
     */
    String getName();

    List<Path> getPaths();

    boolean isEnabledByDefault();
}
