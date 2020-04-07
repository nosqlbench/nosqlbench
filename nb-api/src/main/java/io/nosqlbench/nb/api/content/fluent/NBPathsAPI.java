package io.nosqlbench.nb.api.content.fluent;

import io.nosqlbench.nb.api.content.Content;

import java.util.List;
import java.util.Optional;

public interface NBPathsAPI {

    public static interface Facets extends
        WantsSpaces, ForContentSource, ForPrefix, WantsContentName, ForName, ForExtension {}

    public static interface WantsSpaces {
        /**
         * Only provide content from the class path and the local filesystem.
         * @return this builder
         */
        ForContentSource localContent();

        /**
         * Only return content from remote URLs. If the user is providing non-URL content
         * in this context, it is an error. Throw an error in that case.
         * @return this builder
         */
        ForContentSource remoteContent();

        /**
         * Only return content from the runtime classpath, internal resources that are bundled,
         * and do not return content on the file system.
         * @return this builder
         */
        ForContentSource internalContent();

        /**
         * Only return content from the filesystem, but not remote URLs nor internal bundled resources.
         * @return this builder
         */
        ForContentSource fileContent();

        /**
         * Return content from everywhere, from remote URls, or from the file system and then the internal
         * bundled content if not found in the file system first.
         * @return this builder
         */
        ForContentSource allContent();
    }

    public static interface ForContentSource extends ForPrefix {
        /**
         * Each of the prefix paths will be searched if the resource is not found with the exact
         * path given.
         * @param prefixPaths A list of paths to include in the search
         * @return this builder
         */
        ForPrefix prefix(String... prefixPaths);
    }

    public static interface ForPrefix extends WantsContentName {
        /**
         * Only look at exact matches of the names as given, and if not found, look for exact matches
         * of the path directly within each given search directory.
         * @return this builders
         */
        WantsContentName exact();

        /**
         * Attempt {@link #exact()} matching, and if not found, also attempt to look within any
         * provided search directories recursively for a path which matches the provided path at
         * the end. For example "baz.csv" will be found under search directory "foo" if it is at
         * "foo/bar/baz.csv", as will "bar/baz.csv", so long as "foo" is specified as a search directory.
         * @return this builder
         */
        WantsContentName matchtail();

        /**
         * Attempt {@link #exact()} matching, and if not found, search in each provided search directory
         * for a path name that matches the provided name as a regex pattern.
         * @return this builder
         */
        WantsContentName regex();
    }

    public static interface WantsContentName {
        /**
         * Provide the names of the resources to be resolved. More than one resource may be provided.
         * @param name The name of the resource to load
         * @return this builder
         */
        ForName name(String... name);
    }

    public static interface ForName extends ForExtension {
        /**
         * provide a list of optional file extensions which should be considered. If the content is
         * not found under the provided name, then each of the extensios is tried in order.
         * @param extensions The extension names to try
         * @return this builder
         */
        ForExtension extension(String... extensions);

    }

    public static interface ForExtension {
        /**
         * Return the result of resolving the resource.
         * @return an optional {@code Content<?>} element.
         */
        Optional<Content<?>> first();

        /**
         * Return the result of resolving each of the resource names given. This has the same semantics
         * of {@link #first()}, except that it returns a result pair-wise for each name given.
         * @return A list of optional {@code Content<?>} elements.
         */
        List<Optional<Content<?>>> resolveEach();

        List<Content<?>> list();
    }

}
