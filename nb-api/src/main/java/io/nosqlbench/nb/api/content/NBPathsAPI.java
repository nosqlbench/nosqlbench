/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.api.content;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface NBPathsAPI {

    interface Facets extends
        GetSource, GetPrefix, GetName, GetExtension, DoSearch {
    }

    interface GetSource {
        /**
         * Only provide content from the class path and the local filesystem.
         *
         * @return this builder
         */
        GetPrefix localContent();

        /**
         * Only return content from remote URLs. If the user is providing non-URL content
         * in this context, it is an error. Throw an error in that case.
         *
         * @return this builder
         */
        GetPrefix remoteContent();

        /**
         * Only return content from the runtime classpath, internal resources that are bundled,
         * and do not return content on the file system.
         *
         * @return this builder
         */
        GetPrefix internalContent();

        /**
         * Only return content from the filesystem, but not remote URLs nor internal bundled resources.
         *
         * @return this builder
         */
        GetPrefix fileContent();

        /**
         * Return content from everywhere, from remote URls, or from the file system and then the internal
         * bundled content if not found in the file system first.
         *
         * @return this builder
         */
        GetPrefix allContent();
    }

    interface GetPrefix extends GetName {
        /**
         * Each of the prefix paths will be searched if the resource is not found with the exact
         * path given. To be specific, if you want to search within a location based on wildcards,
         * you must provide a prefix that provides a boundary for the search.
         *
         * @param prefixPaths A list of paths to include in the search
         * @return this builder
         */
        GetPrefix prefix(String... prefixPaths);
    }

    interface GetName extends GetExtension {
        /**
         * Provide the names of the resources to be resolved. More than one resource may be provided.
         * If no name is provided, then a wildcard search is assumed.
         *
         * @param name The name of the resource to load
         * @return this builder
         */
        GetExtension name(String... name);

        /**
         * Provide a combined prefix, name and suffix in a combined form. For each search template provided,
         * the value is sliced up into the three components (prefix, name, extension) and added as if they
         * were specified separately using the following rules:
         * <ol>
         *     <li>Any suffix like <pre>{@code .name}</pre> is stripped off as the extension.</li>
         *     <li>Any literal (non-pattern) path parts are taken as the prefix.</li>
         *     <li>The remainder is taken as the name.</li>
         * </ol>
         *
         * Examples:
         * <ul>
         *     <li>{@code my/prefix/path/..?/name.txt} yields:
         *     <ul>
         *         <li>prefix: {@code my/prefix/path/}</li>
         *         <li>name: {@code ..?/name}</li>
         *         <li>extension: {@code .txt}</li>
         *     </ul>
         *     </li>
         *     <li>{@code .*.yaml} yeilds:
         *     <ul>
         *         <li>prefix: {@code ./}</li>
         *         <li>name: {@code .*}</li>
         *         <li>extension: {@code .yaml}</li>
         *     </ul>
         *     </li>
         * </ul>
         *
         * @param name
         * @return
         */
        DoSearch search(String... name);

    }

    interface GetExtension extends DoSearch {
        /**
         * provide a list of optional file extensions which should be considered. If the content is
         * not found under the provided name, then each of the extension is tried in order.
         * Any provided names are combined with the extensions to create an expanded list of
         * paths to search for. if extensions are provided without a name, then wildcards are created
         * with the extensions as suffix patterns.
         *
         * @param extensions The extension names to try
         * @return this builder
         */
        DoSearch extension(String... extensions);

    }

    interface DoSearch {
        /**
         * Return the result of resolving the resource.
         *
         * @return an optional {@code Content<?>} element.
         */
        Optional<Content<?>> first();

        /**
         * Return the result of resolving each of the resource names given. This has the same semantics
         * of {@link #first()}, except that it returns a result pair-wise for each name given.
         *
         * @return A list of optional {@code Content<?>} elements.
         */
        List<List<Content<?>>> resolveEach();

        /**
         * Provide a list of all matching content that was matched by the search qualifiers
         *
         * @return a list of content
         */
        List<Content<?>> list();

        /**
         * Return a list of paths which are comprised of the relative part
         * once the provided base has been removed from the front. This is done
         * per content item within the direct filesystem the path belongs to.
         *
         * @param base The root path elements to remove
         * @return Relative paths
         */
        List<Path> relativeTo(String... base);

        /**
         * Find exactly one source of content under the search parameters given.
         * It is an error if you find none, or more than one.
         *
         * @return An optional content element.
         */
        Content<?> one();

    }

}
