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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ResolverForNBIOCache implements ContentResolver {
    public static final ResolverForNBIOCache INSTANCE = new ResolverForNBIOCache();
    private final static Logger logger = LogManager.getLogger(ResolverForNBIOCache.class);
    private static String cacheDir = System.getProperty("user.home") + "/.nosqlbench/nbio-cache/";
    private static boolean forceUpdate = false;
    private static boolean verifyChecksum = true;
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

    /**
     * This method is used to resolve the path of a given URI.
     * It first checks if the URI has a scheme (http or https) and if it does, it tries to resolve the path from the cache.
     * If the file is not in the cache, it tries to download it from the remote URL.
     * If the URI does not have a scheme, it returns null.
     *
     * @param uri the URI to resolve the path for
     * @return the resolved Path object, or null if the URI does not have a scheme or the path could not be resolved
     */
    private Path resolvePath(URI uri) {
        if (uri.getScheme() != null && !uri.getScheme().isEmpty() &&
            (uri.getScheme().equalsIgnoreCase("http") ||
                uri.getScheme().equalsIgnoreCase("https"))) {
            Path cachePath = Path.of(cacheDir + uri.getPath());
            if (Files.isReadable(cachePath)) {
                return pathFromLocalCache(cachePath, uri);
            }
            else {
                return pathFromRemoteUrl(uri);
            }
        }
        return null;
    }

    private boolean downloadFile(URI uri, Path cachePath, URLContent checksum) {
        int retries = 0;
        boolean success = false;
        while (retries < maxRetries) {
            try {
                URLContent urlContent = resolveURI(uri);
                if (urlContent != null) {
                    logger.info(() -> "Downloading remote file " + uri + " to cache at " + cachePath);
                    Files.copy(urlContent.getInputStream(), cachePath);
                    logger.info(() -> "Downloaded remote file to cache at " + cachePath);
                    if(checksum != null && verifyChecksum(cachePath, checksum)) {
                        success = true;
                        break;
                    }
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

    private boolean verifyChecksum(Path cachePath, URLContent checksum) {
        try {
            String localChecksumStr = generateSHA256Checksum(cachePath.toString());
            Path checksumPath = Path.of(cachePath.toString().substring(0, cachePath.toString().lastIndexOf('.')) + ".sha256");
            Files.writeString(checksumPath, localChecksumStr);
            logger.debug(() -> "Generated local checksum and saved to cache at " + checksumPath);
            String remoteChecksum = new String(checksum.getInputStream().readAllBytes());
            if (localChecksumStr.equals(remoteChecksum)) {
                return true;
            } else {
                logger.warn(() -> "checksums do not match for " + checksumPath + " and " + checksum);
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is used to download a file from a remote URL and store it in a local cache.
     * It first creates the cache directory if it doesn't exist.
     * Then it tries to download the file and if successful, it generates a SHA256 checksum for the downloaded file.
     * It then compares the generated checksum with the remote checksum.
     * If the checksums match, it returns the path to the cached file.
     * If the checksums don't match or if there was an error during the download, it cleans up the cache and throws a RuntimeException.
     *
     * @param uri the URI of the remote file to download
     * @return the Path to the downloaded file in the local cache
     * @throws RuntimeException if there was an error during the download or if the checksums don't match
     */
    private Path pathFromRemoteUrl(URI uri) {
        Path cachePath = Path.of(cacheDir + uri.getPath());
        createCacheDir(cachePath);
        if (!verifyChecksum) {
            return execute(NBIOResolverConditions.UPDATE_NO_VERIFY, cachePath, uri);
        }
        else {
            return execute(NBIOResolverConditions.UPDATE_AND_VERIFY, cachePath, uri);
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

    private void cleanupCache(Path cachePath) {
        if (!cachePath.toFile().delete())
            logger.warn(() -> "Could not delete cached file " + cachePath);
        Path checksumPath = Path.of(cachePath.toString().substring(0, cachePath.toString().lastIndexOf('.')) + ".sha256");
        if (!checksumPath.toFile().delete())
            logger.warn(() -> "Could not delete cached checksum " + checksumPath);
    }

    private Path execute(NBIOResolverConditions condition, Path cachePath, URI uri) {
        String remoteChecksumFileStr = uri.getPath().substring(0, uri.getPath().indexOf('.')) + ".sha256";
        URLContent checksum = resolveURI(URI.create(uri.toString().replace(uri.getPath(), remoteChecksumFileStr)));
        switch(condition) {
            case UPDATE_AND_VERIFY:
                if (checksum == null) {
                    logger.warn(() -> "Remote checksum file " + remoteChecksumFileStr + " does not exist. Proceeding without verification");
                }
                if (downloadFile(uri, cachePath, checksum)) {
                    return cachePath;
                } else {
                    throw new RuntimeException("Error downloading remote file to cache at " + cachePath);
                }
            case UPDATE_NO_VERIFY:
                logger.warn(() -> "Checksum verification is disabled, downloading remote file to cache at " + cachePath);
                if (downloadFile(uri, cachePath, null)) {
                    return cachePath;
                } else {
                    throw new RuntimeException("Error downloading remote file to cache at " + cachePath);
                }
            case LOCAL_VERIFY:
                if (checksum == null) {
                    logger.warn(() -> "Remote checksum file does not exist, returning cached file " + cachePath);
                    return cachePath;
                }
                try {
                    String localChecksum = Files.readString(getOrCreateChecksum(cachePath));
                    String remoteChecksum = new String(checksum.getInputStream().readAllBytes());
                    if (localChecksum.equals(remoteChecksum)) {
                        return cachePath;
                    }
                    else {
                        logger.warn(() -> "Checksums do not match, rehydrating cache " + cachePath);
                        return pathFromRemoteUrl(uri);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case LOCAL_NO_VERIFY:
                return cachePath;
            default:
                throw new RuntimeException("Invalid NBIO Cache condition");
        }
    }

    /**
     * This method is used to retrieve a file from the local cache.
     * It first checks if the file exists in the cache and if a checksum file is present.
     * If the checksum file is not present, it generates a new one.
     * If the "force update" option is enabled, it deletes the cached file and downloads it from the remote URL.
     * If the "checksum verification" option is enabled, it compares the local checksum with the remote checksum.
     * If the checksums match, it returns the path to the cached file.
     * If the checksums don't match, it deletes the cached file and downloads it from the remote URL.
     * If the remote file or checksum does not exist, it returns the cached file.
     *
     * @param cachePath the Path to the cached file
     * @param uri the URI of the remote file
     * @return the Path to the cached file
     * @throws RuntimeException if there was an error during the checksum comparison or if the checksums don't match
     */
    private Path pathFromLocalCache(Path cachePath, URI uri) {

        if (forceUpdate) {
            return pathFromRemoteUrl(uri);
        }
        if (!verifyChecksum) {
            logger.warn(() -> "Checksum verification is disabled, returning cached file " + cachePath);
            return execute(NBIOResolverConditions.LOCAL_NO_VERIFY, cachePath, uri);
        } else {
            return execute(NBIOResolverConditions.LOCAL_VERIFY, cachePath, uri);
        }

    }

    private Path getOrCreateChecksum(Path cachePath) {
        Path checksumPath = Path.of(cachePath.toString().substring(0, cachePath.toString().lastIndexOf('.')) + ".sha256");
        if (!Files.isReadable(checksumPath)) {
            try {
                Files.writeString(checksumPath, generateSHA256Checksum(cachePath.toString()));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return checksumPath;
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

        Path path = Path.of(cacheDir + uri.getPath());
        if (Files.isDirectory(path)) {
            dirs.add(path);
        }
        return dirs;
    }

    public static void setCacheDir(String cacheDir) {
        ResolverForNBIOCache.cacheDir = cacheDir;
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
