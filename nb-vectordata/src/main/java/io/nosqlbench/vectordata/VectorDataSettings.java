/*
 * Copyright (c) 2026 The NoSQLBench Authors.
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
package io.nosqlbench.vectordata;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Client configuration. Builder values override environment and settings files. */
public final class VectorDataSettings {
    private static final int DEFAULT_CHUNK_SIZE = 1 << 20;
    private final Path cacheDirectory;
    private final String bearerToken;
    private final Duration timeout;
    private final int chunkSize;

    private VectorDataSettings(Builder builder) {
        // An explicit per-client cache is an isolation boundary. Do not let a
        // builder used by a test or embedding application auto-write settings
        // merely to obtain a cache value that will be discarded.
        SettingsFile file = SettingsFile.load(builder.cacheDirectory == null);
        this.cacheDirectory = builder.cacheDirectory != null ? builder.cacheDirectory : file.cacheDirectory();
        this.bearerToken = builder.bearerToken != null ? builder.bearerToken : file.token();
        this.timeout = builder.timeout != null ? builder.timeout : file.timeout();
        this.chunkSize = builder.chunkSize != null ? builder.chunkSize : file.chunkSize();
        if (chunkSize <= 0) throw new VectorDataException("chunkSize must be positive");
    }

    public Path cacheDirectory() { return cacheDirectory; }
    public Optional<String> bearerToken() { return Optional.ofNullable(bearerToken).filter(s -> !s.isBlank()); }
    public Duration timeout() { return timeout; }
    public int chunkSize() { return chunkSize; }
    public static Builder builder() { return new Builder(); }
    public static VectorDataSettings defaults() { return builder().build(); }

    public static final class Builder {
        private Path cacheDirectory;
        private String bearerToken;
        private Duration timeout;
        private Integer chunkSize;
        public Builder cacheDirectory(Path value) { this.cacheDirectory = value; return this; }
        public Builder bearerToken(String value) { this.bearerToken = value; return this; }
        public Builder timeout(Duration value) { this.timeout = value; return this; }
        public Builder chunkSize(int value) { this.chunkSize = value; return this; }
        public VectorDataSettings build() { return new VectorDataSettings(this); }
    }

    /** Test hook: resolve an isolated settings root without reading process configuration. */
    static Path resolveCacheDirectory(Path settings, Path userHome, Path vectordataHome, Path xdgCacheHome, boolean bootstrap) {
        return SettingsFile.load(settings, userHome, vectordataHome, xdgCacheHome, bootstrap).cacheDirectory();
    }

    /** Test hook for the persistence half of the Rust-compatible bootstrap protocol. */
    static void writeCacheDirectory(Path settings, Path cacheDirectory, boolean force) {
        SettingsFile.writeCacheDirectory(settings, cacheDirectory, force);
    }

    private enum AutoCacheReason { HOME_IS_LARGEST_MOUNT, DIFFERENT_MOUNT_IS_LARGEST, MOUNTS_UNAVAILABLE }
    private record AutoCache(Path path, AutoCacheReason reason) { }

    private record SettingsFile(Path cacheDirectory, String token, Duration timeout, int chunkSize) {
        private static SettingsFile load(boolean bootstrap) {
            Path vectordataHome = Optional.ofNullable(System.getenv("VECTORDATA_HOME"))
                .filter(s -> !s.isBlank()).map(Path::of).orElse(null);
            Path userHome = Optional.ofNullable(System.getenv("HOME")).filter(s -> !s.isBlank())
                .map(Path::of).orElseGet(() -> Path.of(System.getProperty("user.home")));
            Path settings = vectordataHome != null ? vectordataHome.resolve("settings.yaml") : userHome.resolve(".config/vectordata/settings.yaml");
            return load(settings, userHome, vectordataHome, xdgCacheHome(userHome), bootstrap);
        }

        private static SettingsFile load(Path settings, Path userHome, Path vectordataHome, Path xdgCacheHome, boolean bootstrap) {
            String token = System.getenv("VECTORDATA_TOKEN");
            Duration timeout = Duration.ofSeconds(30);
            int chunkSize = DEFAULT_CHUNK_SIZE;
            Path configuredCache = null;
            boolean settingsExists = Files.isRegularFile(settings);
            if (settingsExists) {
                try {
                    Object loaded = new Load(LoadSettings.builder().setLabel(settings.toString()).build())
                        .loadFromString(Files.readString(settings));
                    if (loaded instanceof Map<?, ?> map) {
                        configuredCache = configuredPath(map.get("cache_dir"));
                        token = text(map.get("token"), token);
                        timeout = duration(map.get("timeout_seconds"), timeout);
                        chunkSize = integer(map.get("chunk_size"), chunkSize);
                    }
                } catch (IOException | RuntimeException e) {
                    throw new VectorDataException("Cannot read vectordata settings " + settings, e);
                }
            }
            if (configuredCache != null) return new SettingsFile(configuredCache, token, timeout, chunkSize);

            // This is a complete configuration/cache isolation boundary, matching
            // vectordata-rs: it does not create settings.yaml under this root.
            if (vectordataHome != null) return new SettingsFile(vectordataHome.resolve("cache"), token, timeout, chunkSize);
            if (!bootstrap) return new SettingsFile(xdgCacheHome.resolve("vectordata"), token, timeout, chunkSize);

            AutoCache auto = autoCache(userHome, xdgCacheHome);
            if (auto.reason() == AutoCacheReason.DIFFERENT_MOUNT_IS_LARGEST) {
                throw new VectorDataException("No cache_dir is configured in " + settings
                    + "; the largest writable mount is outside the home filesystem. Configure cache_dir explicitly.");
            }
            try {
                writeCacheDirectory(settings, auto.path(), false);
                System.getLogger(VectorDataSettings.class.getName()).log(System.Logger.Level.WARNING,
                    "No vectordata cache_dir was configured; auto-set it to {0}", auto.path());
            } catch (VectorDataException e) {
                throw new VectorDataException("No cache_dir is configured in " + settings + "; configure cache_dir explicitly.", e);
            }
            return new SettingsFile(auto.path(), token, timeout, chunkSize);
        }

