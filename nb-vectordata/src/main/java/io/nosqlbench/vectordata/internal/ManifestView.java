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

import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.FacetDescriptor;
import io.nosqlbench.vectordata.FacetNames;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.PrefetchHandle;
import io.nosqlbench.vectordata.PrefetchPlan;
import io.nosqlbench.vectordata.PrefetchReport;
import io.nosqlbench.vectordata.ShardRange;
import io.nosqlbench.vectordata.TestDataView;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VectorReader;
import io.nosqlbench.vectordata.VectorReaders;
import io.nosqlbench.vectordata.VvecReader;
import io.nosqlbench.vectordata.WholeFacetFallback;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Default TestDataView implementation backed by resolved manifest descriptors. */
public final class ManifestView implements TestDataView {
    private final String dataset, profile; private final Map<String, FacetDescriptor> facets; private final VectorDataSettings settings;
    private final Map<String, Object> attributes;
    /// One handle per facet, so a plan and the fetch that follows it —
    /// and every later ask through this view — share one loaded offset
    /// index and one realized series rather than loading them
    /// repeatedly.
    private final Map<String, Prefetcher.FacetHandle> handles = new ConcurrentHashMap<>();
    public ManifestView(String dataset, String profile, Map<String, FacetDescriptor> facets, VectorDataSettings settings) {
        this(dataset, profile, facets, settings, Map.of());
    }
    public ManifestView(String dataset, String profile, Map<String, FacetDescriptor> facets, VectorDataSettings settings,
                        Map<String, Object> attributes) {
        this.dataset = dataset; this.profile = profile; this.facets = Map.copyOf(new LinkedHashMap<>(facets)); this.settings = settings;
        this.attributes = Map.copyOf(attributes);
    }
    @Override public String dataset() { return dataset; }
    @Override public String profile() { return profile; }
    @Override public Map<String, FacetDescriptor> facets() { return facets; }
    @Override public Optional<FacetDescriptor> facet(String name) { return Optional.ofNullable(facets.get(canonical(name))); }
    @Override public VectorReader<float[]> baseVectors() { return fixed("base_vectors", ElementType.F32); }
    @Override public VectorReader<float[]> queryVectors() { return fixed("query_vectors", ElementType.F32); }
    @Override public VectorReader<int[]> neighborIndices() { return fixed("neighbor_indices", ElementType.I32); }
    @Override public VectorReader<float[]> neighborDistances() { return fixed("neighbor_distances", ElementType.F32); }
    @Override public VectorReader<int[]> metadataResults() { return fixed("metadata_results", ElementType.I32); }
    @Override public VectorReader<?> openFacet(String name) { FacetDescriptor facet = require(name); return window(facet, open(facet)); }
    @Override public VvecReader<?> openVariableFacet(String name) {
        FacetDescriptor facet = require(name);
        return facet.isSeries() ? new ShardedVvecReader<>(handle(name).series()) : VectorReaders.openVvec(facet.source(), settings, dataset);
    }
    @Override public Map<String, Object> attributes() { return attributes; }
    @Override public void prebuffer(PrebufferProgress progress) { for (String name : facets.keySet()) openFacet(name).prebuffer(progress); }
    @Override public PrefetchPlan prefetchPlan(String facet, DSWindow window) { return Prefetcher.plan(handle(facet), window); }
    @Override public PrefetchReport prefetch(String facet, DSWindow window, WholeFacetFallback fallback, PrebufferProgress progress) {
        // One handle for both the plan and the fetch, so the offset
        // index a vvec window needs is loaded once rather than twice.
        Prefetcher.FacetHandle handle = handle(facet);
        PrefetchPlan planned = Prefetcher.plan(handle, window);
        Prefetcher.checkFallback(facet, planned, fallback);
        return new PrefetchReport(planned, fetch(handle, planned, progress));
    }
    @Override public PrefetchHandle prefetchInBackground(String facet, DSWindow window, WholeFacetFallback fallback) {
        Prefetcher.FacetHandle handle = handle(facet);
        PrefetchPlan planned = Prefetcher.plan(handle, window);
        Prefetcher.checkFallback(facet, planned, fallback);
        return PrefetchHandle.launch(facet, planned, ticker -> {
            if (planned.degradesToFullDownload()) { handle.prebufferWhole((cached, total) -> ticker.bytes(cached)); return; }
            long fetched = 0;
            for (ShardRange range : planned.byteRanges()) {
                if (ticker.cancelled()) break;
                long base = fetched;
                long[] seen = {0};
                handle.prebufferShardRange(range.shard(), range.start(), range.end(), (cached, total) -> { seen[0] = cached; ticker.bytes(base + cached); });
                fetched = base + seen[0];
                ticker.rangeDone();
            }
        });
    }
    /// Fetches what a plan says, range by range within the shard each
    /// names, and reports how many ranges were issued.
    private static int fetch(Prefetcher.FacetHandle handle, PrefetchPlan plan, PrebufferProgress progress) {
        if (plan.degradesToFullDownload()) { handle.prebufferWhole(progress); return 1; }
        int fetched = 0;
        for (ShardRange range : plan.byteRanges()) { handle.prebufferShardRange(range.shard(), range.start(), range.end(), progress); fetched++; }
        return fetched;
    }
    private Prefetcher.FacetHandle handle(String name) {
        return handles.computeIfAbsent(canonical(name), key -> new Prefetcher.FacetHandle(require(key), settings, dataset));
    }
    private FacetDescriptor require(String name) { return facet(name).orElseThrow(() -> new VectorDataException("Profile " + profile + " lacks facet " + name)); }
    private VectorReader<?> open(FacetDescriptor facet) {
        return facet.isSeries() ? new ShardedVectorReader<>(handle(facet.name()).series()) : VectorReaders.open(facet.source(), settings, dataset);
    }
    private <T> VectorReader<T> fixed(String name, ElementType type) {
        FacetDescriptor facet = require(name);
        return window(facet, VectorReaders.expectFixed(open(facet), type, facet.isSeries() ? name : facet.source()));
    }
    /// Wraps a reader in the facet's declared window — in facet
    /// ordinals, so it selects the same records over one file or over
    /// five.
    private static <T> VectorReader<T> window(FacetDescriptor facet, VectorReader<T> reader) {
        return facet.window() == null || facet.window().isBlank() ? reader : new WindowedVectorReader<>(reader, facet.window());
    }
    private static String canonical(String name) { return FacetNames.canonical(name); }
}
