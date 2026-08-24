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

import io.nosqlbench.vectordata.CacheStats;
import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VvecReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Reader for variable vectors, indexed by byte offsets in an IDXFOR sidecar. */
public final class VariableVectorReader<A> implements VvecReader<A> {
    private final ByteStorage data; private final ByteStorage index; private final ElementType type; private final int width; private final long count;
    public VariableVectorReader(ByteStorage data, ByteStorage index, ElementType type, boolean index64) {
        this.data = data; this.index = index; this.type = type; this.width = index64 ? 8 : 4;
        if (index.size() % width != 0 || index.size() < width * 2L) throw new VectorDataException("Invalid IDXFOR sidecar length");
        count = index.size() / width - 1;
        long end = offset(count); if (end != data.size()) throw new VectorDataException("IDXFOR final offset does not equal vvec data size");
    }
    @Override public long count() { return count; }
    @Override public int dimensionAt(long value) { return record(value).order(ByteOrder.LITTLE_ENDIAN).getInt(); }
    @Override @SuppressWarnings("unchecked") public A get(long value) {
        ByteBuffer record = record(value).order(ByteOrder.LITTLE_ENDIAN); int dimension = record.getInt();
        if (dimension < 0 || record.remaining() != Math.multiplyExact(dimension, type.width())) throw new VectorDataException("Malformed vvec record " + value);
        return (A) ElementCodec.decode(record.slice(), type, dimension);
    }
    private ByteBuffer record(long value) {
        if (value < 0 || value >= count) throw new IndexOutOfBoundsException("vvec index " + value);
        long begin = offset(value), end = offset(value + 1);
        if (begin < 0 || end < begin || end > data.size() || end - begin > Integer.MAX_VALUE) throw new VectorDataException("Invalid IDXFOR range at " + value);
        return data.read(begin, Math.toIntExact(end - begin));
    }
    private long offset(long value) {
        ByteBuffer bytes = index.read(value * width, width).order(ByteOrder.LITTLE_ENDIAN);
        return width == 4 ? Integer.toUnsignedLong(bytes.getInt()) : bytes.getLong();
    }
    @Override public void prebuffer(PrebufferProgress progress) { data.prebuffer(progress); index.prebuffer(PrebufferProgress.NONE); }
    @Override public boolean isComplete() { return data.isComplete() && index.isComplete(); }
    @Override public CacheStats cacheStats() { return data.stats(); }
    @Override public ElementType elementType() { return type; }
}
