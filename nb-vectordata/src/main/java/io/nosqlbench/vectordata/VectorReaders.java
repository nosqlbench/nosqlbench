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

import io.nosqlbench.vectordata.internal.ByteStorage;
import io.nosqlbench.vectordata.internal.FixedVectorReader;
import io.nosqlbench.vectordata.internal.ScalarReader;
import io.nosqlbench.vectordata.internal.StorageFactory;
import io.nosqlbench.vectordata.internal.VariableVectorReader;

import java.net.URI;
import java.nio.file.Path;
import java.util.Locale;

/** Factories for source readers. */
public final class VectorReaders {
    private VectorReaders() { }
    public static VectorReader<?> open(String source) { return open(toUri(source), VectorDataSettings.defaults(), "direct"); }
    public static VectorReader<?> open(URI source, VectorDataSettings settings, String identity) {
        ElementType type = ElementType.forExtension(source.toString());
        ByteStorage storage = new StorageFactory(settings).open(source, identity);
        return isScalar(source.toString()) ? new ScalarReader(storage, type) : new FixedVectorReader<>(storage, type);
    }
    public static VvecReader<?> openVvec(URI source, VectorDataSettings settings, String identity) {
        String text = source.toString().toLowerCase(Locale.ROOT);
        ElementType type = ElementType.forExtension(text);
        if (!isVector(text)) throw new VectorDataException("vvec source must use a vector extension: " + source);
        ByteStorage data = new StorageFactory(settings).open(source, identity);
        URI i32 = URI.create(source.toString().replaceAll("[^/]+$", "IDXFOR__" + filename(source) + ".i32"));
        try { return new VariableVectorReader<>(data, new StorageFactory(settings).open(i32, identity), type, false); }
        catch (VectorDataException missingI32) {
            URI i64 = URI.create(source.toString().replaceAll("[^/]+$", "IDXFOR__" + filename(source) + ".i64"));
            return new VariableVectorReader<>(data, new StorageFactory(settings).open(i64, identity), type, true);
        }
    }
    @SuppressWarnings("unchecked") public static VectorReader<float[]> f32(URI source, VectorDataSettings settings, String identity) {
        VectorReader<?> reader = open(source, settings, identity); if (reader.elementType() != ElementType.F32 || !(reader instanceof FixedVectorReader<?>)) throw new VectorDataException("Expected f32 fixed vectors: " + source); return (VectorReader<float[]>) reader;
    }
    @SuppressWarnings("unchecked") public static VectorReader<int[]> i32(URI source, VectorDataSettings settings, String identity) {
        VectorReader<?> reader = open(source, settings, identity); if (reader.elementType() != ElementType.I32 || !(reader instanceof FixedVectorReader<?>)) throw new VectorDataException("Expected i32 fixed vectors: " + source); return (VectorReader<int[]>) reader;
    }
    static boolean isScalar(String source) { String ext = extension(source); return ext.matches("u8|i8|u16|i16|u32|i32|u64|i64"); }
    static boolean isVector(String source) { return !isScalar(source); }
    private static String extension(String value) { int dot = value.lastIndexOf('.'); int end = value.indexOf('?', dot); return value.substring(dot + 1, end < 0 ? value.length() : end).toLowerCase(Locale.ROOT); }
    private static URI toUri(String source) { return source.contains("://") || source.startsWith("file:") ? URI.create(source) : Path.of(source).toAbsolutePath().toUri(); }
    private static String filename(URI source) { String path = source.getPath(); return path.substring(path.lastIndexOf('/') + 1); }
}
