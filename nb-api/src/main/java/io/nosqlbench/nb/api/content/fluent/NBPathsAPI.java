package io.nosqlbench.nb.api.content.fluent;

import io.nosqlbench.nb.api.content.Content;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public interface NBPathsAPI {

    public static interface Facets extends
        WantsSpaces, ForPrefix, WantsContentName, ForName, ForExtension {}

    public static interface WantsSpaces {
        /**
         * Only provide content from the class path and the local filesystem.
         * @return this builder
         */
        ForPrefix localContent();

        /**
         * Only return content from remote URLs. If the user is providing non-URL content
         * in this context, it is an error. Throw an error in that case.
         * @return this builder
         */
        ForPrefix remoteContent();

        /**
         * Only return content from the runtime classpath, internal resources that are bundled,
         * and do not return content on the file system.
         * @return this builder
         */
        ForPrefix internalContent();

        /**
         * Only return content from the filesystem, but not remote URLs nor internal bundled resources.
         * @return this builder
         */
        ForPrefix fileContent();

        /**
         * Return content from everywhere, from remote URls, or from the file system and then the internal
         * bundled content if not found in the file system first.
         * @return this builder
         */
        ForPrefix allContent();
    }

    public static interface ForPrefix extends WantsContentName {
        /**
         * Each of the prefix paths will be searched if the resource is not found with the exact
         * path given.
         * @param prefixPaths A list of paths to include in the search
         * @return this builder
         */
        WantsContentName prefix(String... prefixPaths);
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

        Optional<Content<?>> maybeOne();

        /**
         * Find exactly one source of content under the search parameters given.
         * It is an error if you find none, or more than one.
         * @return An optional content element.
         */
        Optional<Content<?>> one();

    }

}
