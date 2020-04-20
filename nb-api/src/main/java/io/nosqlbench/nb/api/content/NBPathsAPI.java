package io.nosqlbench.nb.api.content;

import io.nosqlbench.nb.api.content.Content;

import java.util.List;
import java.util.Optional;

public interface NBPathsAPI {

    public static interface Facets extends
        GetSource, GetPrefix, GetName, GetExtension, DoSearch {}

    public static interface GetSource {
        /**
         * Only provide content from the class path and the local filesystem.
         * @return this builder
         */
        GetPrefix localContent();

        /**
         * Only return content from remote URLs. If the user is providing non-URL content
         * in this context, it is an error. Throw an error in that case.
         * @return this builder
         */
        GetPrefix remoteContent();

        /**
         * Only return content from the runtime classpath, internal resources that are bundled,
         * and do not return content on the file system.
         * @return this builder
         */
        GetPrefix internalContent();

        /**
         * Only return content from the filesystem, but not remote URLs nor internal bundled resources.
         * @return this builder
         */
        GetPrefix fileContent();

        /**
         * Return content from everywhere, from remote URls, or from the file system and then the internal
         * bundled content if not found in the file system first.
         * @return this builder
         */
        GetPrefix allContent();
    }

    public static interface GetPrefix extends GetName {
        /**
         * Each of the prefix paths will be searched if the resource is not found with the exact
         * path given.
         * @param prefixPaths A list of paths to include in the search
         * @return this builder
         */
        GetPrefix prefix(String... prefixPaths);
    }

    public static interface GetName extends GetExtension {
        /**
         * Provide the names of the resources to be resolved. More than one resource may be provided.
         * @param name The name of the resource to load
         * @return this builder
         */
        GetExtension name(String... name);
    }

    public static interface GetExtension extends DoSearch {
        /**
         * provide a list of optional file extensions which should be considered. If the content is
         * not found under the provided name, then each of the extensios is tried in order.
         * @param extensions The extension names to try
         * @return this builder
         */
        DoSearch extension(String... extensions);

    }

    public static interface DoSearch {
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
        List<List<Content<?>>> resolveEach();

        List<Content<?>> list();

        /**
         * Find exactly one source of content under the search parameters given.
         * It is an error if you find none, or more than one.
         * @return An optional content element.
         */
        Content<?> one();

    }

}
