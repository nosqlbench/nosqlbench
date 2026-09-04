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
import io.nosqlbench.vectordata.VectorReaders;
import io.nosqlbench.vectordata.VvecReader;

import java.util.concurrent.atomic.AtomicReferenceArray;

/// A variable-length facet spread across several files, presented as
/// one reader. Each **file** has its own `IDXFOR__` sidecar whose
/// offsets are local to that file, so a window touching one shard loads
/// one file's index rather than the whole facet's — and two shards
/// slicing one file load that index once. Files open on first read.
public final class ShardedVvecReader<A> implements VvecReader<A> {
    private final FacetSeries series;
    private final AtomicReferenceArray<VvecReader<A>> readers;
    private final ElementType type;
    private final long count;

    public ShardedVvecReader(FacetSeries series) {
        this.series = series;
        this.readers = new AtomicReferenceArray<>(series.fileCount());
        this.type = ElementType.forExtension(series.filePath(0));
        this.count = series.shards().count();
    }

    @SuppressWarnings("unchecked")
    private VvecReader<A> reader(int file) {
        VvecReader<A> existing = readers.get(file);
        if (existing != null) return existing;
        VvecReader<A> opened = (VvecReader<A>) VectorReaders.openVvec(series.fileUri(file), series.settings(), series.identity());
        readers.compareAndSet(file, null, opened);
        return readers.get(file);
    }

    private VvecReader<A> at(long index, long[] fileOrdinal) {
        Shards.Located located = series.shards().locate(index);
        if (located == null) throw new IndexOutOfBoundsException("vvec index " + index + " outside 0.." + (count - 1));
        fileOrdinal[0] = located.fileOrdinal();
        return reader(series.fileIndexOfShard(located.shard()));
    }

    @Override public long count() { return count; }
    @Override public int dimensionAt(long index) { long[] at = new long[1]; return at(index, at).dimensionAt(at[0]); }
    @Override public A get(long index) { long[] at = new long[1]; return at(index, at).get(at[0]); }
    @Override public void prebuffer(PrebufferProgress progress) {
        for (int file = 0; file < series.fileCount(); file++) reader(file).prebuffer(progress);
    }
    @Override public boolean isComplete() {
        for (int file = 0; file < series.fileCount(); file++) if (!reader(file).isComplete()) return false;
        return true;
    }
    @Override public CacheStats cacheStats() { return series.cacheStats(); }
    @Override public ElementType elementType() { return type; }
}
