/*
 * Copyright (c) nosqlbench
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
     * It first checks if the URI has a scheme (http or https) and if it does, it tries to resolve the path from the
     * cache.
     * If the file is not in the cache, it tries to download it from the remote URL.
     * If the URI does not have a scheme, it returns null.
     *
     * @param uri
     *     the URI to resolve the path for
     * @return the resolved Path object, or null if the URI does not have a scheme or the path could not be resolved
     */
    private Path resolvePath(URI uri) {
        if (uri.getScheme() != null && !uri.getScheme().isEmpty() &&
            (uri.getScheme().equalsIgnoreCase("http") ||
                uri.getScheme().equalsIgnoreCase("https"))) {
            Path cachedFilePath = Path.of(cacheDir + uri.getPath());
            if (Files.isReadable(cachedFilePath)) {
                return pathFromLocalCache(cachedFilePath, uri);
            } else {
                return pathFromRemoteUrl(uri);
            }
        }
        return null;
    }

    private static class ProgressPrinter extends TimerTask {
        private final long fileSize;
        private long totalBytesRead;

        public ProgressPrinter(long fileSize, long totalBytesRead) {
            this.fileSize = fileSize;
            this.totalBytesRead = totalBytesRead;
        }

        public void update(long totalBytesRead) {
            this.totalBytesRead = totalBytesRead;
        }

        @Override
        public void run() {
            double progress = (double) totalBytesRead / fileSize * 100;
            logger.info(() -> "Progress: " + String.format("%.2f", progress) + "% completed");
            if (totalBytesRead == fileSize) {
                cancel();
            }
        }
    }

    private boolean downloadFile(URI uri, Path cachedFilePath, URLContent checksum) {
        int retries = 0;
        boolean success = false;
        while (retries < maxRetries) {
            try {
                if (this.remoteFileExists(uri)) {
                    checkLocalDiskSpace(uri);
                    Timer timer = new Timer();
                    try {
                        logger.info(() -> "Downloading remote file " + uri + " to cache at " + cachedFilePath);
                        ReadableByteChannel channel = Channels.newChannel(uri.toURL().openStream());
                        FileOutputStream outputStream = new FileOutputStream(cachedFilePath.toFile());
                        long fileSize = uri.toURL().openConnection().getContentLengthLong();
                        long totalBytesRead = 0;
                        FileChannel fileChannel = outputStream.getChannel();
                        ByteBuffer buffer = ByteBuffer.allocate(32768);

                        ProgressPrinter printer = new ProgressPrinter(fileSize, 0);
                        timer.scheduleAtFixedRate(printer, 2000, 2000);
                        while (channel.read(buffer) != -1) {
                            buffer.flip();
                            totalBytesRead += fileChannel.write(buffer);
                            printer.update(totalBytesRead);
                            buffer.clear();
                        }
                        outputStream.close();
                        channel.close();
                        logger.info(() -> "Downloaded remote file to cache at " + cachedFilePath);
                        if (checksum == null || verifyChecksum(cachedFilePath, checksum)) {
                            success = true;
                            break;
                        }
                    } finally {
                        timer.cancel();
                    }
                } else {
                    logger.error(() -> "Error downloading remote file to cache at " + cachedFilePath + ", retrying...");
                    retries++;
                }
            } catch (IOException e) {
                logger.error(() -> "Error downloading remote file to cache at " + cachedFilePath + ":" + e + ", " +
                    "retrying...");
                retries++;
            } catch (NBIOCacheException e) {
                logger.error(() -> "Error downloading remote file to cache at " + cachedFilePath + ":" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return success;
    }

    private void checkLocalDiskSpace(URI uri) throws NBIOCacheException {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("HEAD");
            int length = connection.getContentLength();
            long freeSpace = Files.getFileStore(Path.of(cacheDir)).getUsableSpace();
            if (length > (freeSpace * 0.9)) {
                throw new NBIOCacheException("Not enough space to download file " + uri + " of size " + length +
                    " to cache at " + cacheDir + " with only " + freeSpace + " bytes free");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private boolean verifyChecksum(Path cachedFilePath, URLContent checksum) {
        try {
            String localChecksumStr = generateSHA256Checksum(cachedFilePath.toString());
            Path checksumPath = checksumPath(cachedFilePath);
            Files.writeString(checksumPath, localChecksumStr);
            logger.info(() -> "Generated local checksum and saved to cache at " + checksumPath);
            String remoteChecksum = stripControlCharacters(new String(checksum.getInputStream().readAllBytes()));
            if (localChecksumStr.equals(remoteChecksum)) {
                logger.info(() -> "Checksums match for " + checksumPath + " and " + checksum);
                return true;
            } else {
                logger.warn(() -> "checksums do not match for " + checksumPath + " and " + checksum);
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String stripControlCharacters(String input) {
        return input.replaceAll("[\\p{Cntrl}]+$", "");
    }

    /**
     * This method is used to download a file from a remote URL and store it in a local cache.
     * It first creates the cache directory if it doesn't exist.
     * Then it tries to download the file and if successful, it generates a SHA256 checksum for the downloaded file.
     * It then compares the generated checksum with the remote checksum.
     * If the checksums match, it returns the path to the cached file.
     * If the checksums don't match or if there was an error during the download, it cleans up the cache and throws a
     * RuntimeException.
     *
     * @param uri
     *     the URI of the remote file to download
     * @return the Path to the downloaded file in the local cache
     * @throws RuntimeException
     *     if there was an error during the download or if the checksums don't match
     */
    private Path pathFromRemoteUrl(URI uri) {
        Path cachedFilePath = Path.of(cacheDir + uri.getPath());
        createCacheDir(cachedFilePath);
        if (!verifyChecksum) {
            return execute(NBIOResolverConditions.UPDATE_NO_VERIFY, cachedFilePath, uri);
        } else {
            return execute(NBIOResolverConditions.UPDATE_AND_VERIFY, cachedFilePath, uri);
        }
    }

    private void createCacheDir(Path cachedFilePath) {
        Path dir = cachedFilePath.getParent();
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void cleanupCache(Path cachedFilePath) {
        logger.info(() -> "Cleaning up cache for " + cachedFilePath);
        if (!cachedFilePath.toFile().delete())
            logger.warn(() -> "Could not delete cached file " + cachedFilePath);
        Path checksumPath = checksumPath(cachedFilePath);
        if (!checksumPath.toFile().delete())
            logger.warn(() -> "Could not delete cached checksum " + checksumPath);
    }

    private Path execute(NBIOResolverConditions condition, Path cachedFilePath, URI uri) {
        String remoteChecksumFileStr = uri.getPath() + ".sha256";
        URLContent checksum = resolveURI(URI.create(uri.toString().replace(uri.getPath(), remoteChecksumFileStr)));
        switch (condition) {
            case UPDATE_AND_VERIFY:
                if (checksum == null) {
                    logger.warn(() -> "Remote checksum file " + remoteChecksumFileStr + " does not exist. Proceeding without verification");
                }
                if (downloadFile(uri, cachedFilePath, checksum)) {
                    return cachedFilePath;
                } else {
                    cleanupCache(cachedFilePath);
                    throw new RuntimeException("Error downloading remote file to cache at " + cachedFilePath);
                }
            case UPDATE_NO_VERIFY:
                logger.warn(() -> "Checksum verification is disabled, downloading remote file to cache at " + cachedFilePath);
                if (downloadFile(uri, cachedFilePath, null)) {
                    return cachedFilePath;
                } else {
                    cleanupCache(cachedFilePath);
                    throw new RuntimeException("Error downloading remote file to cache at " + cachedFilePath);
                }
            case LOCAL_VERIFY:
                if (checksum == null) {
                    logger.warn(() -> "Remote checksum file does not exist, returning cached file " + cachedFilePath);
                    return cachedFilePath;
                }
                try {
                    String localChecksum = Files.readString(getOrCreateChecksum(cachedFilePath));
                    String remoteChecksum = stripControlCharacters(new String(checksum.getInputStream().readAllBytes()));
                    if (localChecksum.equals(remoteChecksum)) {
                        logger.info(() -> "Checksums match, returning cached file " + cachedFilePath);
                        return cachedFilePath;
                    } else {
                        logger.warn(() -> "Checksums do not match, rehydrating cache " + cachedFilePath);
                        return pathFromRemoteUrl(uri);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            case LOCAL_NO_VERIFY:
                return cachedFilePath;
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
     * @param cachedFilePath
     *     the Path to the cached file
     * @param uri
     *     the URI of the remote file
     * @return the Path to the cached file
     * @throws RuntimeException
     *     if there was an error during the checksum comparison or if the checksums don't match
     */
    private Path pathFromLocalCache(Path cachedFilePath, URI uri) {

        if (forceUpdate) {
            return pathFromRemoteUrl(uri);
        }
        if (!verifyChecksum) {
            logger.warn(() -> "Checksum verification is disabled, returning cached file " + cachedFilePath);
            return execute(NBIOResolverConditions.LOCAL_NO_VERIFY, cachedFilePath, uri);
        } else {
            return execute(NBIOResolverConditions.LOCAL_VERIFY, cachedFilePath, uri);
        }

    }

    private Path getOrCreateChecksum(Path cachedFilePath) {
        Path checksumPath = checksumPath(cachedFilePath);
        if (!Files.isReadable(checksumPath)) {
            try {
                Files.writeString(checksumPath, generateSHA256Checksum(cachedFilePath.toString()));
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return checksumPath;
    }

    private Path checksumPath(Path cachedFilePath) {
        return Path.of(cachedFilePath + ".sha256");
    }

    private static String generateSHA256Checksum(String filePath) throws IOException, NoSuchAlgorithmException {
        logger.info(() -> "Generating sha256 checksum for " + filePath);
        long fileSize = Files.size(Path.of(filePath));
        long totalBytesRead = 0;
        Timer timer = new Timer();
        ProgressPrinter printer = new ProgressPrinter(fileSize, 0);
        timer.scheduleAtFixedRate(printer, 2000, 2000);
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream is = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                printer.update(totalBytesRead);
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

    private boolean remoteFileExists(URI uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false; // Error occurred or file does not exist
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
