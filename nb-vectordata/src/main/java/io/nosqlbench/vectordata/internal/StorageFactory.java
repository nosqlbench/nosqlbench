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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/** Shares byte storage by canonical source URI within a JVM. */
public final class StorageFactory {
    private static final ConcurrentHashMap<String, WeakReference<ByteStorage>> OPEN = new ConcurrentHashMap<>();
    private final VectorDataSettings settings;
    public StorageFactory(VectorDataSettings settings) { this.settings = settings; }

    public ByteStorage open(URI source, String datasetIdentity) {
        String key = source.normalize().toString();
        synchronized (OPEN) {
            ByteStorage existing = OPEN.containsKey(key) ? OPEN.get(key).get() : null;
            if (existing != null) return existing;
            ByteStorage created;
            if ("file".equalsIgnoreCase(source.getScheme()) || source.getScheme() == null) {
                created = new MappedStorage(Path.of(source));
            } else if ("http".equalsIgnoreCase(source.getScheme()) || "https".equalsIgnoreCase(source.getScheme())) {
                Path cache = settings.cacheDirectory().resolve(safe(datasetIdentity)).resolve(digest(key));
                created = new RemoteStorage(source, settings, cache);
            } else throw new VectorDataException("Unsupported source URI scheme: " + source);
            OPEN.put(key, new WeakReference<>(created));
            return created;
        }
    }
    private static String safe(String identity) { return identity.replaceAll("[^A-Za-z0-9._-]", "_"); }
    private static String digest(String value) {
        try {
            byte[] hashed = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(); for (byte b : hashed) result.append(String.format("%02x", b));
            return result.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
