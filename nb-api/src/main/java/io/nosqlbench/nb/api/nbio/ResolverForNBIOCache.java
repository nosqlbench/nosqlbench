/*
 * Copyright (c) 2024 nosqlbench
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
 *
 */

package io.nosqlbench.nb.api.nbio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResolverForNBIOCache implements ContentResolver {
    public static final ResolverForNBIOCache INSTANCE = new ResolverForNBIOCache();
    private final static Logger logger = LogManager.getLogger(ResolverForNBIOCache.class);
    //TODO: This needs to be set somehow - envvar, yaml setting, etc.
    private static final String cache = "/tmp/nbio-cache";
    @Override
    public List<Content<?>> resolve(URI uri) {
        List<Content<?>> contents = new ArrayList<>();
        Path path = resolvePath(uri);

        if (path != null) {
            contents.add(new PathContent(path));
        }
        return contents;
    }

    private Path resolvePath(URI uri) {
        /*
         * 1st time through this will just be the name of the file. On the second path it will include the full
         * URI, including the scheme (eg file:/// or https://, etc.). Since we need to at least verify the
         * existence of the remote file, and more typically compare checksums, we don't do anything until
         * we get the full URI
         */
        if (uri.getScheme() != null && !uri.getScheme().isEmpty() &&
            (uri.getScheme().equalsIgnoreCase("http") ||
                uri.getScheme().equalsIgnoreCase("https"))) {

            Path cachePath = Path.of(cache + uri.getPath());
            if (Files.isReadable(cachePath)) {
                return pathFromLocalCache(cachePath, uri);
            }
            else {
                return pathFromRemoteUrl(uri);
            }
        }
        return null;
    }

    private Path pathFromRemoteUrl(URI uri) {
        URLContent urlContent = resolveURI(uri);
        /*
         * File is not in cache - next steps:
         * 1. Download the file and put it in the cache
         *   1a. If the remote file does not exist throw an exception
         *   1b. If the download fails jump to step 6
         * 2. Download the checksum and put it in the cache
         * 3. Generate a new checksum for the file
         * 4. compare the checksums
         * 5. If they match, return the path to the file in the cache
         * 6. If they don't match/exception downloading repeat steps 1-5 up to a configurable number of times
         *   6a. If the max attempts have been exceeded throw an exception and clean up the cache
         */
        return null;
    }

    private Path pathFromLocalCache(Path cachePath, URI uri) {
        /*
         * File is in cache - next steps:
         * 1. Check "force update" option
         *   1a. If true remove file from cache and go to "File is not in cache" operations
         *   1b. If not specified default to false
         * 2. Check for existence of remote file
         *   2a. If the remote file does not exist generate warning message and return local file
         * 3. Check "checksum verification" option (default = true)
         *   3a. If false generate warning message and return local file
         * 4. If a local checksum exists compare it against the remote checksum
         *   4a. If none exists generate a new one and compare it against the remote checksum
         * 5. If checksums match return the local file
         * 6. If checksums do not match remove the local file and go to "File is not in cache" operations
         */
        return null;
    }

    private URLContent resolveURI(URI uri) {
        return null;
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
}
