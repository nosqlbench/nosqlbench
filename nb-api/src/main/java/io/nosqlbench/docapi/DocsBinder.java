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

package io.nosqlbench.docapi;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DocsInfo is a manifest view of a set of namespaces and included
 * paths. The namespaces act as content slots to allow content to be
 * managed by different layers of artifacts. For example, a downstream
 * artifact can replace a context by replacing the same-named namespace.
 *
 * <pre>{@code
 * docsinfo = {
 *   'namespaces': [
 *     {
 *         'namespace': 'example-namespace-1',
 *         'paths': ['path1','path2']
 *     },
 *     {
 *         'namespace': 'example-namespace-2',
 *         'paths': ['path-foo','other-path']
 *     }
 *   ]
 * }
 * }</pre>
 *
 * Internally, The namespace entries are represented by {@link DocsNameSpace} types.
 */
public interface DocsBinder extends Iterable<DocsNameSpace> {

    /**
     * The result of merging two DocsInfo elements together
     * is a combined DocsInfo element with all unique namespaces
     * occurring exactly once, and with the other namespace overwriting
     * same-named namespaces in the original element.
     *
     * @param other The DocsInfo containing namespaces to overlay into this DocsInfo
     * @return
     */
    DocsBinder merge(DocsBinder other);

    /**
     * The result of merging a DocPathInfo entry into a DocsInfo element
     * is the combined DocsInfo element with the added entry overwriting
     * any namespace of the same name.
     *
     * @param other The namespace entry to overlay into this DocsInfo
     * @return
     */
    DocsBinder merge(DocsNameSpace other);

    /**
     * The result of removing a set of namespaces from a DocsInfo element
     * is a new DocsInfo element containing only the namespaces which were
     * removed. The original DocsInfo element is modified statefully in this
     * case. It is not an error to remove namespaces which are not present
     * in the original DocsInfo. If this condition is important, check for it
     * manually.
     *
     * @param namespaces The names of the namespaces to remove, should they
     *                  be present.
     * @return A new DocsInfo object representing what was actually removed.
     */
    DocsBinder remove(Set<String> namespaces);

    /**
     * @return All paths in all namespaces are returned, in no guaranteed order.
     */
    List<Path> getPaths();

    /**
     * @return A map of all namespaces to each set of provided paths is returned.
     */
    Map<String, Set<Path>> getPathMap();

    List<DocsNameSpace> getNamespaces();
}
