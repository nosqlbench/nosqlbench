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

import io.nosqlbench.vectordata.ByteRange;
import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.FacetDescriptor;
import io.nosqlbench.vectordata.FacetNames;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.PrefetchHandle;
import io.nosqlbench.vectordata.PrefetchPlan;
import io.nosqlbench.vectordata.PrefetchReport;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.vectordata.VectorReaders;
import io.nosqlbench.vectordata.VvecReader;
import io.nosqlbench.vectordata.WholeFacetFallback;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Default TestDataView implementation backed by resolved manifest descriptors. */
public final class ManifestView implements TestDataView {
    private final String dataset, profile; private final Map<String, FacetDescriptor> facets; private final VectorDataSettings settings;
    /// One handle per facet, so a plan and the fetch that follows it —
    /// and every later ask through this view — share one loaded offset
    /// index rather than loading it repeatedly.
    private final Map<String, Prefetcher.FacetHandle> handles = new ConcurrentHashMap<>();
    public ManifestView(String dataset, String profile, Map<String, FacetDescriptor> facets, VectorDataSettings settings) {
        this.dataset = dataset; this.profile = profile; this.facets = Map.copyOf(new LinkedHashMap<>(facets)); this.settings = settings;
    }
    @Override public String dataset() { return dataset; }
    @Override public String profile() { return profile; }
    @Override public Map<String, FacetDescriptor> facets() { return facets; }
    @Override public Optional<FacetDescriptor> facet(String name) { return Optional.ofNullable(facets.get(canonical(name))); }
    @Override public VectorReader<float[]> baseVectors() { return fixed("base_vectors", VectorReaders::f32); }
    @Override public VectorReader<float[]> queryVectors() { return fixed("query_vectors", VectorReaders::f32); }
    @Override public VectorReader<int[]> neighborIndices() { return fixed("neighbor_indices", VectorReaders::i32); }
    @Override public VectorReader<float[]> neighborDistances() { return fixed("neighbor_distances", VectorReaders::f32); }
    @Override public VectorReader<int[]> metadataResults() { return fixed("metadata_results", VectorReaders::i32); }
    @Override public VectorReader<?> openFacet(String name) { FacetDescriptor facet = require(name); return window(VectorReaders.open(facet.source(), settings, dataset), facet); }
    @Override public VvecReader<?> openVariableFacet(String name) { FacetDescriptor facet = require(name); return VectorReaders.openVvec(facet.source(), settings, dataset); }
    @Override public void prebuffer(PrebufferProgress progress) { for (String name : facets.keySet()) openFacet(name).prebuffer(progress); }
    @Override public PrefetchPlan prefetchPlan(String facet, DSWindow window) { return Prefetcher.plan(handle(facet), window); }
    @Override public PrefetchReport prefetch(String facet, DSWindow window, WholeFacetFallback fallback, PrebufferProgress progress) {
        // One handle for both the plan and the fetch, so the offset
        // index a vvec window needs is loaded once rather than twice.
        Prefetcher.FacetHandle handle = handle(facet);
        PrefetchPlan planned = Prefetcher.plan(handle, window);
        Prefetcher.checkFallback(facet, planned, fallback);
        ByteStorage storage = handle.data();
        if (planned.degradesToFullDownload()) { storage.prebuffer(progress); return new PrefetchReport(planned, 1); }
        int fetched = 0;
        for (ByteRange range : planned.byteRanges()) { storage.prebufferRange(range.start(), range.end(), progress); fetched++; }
        return new PrefetchReport(planned, fetched);
    }
    @Override public PrefetchHandle prefetchInBackground(String facet, DSWindow window, WholeFacetFallback fallback) {
        Prefetcher.FacetHandle handle = handle(facet);
        PrefetchPlan planned = Prefetcher.plan(handle, window);
        Prefetcher.checkFallback(facet, planned, fallback);
        ByteStorage storage = handle.data();
        return PrefetchHandle.launch(facet, planned, ticker -> {
            if (planned.degradesToFullDownload()) { storage.prebuffer((cached, total) -> ticker.bytes(cached)); return; }
            long fetched = 0;
            for (ByteRange range : planned.byteRanges()) {
                if (ticker.cancelled()) break;
                long base = fetched;
                long[] seen = {0};
                storage.prebufferRange(range.start(), range.end(), (cached, total) -> { seen[0] = cached; ticker.bytes(base + cached); });
                fetched = base + seen[0];
                ticker.rangeDone();
            }
        });
    }
    private Prefetcher.FacetHandle handle(String name) {
        return handles.computeIfAbsent(canonical(name), key -> new Prefetcher.FacetHandle(require(key), settings, dataset));
    }
    private FacetDescriptor require(String name) { return facet(name).orElseThrow(() -> new VectorDataException("Profile " + profile + " lacks facet " + name)); }
    private <T> VectorReader<T> fixed(String name, FixedFactory<T> factory) { FacetDescriptor facet = require(name); return window(factory.open(facet.source(), settings, dataset), facet); }
    private static <T> VectorReader<T> window(VectorReader<T> reader, FacetDescriptor facet) { return facet.window() == null || facet.window().isBlank() ? reader : new WindowedVectorReader<>(reader, facet.window()); }
    private static String canonical(String name) { return FacetNames.canonical(name); }
    @FunctionalInterface private interface FixedFactory<T> { VectorReader<T> open(URI source, VectorDataSettings settings, String identity); }
}
