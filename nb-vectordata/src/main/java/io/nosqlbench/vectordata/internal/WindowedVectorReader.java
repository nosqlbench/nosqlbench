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

/** Contiguous logical index window over a fixed vector reader. */
public final class WindowedVectorReader<A> implements VectorReader<A> {
    private final VectorReader<A> delegate; private final long begin, count;
    public WindowedVectorReader(VectorReader<A> delegate, String expression) {
        this.delegate = delegate;
        String[] parts = expression.trim().replace("[", "").replace("]", "").split("\\.\\.", -1);
        if (parts.length != 2) throw new VectorDataException("Window must be a contiguous start..end range: " + expression);
        try { begin = parts[0].isBlank() ? 0 : Long.parseLong(parts[0].trim()); long end = parts[1].isBlank() ? delegate.count() : Long.parseLong(parts[1].trim()); count = end - begin; }
        catch (NumberFormatException e) { throw new VectorDataException("Invalid window " + expression, e); }
        if (begin < 0 || count < 0 || begin > delegate.count() - count) throw new VectorDataException("Window lies outside source: " + expression);
    }
    @Override public long count() { return count; }
    @Override public int dimension() { return delegate.dimension(); }
    @Override public A get(long index) { check(index); return delegate.get(begin + index); }
    @Override public void get(long index, A target) { check(index); delegate.get(begin + index, target); }
    private void check(long index) { if (index < 0 || index >= count) throw new IndexOutOfBoundsException(); }
    @Override public void prebuffer(PrebufferProgress progress) { delegate.prebuffer(progress); }
    @Override public boolean isComplete() { return delegate.isComplete(); }
    @Override public CacheStats cacheStats() { return delegate.cacheStats(); }
    @Override public ElementType elementType() { return delegate.elementType(); }
}
