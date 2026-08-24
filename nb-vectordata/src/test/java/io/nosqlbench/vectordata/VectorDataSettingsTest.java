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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Settings tests use explicit temporary roots and never inspect or write user configuration. */
class VectorDataSettingsTest {
    @TempDir Path temporary;

    @Test void configuredCacheDirectoryWinsWithoutChangingSettings() throws Exception {
        Path settings = temporary.resolve("config/settings.yaml");
        Path configured = temporary.resolve("configured-cache");
        Files.createDirectories(settings.getParent());
        Files.writeString(settings, "# retain this comment\ncache_dir: " + configured + "\nchunk_size: 8192\n");

        assertEquals(configured.toAbsolutePath(), VectorDataSettings.resolveCacheDirectory(
            settings, temporary.resolve("home"), null, temporary.resolve("xdg"), true));
        assertEquals("# retain this comment\ncache_dir: " + configured + "\nchunk_size: 8192\n", Files.readString(settings));
    }

    @Test void vectordataHomeProvidesIsolatedDefaultWithoutWritingSettings() {
        Path root = temporary.resolve("isolated-vectordata-home");
        Path settings = root.resolve("settings.yaml");

        assertEquals(root.resolve("cache"), VectorDataSettings.resolveCacheDirectory(
            settings, temporary.resolve("home"), root, temporary.resolve("xdg"), true));
        assertFalse(Files.exists(settings));
        assertFalse(Files.exists(root.resolve("cache")), "cache creation remains lazy until a remote reader opens");
    }

    @Test void autoBootstrapWriterCreatesDirectoryAndRustCompatibleSettingsFile() throws Exception {
        Path settings = temporary.resolve("config/settings.yaml");
        Path cache = temporary.resolve("cache/vectordata");

        VectorDataSettings.writeCacheDirectory(settings, cache, false);

        assertTrue(Files.isDirectory(cache));
        assertEquals("cache_dir: " + cache + "\nprotect_settings: true\n", Files.readString(settings));
        assertEquals(cache.toAbsolutePath(), VectorDataSettings.resolveCacheDirectory(
            settings, temporary.resolve("home"), null, temporary.resolve("xdg"), false));
    }

    @Test void absentConfigurationBootstrapsTheSafeXdgFallback() throws Exception {
        Path settings = temporary.resolve("config/settings.yaml");
        Path xdg = temporary.resolve("xdg");
        Path expected = xdg.resolve("vectordata");

        // A non-existent home makes mount discovery unavailable deterministically;
        // both clients then use and persist the XDG fallback.
        assertEquals(expected, VectorDataSettings.resolveCacheDirectory(
            settings, temporary.resolve("unavailable-home"), null, xdg, true));
        assertTrue(Files.isDirectory(expected));
        assertEquals("cache_dir: " + expected + "\nprotect_settings: true\n", Files.readString(settings));
    }

    @Test void automaticBootstrapRefusesToChangeAnExistingSettingsFileWithoutCacheDir() throws Exception {
        Path settings = temporary.resolve("config/settings.yaml");
        Files.createDirectories(settings.getParent());
        Files.writeString(settings, "token: preserved\n");

        assertThrows(VectorDataException.class, () -> VectorDataSettings.writeCacheDirectory(
            settings, temporary.resolve("cache"), false));
        assertEquals("token: preserved\n", Files.readString(settings));
    }

    @Test void explicitCallerCacheDoesNotRequireBootstrapPersistence() {
        Path settings = temporary.resolve("config/settings.yaml");
        Path fallback = temporary.resolve("xdg/vectordata");

        assertEquals(fallback, VectorDataSettings.resolveCacheDirectory(
            settings, temporary.resolve("home"), null, temporary.resolve("xdg"), false));
        assertFalse(Files.exists(settings));
    }
}
