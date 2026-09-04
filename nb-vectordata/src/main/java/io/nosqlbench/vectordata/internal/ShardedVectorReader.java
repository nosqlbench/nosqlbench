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
import io.nosqlbench.vectordata.VectorReaders;

import java.util.concurrent.atomic.AtomicReferenceArray;

/// A fixed-record facet spread across several files, presented as one
/// reader: `count()` is the series total, `dimension()` the shared
/// dimension, and `get(o)` resolves the owning shard and reads from its
/// file. Callers that never ask about layout never learn it exists.
///
/// Every file is opened at construction to check that it agrees on
/// dimension and element width — a disagreement means these files are
/// not one facet, and is refused rather than tolerated.
public final class ShardedVectorReader<A> implements VectorReader<A> {
    private final FacetSeries series;
    private final AtomicReferenceArray<VectorReader<A>> readers;
    private final ElementType type;
    private final boolean scalar;
    private final int dimension;
    private final long count;

    public ShardedVectorReader(FacetSeries series) {
        this.series = series;
        this.readers = new AtomicReferenceArray<>(series.fileCount());
        this.scalar = VectorReaders.isScalar(series.filePath(0));
        VectorReader<A> first = reader(0);
        this.type = first.elementType();
        this.dimension = first.dimension();
        for (int file = 1; file < series.fileCount(); file++) {
            VectorReader<A> other = reader(file);
            if (other.dimension() != dimension || other.elementType() != type)
                throw new VectorDataException("shard file " + file + " ('" + series.filePath(file) + "') has dimension " + other.dimension()
                    + " of " + other.elementType() + ", but the series is dimension " + dimension + " of " + type
                    + " — every shard of a facet must share one shape");
        }
        this.count = series.shards().count();
    }

    /// Whether the files are packed scalars rather than xvec records.
    public boolean isScalar() { return scalar; }

    @SuppressWarnings("unchecked")
    private VectorReader<A> reader(int file) {
        VectorReader<A> existing = readers.get(file);
        if (existing != null) return existing;
        VectorReader<A> opened = (VectorReader<A>) VectorReaders.over(series.file(file), series.filePath(file));
        readers.compareAndSet(file, null, opened);
        return readers.get(file);
    }

    private VectorReader<A> at(long index, long[] fileOrdinal) {
        Shards.Located located = series.shards().locate(index);
        if (located == null) throw new IndexOutOfBoundsException("Vector index " + index + " outside 0.." + (count - 1));
        fileOrdinal[0] = located.fileOrdinal();
        return reader(series.fileIndexOfShard(located.shard()));
    }

    @Override public long count() { return count; }
    @Override public int dimension() { return dimension; }
    @Override public A get(long index) { long[] at = new long[1]; return at(index, at).get(at[0]); }
    @Override public void get(long index, A target) { long[] at = new long[1]; at(index, at).get(at[0], target); }
    /// Fetches every file whole; the window-scoped fetch is the view's
    /// prefetch, planned against the facet's declared window.
    @Override public void prebuffer(PrebufferProgress progress) {
        for (int file = 0; file < series.fileCount(); file++) series.file(file).prebuffer(progress);
    }
    @Override public boolean isComplete() {
        for (int file = 0; file < series.fileCount(); file++) if (!series.file(file).isComplete()) return false;
        return true;
    }
    @Override public CacheStats cacheStats() { return series.cacheStats(); }
    @Override public ElementType elementType() { return type; }
}
