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
import io.nosqlbench.vectordata.VectorReader;

/** Reader for dimension-prefixed, fixed-width xvec records. */
public final class FixedVectorReader<A> implements VectorReader<A> {
    private final ByteStorage storage;
    private final ElementType type;
    private final int dimension;
    private final int recordBytes;
    private final long count;
    public FixedVectorReader(ByteStorage storage, ElementType type) {
        this.storage = storage; this.type = type;
        if (storage.size() < 4) throw new VectorDataException("xvec payload is too short for a dimension prefix");
        dimension = storage.read(0, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        if (dimension < 0) throw new VectorDataException("Negative xvec dimension: " + dimension);
        try { recordBytes = Math.addExact(4, Math.multiplyExact(dimension, type.width())); }
        catch (ArithmeticException e) { throw new VectorDataException("xvec record is too large", e); }
        if (recordBytes == 0 || storage.size() % recordBytes != 0) throw new VectorDataException("xvec file length is not a whole number of records");
        count = storage.size() / recordBytes;
    }
    @Override public long count() { return count; }
    @Override public int dimension() { return dimension; }
    @SuppressWarnings("unchecked") @Override public A get(long index) {
        check(index);
        long offset = index * (long) recordBytes;
        int current = storage.read(offset, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        if (current != dimension) throw new VectorDataException("Inconsistent xvec dimension at record " + index + ": " + current + " != " + dimension);
        return (A) ElementCodec.decode(storage.read(offset + 4, recordBytes - 4), type, dimension);
    }
    @Override public void get(long index, A target) { Object value = get(index); if (ElementCodec.arrayLength(target) != dimension) throw new VectorDataException("Target dimension differs from " + dimension); ElementCodec.copy(value, target); }
    private void check(long index) { if (index < 0 || index >= count) throw new IndexOutOfBoundsException("Vector index " + index + " outside 0.." + (count - 1)); }
    @Override public void prebuffer(PrebufferProgress progress) { storage.prebuffer(progress); }
    @Override public boolean isComplete() { return storage.isComplete(); }
    @Override public CacheStats cacheStats() { return storage.stats(); }
    @Override public ElementType elementType() { return type; }
}
