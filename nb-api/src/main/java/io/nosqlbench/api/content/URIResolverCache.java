/*
 * Copyright (c) 2023 nosqlbench
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

import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.system.NBStatePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.regex.Pattern;

public class URIResolverCache implements ContentResolver {

    public final static Logger logger = LogManager.getLogger(URIResolverCache.class);

    private final Path cachedir;
    private final ResolverForURL internalResolver;
    private final long expirySeconds;
    private final Pattern matcher;

    public URIResolverCache(ResolverForURL resolver) {
        this(resolver,Pattern.compile(".+\\..+").pattern(),Long.MAX_VALUE);
    }
    public URIResolverCache(ResolverForURL resolver, String matcher, long expirySeconds) {
        this.expirySeconds=expirySeconds;
        this.matcher = Pattern.compile(matcher);
        this.internalResolver = resolver;
        cachedir = NBStatePath.initialize().resolve("filecache");

        if (!Files.exists(cachedir)) try {
            Files.createDirectories(
                cachedir,
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwx---"))
            );
            logger.debug(() -> "Created remote file cache at " + cachedir);
        } catch (final IOException e) {
            throw new BasicError("Could not create state directory at '" + cachedir + "': " + e.getMessage());
        }
    }

    @Override
    public List<Content<?>> resolve(URI uri) {
        if (!matcher.matcher(uri.toString()).matches()) {
            logger.debug(() -> "bypassing file cache for " + uri + " since it does not match pattern '" + matcher.pattern() + "'");
            return internalResolver.resolve(uri);
        }

        String path = uri.getPath();
        String[] pathParts = path.split("/");
        Path cachedAt = cachedir.resolve(Path.of(pathParts[pathParts.length-1]));
        if (Files.exists(cachedAt) && freshOrRefresh(cachedAt)) {
            logger.debug(() -> "returning cached file from '" + cachedAt +"'");
            return List.of(new PathContent(cachedAt));
        }

        List<Content<?>> loaded = internalResolver.resolve(uri);
        if (loaded.size()!=1) {
            throw new RuntimeException("Found " + loaded.size() + " results when loading " + uri);
        }
        Content<?> found = loaded.get(0);
        try {
            if (Files.exists(cachedAt)) {
                Files.delete(cachedAt);
            }
            Files.write(cachedAt,found.asString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Unable to save file in cache: " + e, e);
        }
        logger.info("Cached and served '" + uri + "' as '" + cachedAt +"'");
        return List.of(new PathContent(cachedAt));
    }

    private boolean freshOrRefresh(Path cachedAt) {
        try {
            FileTime lastModified = Files.getLastModifiedTime(cachedAt);
            if (lastModified.toMillis() +(expirySeconds*1000)> System.currentTimeMillis()) {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        return null;
    }
}
