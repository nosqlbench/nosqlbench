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
import io.nosqlbench.vectordata.internal.ShardedVectorReader;
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
        return over(new StorageFactory(settings).open(source, identity), source.toString());
    }
    /// A reader over storage already open, chosen by the source's
    /// extension — the per-file reader a facet spread across several
    /// files composes.
    public static VectorReader<?> over(ByteStorage storage, String source) {
        ElementType type = ElementType.forExtension(source);
        return isScalar(source) ? new ScalarReader(storage, type) : new FixedVectorReader<>(storage, type);
    }
    public static VvecReader<?> openVvec(URI source, VectorDataSettings settings, String identity) {
        String text = source.toString().toLowerCase(Locale.ROOT);
        ElementType type = ElementType.forExtension(text);
        if (!isVector(text)) throw new VectorDataException("vvec source must use a vector extension: " + source);
        ByteStorage data = new StorageFactory(settings).open(source, identity);
        // Probe the sidecar width the payload size calls for first —
        // the rule the writers use — so the common case costs one
        // request instead of a guaranteed miss.
        boolean expect64 = data.size() > Integer.MAX_VALUE;
        URI expected = URI.create(source.toString().replaceAll("[^/]+$", "IDXFOR__" + filename(source) + (expect64 ? ".i64" : ".i32")));
        try { return new VariableVectorReader<>(data, new StorageFactory(settings).open(expected, identity), type, expect64); }
        catch (VectorDataException missing) {
            URI other = URI.create(source.toString().replaceAll("[^/]+$", "IDXFOR__" + filename(source) + (expect64 ? ".i32" : ".i64")));
            return new VariableVectorReader<>(data, new StorageFactory(settings).open(other, identity), type, !expect64);
        }
    }
    public static VectorReader<float[]> f32(URI source, VectorDataSettings settings, String identity) {
        return expectFixed(open(source, settings, identity), ElementType.F32, source);
    }
    public static VectorReader<int[]> i32(URI source, VectorDataSettings settings, String identity) {
        return expectFixed(open(source, settings, identity), ElementType.I32, source);
    }
    /// Narrows a reader to a fixed-vector element type, single-file and
    /// sharded alike; a packed scalar or another element type is refused
    /// naming what was expected.
    @SuppressWarnings("unchecked")
    public static <T> VectorReader<T> expectFixed(VectorReader<?> reader, ElementType type, Object source) {
        boolean fixed = reader instanceof FixedVectorReader<?> || (reader instanceof ShardedVectorReader<?> sharded && !sharded.isScalar());
        if (reader.elementType() != type || !fixed)
            throw new VectorDataException("Expected " + type.name().toLowerCase(Locale.ROOT) + " fixed vectors: " + source);
        return (VectorReader<T>) reader;
    }
    public static boolean isScalar(String source) { String ext = extension(source); return ext.matches("u8|i8|u16|i16|u32|i32|u64|i64"); }
    static boolean isVector(String source) { return !isScalar(source); }
    private static String extension(String value) { int dot = value.lastIndexOf('.'); int end = value.indexOf('?', dot); return value.substring(dot + 1, end < 0 ? value.length() : end).toLowerCase(Locale.ROOT); }
    private static URI toUri(String source) { return source.contains("://") || source.startsWith("file:") ? URI.create(source) : Path.of(source).toAbsolutePath().toUri(); }
    private static String filename(URI source) { String path = source.getPath(); return path.substring(path.lastIndexOf('/') + 1); }
}
