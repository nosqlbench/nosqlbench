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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ResolverForNBIOCache implements ContentResolver {
    public static final ResolverForNBIOCache INSTANCE = new ResolverForNBIOCache();
    private final static Logger logger = LogManager.getLogger(ResolverForNBIOCache.class);
    private static final String userHomeDirectory = System.getProperty("user.home");
    //TODO: This needs to be set somehow - envvar, yaml setting, etc.
    private static String cache = userHomeDirectory + "/.nosqlbench/nbio-cache/";
    //TODO: This needs to be set through configuration at runtime
    private static boolean forceUpdate = false;
    //TODO: This needs to be set through configuration at runtime
    private static boolean verifyChecksum = true;
    //TODO: This needs to be set through configuration at runtime
    private static int maxRetries = 3;
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
         *
         * TODO: Need to handle situation where file is in the cache, we want to force update but the update fails.
         *       In this case we don't want to delete the local file because we need to return it.
         * Suggestion: add enum type defining behavior (force update, for update IF condition x, do not update, etc.)
         * See NBIOResolverConditions
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

    private boolean downloadFile(URI uri, Path cachePath) {
        int retries = 0;
        boolean success = false;
        while (retries < maxRetries) {
            try {
                URLContent urlContent = resolveURI(uri);
                if (urlContent != null) {
                    logger.info(() -> "Downloading remote file " + uri + " to cache at " + cachePath);
                    Files.copy(urlContent.getInputStream(), cachePath);
                    logger.info(() -> "Downloaded remote file to cache at " + cachePath);
                    success = true;
                    break;
                } else {
                    logger.error(() -> "Error downloading remote file to cache at " + cachePath + ", retrying...");
                    retries++;
                }
            } catch (IOException e) {
                logger.error(() -> "Error downloading remote file to cache at " + cachePath + ", retrying...");
                retries++;
            }
        }
        return success;
    }

    private Path pathFromRemoteUrl(URI uri) {
        /*
         * File is not in cache - next steps:
         * 1. Download the file and put it in the cache
         *   1a. If the remote file does not exist log error and return null
         *   1b. If the download fails jump to step 6
         * 2. Download the checksum and store in memory
         * 3. Generate a new checksum for the file and put it in the cache
         * 4. compare the checksums
         * 5. If they match, return the path to the file in the cache
         * 6. If they don't match/exception downloading repeat steps 1-5 up to a configurable number of times
         *   6a. If the max attempts have been exceeded throw an exception and clean up the cache
         */
        Path cachePath = Path.of(cache + uri.getPath());
        createCacheDir(cachePath);
        if (downloadFile(uri, cachePath)) {
            String remoteChecksumFileStr = uri.getPath().substring(0, uri.getPath().indexOf('.')) + ".sha256";
            try {
                String localChecksumStr = generateSHA256Checksum(cachePath.toString());
                URLContent checksum = resolveURI(URI.create(uri.toString().replace(uri.getPath(), remoteChecksumFileStr)));
                if (checksum == null) {
                    logger.warn(() -> "Remote checksum file " + remoteChecksumFileStr + " does not exist");
                    return cachePath;
                } else {
                    Path checksumPath = Path.of(cachePath.toString().substring(0, cachePath.toString().lastIndexOf('.')) + ".sha256");
                    Files.writeString(checksumPath, localChecksumStr);
                    logger.debug(() -> "Generated local checksum and saved to cache at " + checksumPath);
                    String remoteChecksum = new String(checksum.getInputStream().readAllBytes());
                    if (localChecksumStr.equals(remoteChecksum)) {
                        return cachePath;
                    } else {
                        if (!cachePath.toFile().delete())
                            logger.warn(() -> "Could not delete cached file " + cachePath);
                        if (!checksumPath.toFile().delete())
                            logger.warn(() -> "Could not delete cached checksum " + checksumPath);
                        throw new RuntimeException("Checksums do not match");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            cleanupCache();
            throw new RuntimeException("Error downloading remote file to cache at " + cachePath);
        }
    }

    private void createCacheDir(Path cachePath) {
        Path dir = cachePath.getParent();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void cleanupCache() {
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
        Path checksumPath = Path.of(cachePath.toString().substring(0, cachePath.toString().lastIndexOf('.')) + ".sha256");
        if (!Files.isReadable(checksumPath)) {
            try {
                Files.writeString(checksumPath, generateSHA256Checksum(cachePath.toString()));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        if (forceUpdate) {
            if (!cachePath.toFile().delete())
                logger.warn(() -> "Could not delete cached file " + cachePath);
            if (!checksumPath.toFile().delete())
                logger.warn(() -> "Could not delete cached checksum " + checksumPath);
            return pathFromRemoteUrl(uri);
        }
        if (!verifyChecksum) {
            logger.warn(() -> "Checksum verification is disabled, returning cached file " + cachePath);
            return cachePath;
        }
        URLContent content = resolveURI(uri);
        if (content == null) {
            logger.warn(() -> "Remote file does not exist, returning cached file " + cachePath);
            return cachePath;
        }
        String remoteChecksumFileStr = uri.getPath().substring(0, uri.getPath().indexOf('.')) + ".sha256";
        URLContent checksum = resolveURI(URI.create(uri.toString().replace(uri.getPath(), remoteChecksumFileStr)));
        if (checksum == null) {
            logger.warn(() -> "Remote checksum file does not exist, returning cached file " + cachePath);
            return cachePath;
        }
        try {
            String localChecksum = Files.readString(checksumPath);
            String remoteChecksum = new String(checksum.getInputStream().readAllBytes());
            if (localChecksum.equals(remoteChecksum)) {
                return cachePath;
            }
            else {
                if (!cachePath.toFile().delete())
                    logger.warn(() -> "Could not delete cached file " + cachePath);
                return pathFromRemoteUrl(uri);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateSHA256Checksum(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private URLContent resolveURI(URI uri) {
        try {
            URL url = uri.toURL();
            InputStream inputStream = url.openStream();
            logger.debug(() -> "Found accessible remote file at " + url);
            return new URLContent(url, inputStream);
        } catch (IOException e) {
            logger.error(() -> "Unable to find content at URI '" + uri + "', this often indicates a configuration error.");
            return null;
        }
    }

    @Override
    public List<Path> resolveDirectory(URI uri) {
        List<Path> dirs = new ArrayList<>();

        Path path = Path.of(cache + uri.getPath());
        if (Files.isDirectory(path)) {
            dirs.add(path);
        }
        return dirs;
    }

    public static void setCache(String cache) {
        ResolverForNBIOCache.cache = cache;
    }

    public static void setForceUpdate(boolean forceUpdate) {
        ResolverForNBIOCache.forceUpdate = forceUpdate;
    }

    public static void setVerifyChecksum(boolean verifyChecksum) {
        ResolverForNBIOCache.verifyChecksum = verifyChecksum;
    }

    public static void setMaxRetries(int maxRetries) {
        ResolverForNBIOCache.maxRetries = maxRetries;
    }

}
