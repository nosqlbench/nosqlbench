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

/// Binding-layer clip of a [VectorReader] to a record window, with the
/// same semantics a profile-declared window gives a reader in
/// nb-vectordata (and in `vectordata-rs`): only the first interval
/// applies, and both bounds clamp to the underlying reader's count.
/// This adapter lives here rather than in nb-vectordata because the
/// vectordata crate exposes no caller-side reader windowing — windows
/// there come from facet configuration — and the binding functions'
/// window argument is a binding-layer convenience.
public final class WindowedReader<A> implements VectorReader<A> {

    private final VectorReader<A> inner;
    private final long begin, count;

    /// Clips `reader` to the first interval of `window`, in the
    /// reader's own index coordinates. A blank window returns the
    /// reader unchanged.
    public static <A> VectorReader<A> clip(VectorReader<A> reader, String window) {
        DSWindow parsed = DSWindow.parse(window == null ? "" : window);
        if (parsed.isEmpty()) return reader;
        DSWindow.Interval first = parsed.intervals().get(0);
        long total = reader.count();
        long begin = Math.min(first.minIncl(), total);
        long count = Math.max(0, Math.min(first.maxExcl(), total) - begin);
        return new WindowedReader<>(reader, begin, count);
    }

    private WindowedReader(VectorReader<A> inner, long begin, long count) {
        this.inner = inner; this.begin = begin; this.count = count;
    }

    @Override public long count() { return count; }
    @Override public int dimension() { return inner.dimension(); }
    @Override public A get(long index) { check(index); return inner.get(begin + index); }
    @Override public void get(long index, A target) { check(index); inner.get(begin + index, target); }
    private void check(long index) { if (index < 0 || index >= count) throw new IndexOutOfBoundsException("windowed index " + index + " outside 0.." + (count - 1)); }
    @Override public void prebuffer(PrebufferProgress progress) { inner.prebuffer(progress); }
    @Override public boolean isComplete() { return inner.isComplete(); }
    @Override public CacheStats cacheStats() { return inner.cacheStats(); }
    @Override public ElementType elementType() { return inner.elementType(); }
}
