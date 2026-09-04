package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.vectordata.CacheStats;
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.VectorReader;

/// Restricts a [VectorReader] to a record range **without renumbering
/// it**: a window of `[50000..100000)` means indices 50000 through
/// 99999 are readable and everything else is refused. Indices stay
/// absolute, so the same ordinals mean the same records here as
/// everywhere else in this package.
///
/// Used by the `windowedFacet` expression function, where a caller has
/// explicitly asked for a restricted reader. The binding functions do
/// **not** wrap their readers in this: a binding window only warms, and
/// restricting it would break resolution, since VirtData probes a newly
/// resolved function with a small sample index before any cycle runs.
///
/// Only the first interval of the window applies, and both bounds clamp
/// to the underlying reader's count.
public final class WindowedReader<A> implements VectorReader<A> {

    private final VectorReader<A> inner;
    private final long begin, end;

    /// Restricts `reader` to the first interval of `window`, in the
    /// reader's own — absolute — index coordinates. A blank window
    /// returns the reader unchanged.
    public static <A> VectorReader<A> clip(VectorReader<A> reader, String window) {
        DSWindow parsed = DSWindow.parse(window == null ? "" : window);
        if (parsed.isEmpty()) return reader;
        DSWindow.Interval first = parsed.intervals().get(0);
        long total = reader.count();
        return new WindowedReader<>(reader, Math.min(first.minIncl(), total), Math.min(first.maxExcl(), total));
    }

    private WindowedReader(VectorReader<A> inner, long begin, long end) {
        this.inner = inner; this.begin = begin; this.end = end;
    }

    /// The exclusive upper bound of the readable range — the highest
    /// index this reader accepts, plus one. Indices below the window's
    /// start are refused even though they are below this count, because
    /// the window is a restriction rather than a re-basing.
    @Override public long count() { return end; }
    /// First index this reader accepts.
    public long begin() { return begin; }
    @Override public int dimension() { return inner.dimension(); }
    @Override public A get(long index) { check(index); return inner.get(index); }
    @Override public void get(long index, A target) { check(index); inner.get(index, target); }
    private void check(long index) {
        if (index < begin || index >= end)
            throw new IndexOutOfBoundsException("index " + index + " is outside the binding window ["
                + begin + ".." + end + "); window indices are absolute record ordinals, so cycles must fall inside it");
    }
    @Override public void prebuffer(PrebufferProgress progress) { inner.prebuffer(progress); }
    @Override public boolean isComplete() { return inner.isComplete(); }
    @Override public CacheStats cacheStats() { return inner.cacheStats(); }
    @Override public ElementType elementType() { return inner.elementType(); }
}
