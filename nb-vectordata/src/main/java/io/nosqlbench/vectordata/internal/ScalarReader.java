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

/** Reader for packed scalar payloads. */
public final class ScalarReader implements VectorReader<Number> {
    private final ByteStorage storage; private final ElementType type; private final long count;
    public ScalarReader(ByteStorage storage, ElementType type) {
        this.storage = storage; this.type = type;
        if (storage.size() % type.width() != 0) throw new VectorDataException("Scalar payload is not aligned to " + type.width() + " bytes");
        count = storage.size() / type.width();
    }
    @Override public long count() { return count; }
    @Override public int dimension() { return 1; }
    @Override public Number get(long index) { if (index < 0 || index >= count) throw new IndexOutOfBoundsException(); return ElementCodec.number(storage.read(index * type.width(), type.width()), type); }
    @Override public void prebuffer(PrebufferProgress progress) { storage.prebuffer(progress); }
    @Override public boolean isComplete() { return storage.isComplete(); }
    @Override public CacheStats cacheStats() { return storage.stats(); }
    @Override public ElementType elementType() { return type; }
}