        private static Path configuredPath(Object value) {
            String text = text(value, null);
            return text == null || text.isBlank() ? null : Path.of(text);
        }

        private static Path xdgCacheHome(Path userHome) {
            String configured = System.getenv("XDG_CACHE_HOME");
            if (configured != null && !configured.isBlank()) {
                Path candidate = Path.of(configured);
                if (candidate.isAbsolute()) return candidate;
            }
            return userHome.resolve(".cache");
        }

        /** Mirrors Rust: never silently place a large cache on a different, larger mount. */
        private static AutoCache autoCache(Path userHome, Path xdgCacheHome) {
            try {
                FileStore homeStore = Files.getFileStore(userHome);
                List<Path> mounts = writableMounts();
                if (mounts.isEmpty()) return new AutoCache(xdgCacheHome.resolve("vectordata"), AutoCacheReason.MOUNTS_UNAVAILABLE);
                mounts.sort(Comparator.comparingLong(SettingsFile::usableSpace).reversed());
                for (Path mount : mounts) {
                    FileStore store;
                    try { store = Files.getFileStore(mount); } catch (IOException ignored) { continue; }
                    if (homeStore.equals(store)) return new AutoCache(xdgCacheHome.resolve("vectordata"), AutoCacheReason.HOME_IS_LARGEST_MOUNT);
                    if (Files.isWritable(mount)) return new AutoCache(mount.resolve("vectordata-cache"), AutoCacheReason.DIFFERENT_MOUNT_IS_LARGEST);
                }
            } catch (IOException ignored) {
                // An unusable mount table must not block the XDG fallback.
            }
            return new AutoCache(xdgCacheHome.resolve("vectordata"), AutoCacheReason.MOUNTS_UNAVAILABLE);
        }

        private static List<Path> writableMounts() {
            Path mounts = Path.of("/proc/mounts");
            if (!Files.isRegularFile(mounts)) return List.of();
            try {
                List<Path> result = new ArrayList<>();
                for (String line : Files.readAllLines(mounts)) {
                    String[] fields = line.split("\\s+");
                    if (fields.length >= 4 && fields[3].contains("rw")) {
                        Path mount = Path.of(unescapeMount(fields[1]));
                        if (Files.isDirectory(mount) && Files.isWritable(mount)) result.add(mount);
                    }
                }
                return result;
            } catch (IOException ignored) { return List.of(); }
        }

        private static long usableSpace(Path path) {
            try { return Files.getFileStore(path).getUsableSpace(); }
            catch (IOException ignored) { return -1L; }
        }

        private static String unescapeMount(String value) {
            return value.replace("\\040", " ").replace("\\011", "\t").replace("\\012", "\n").replace("\\134", "\\");
        }

        private static void writeCacheDirectory(Path settings, Path cacheDirectory, boolean force) {
            try {
                if (Files.isRegularFile(settings)) {
                    Path existing = existingCacheDirectory(settings);
                    if (cacheDirectory.equals(existing)) return;
                    if (!force) throw new VectorDataException("Refusing to overwrite existing vectordata settings " + settings);
                }
                if (settings.getParent() != null) Files.createDirectories(settings.getParent());
                Files.createDirectories(cacheDirectory);
                String content = Files.isRegularFile(settings)
                    ? rewriteCacheDirectory(Files.readString(settings), cacheDirectory)
                    : "cache_dir: " + cacheDirectory + "\nprotect_settings: true\n";
                Files.writeString(settings, content);
            } catch (IOException e) {
                throw new VectorDataException("Cannot write vectordata settings " + settings, e);
            }
        }

        private static Path existingCacheDirectory(Path settings) {
            try {
                Object loaded = new Load(LoadSettings.builder().setLabel(settings.toString()).build()).loadFromString(Files.readString(settings));
                return loaded instanceof Map<?, ?> map ? configuredPath(map.get("cache_dir")) : null;
            } catch (IOException | RuntimeException e) { throw new VectorDataException("Cannot read vectordata settings " + settings, e); }
        }

        private static String rewriteCacheDirectory(String existing, Path value) {
            StringBuilder result = new StringBuilder(); boolean replaced = false;
            for (String line : existing.split("(?<=\\n)", -1)) {
                String trimmed = line.stripLeading();
                if (!replaced && !trimmed.startsWith("#") && trimmed.startsWith("cache_dir:")) {
                    result.append("cache_dir: ").append(value).append('\n'); replaced = true;
                } else result.append(line);
            }
            if (!replaced) {
                if (!existing.isEmpty() && !existing.endsWith("\n")) result.append('\n');
                result.append("cache_dir: ").append(value).append('\n');
            }
            return result.toString();
        }

        private static String text(Object value, String fallback) { return value == null ? fallback : String.valueOf(value); }
        private static int integer(Object value, int fallback) {
            if (value instanceof Number n) return n.intValue();
            try { return value == null ? fallback : Integer.parseInt(String.valueOf(value)); }
            catch (NumberFormatException ignored) { return fallback; }
        }
        private static Duration duration(Object value, Duration fallback) {
            return Duration.ofSeconds(integer(value, Math.toIntExact(fallback.toSeconds())));
        }
    }
}
