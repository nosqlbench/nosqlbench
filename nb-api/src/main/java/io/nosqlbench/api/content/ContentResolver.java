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

package io.nosqlbench.api.content;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public interface ContentResolver {

    /**
     * Locate the content referenced by the specified name. Content is any
     * URL or file path which contains data to be loaded.
     * <p>
     * Implementors should take care to ensure the following conditions are met:
     *
     * <UL>
     * <LI>For URL style content, resolution is only successful if a stream to download the content
     * is acquired.</LI>
     * <LI>For file paths, resolution is only successful if the filesystem does a standard access
     * check for readability of a file that is present.</LI>
     * </UL>
     *
     * A content resolver may be given a path which is fundamentally incompatible with the
     * schemes supported by the resolver implementation. It is
     * required that the resolver return null for such URI values.
     *
     * @param uri The URI of a content location, like a file name or URL.
     * @return A content element which may then be used to access the content
     */
    List<Content<?>> resolve(URI uri);
    default List<Content<?>> resolve(String uri) {
        return resolve(URI.create(uri));
    }


    /**
     * Return a list of matching directory Paths for the {@link URI} which are accessible under
     * the scheme of the implementing resolver. It is possible that a logical path can return
     * more than one physical directory path, such as with jar files.
     * @param uri
     * @return A list of accessible paths matching the uri, or an empty list
     */
    List<Path> resolveDirectory(URI uri);
    default List<Path> resolveDirectory(String uri) {
        return resolveDirectory(URI.create(uri));
    }

}
