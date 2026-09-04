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
package io.nosqlbench.vectordata;

import java.util.Map;
import java.util.Optional;

/** A selected dataset profile with lazily opened standard and custom facets. */
public interface TestDataView {
    String dataset();
    String profile();
    Map<String, FacetDescriptor> facets();
    Optional<FacetDescriptor> facet(String name);
    VectorReader<float[]> baseVectors();
    VectorReader<float[]> queryVectors();
    VectorReader<int[]> neighborIndices();
    VectorReader<float[]> neighborDistances();
    VectorReader<int[]> metadataResults();
    /// Opens a facet by name. A facet spanning several files presents
    /// the same reader surface as a single-file one — `count()` is the
    /// series total, `get(o)` resolves and reads from the owning shard —
    /// and callers that never ask about layout never learn it exists.
    VectorReader<?> openFacet(String name);
    VvecReader<?> openVariableFacet(String name);
    /// Dataset-level attributes from the manifest's `attributes:`
    /// block — `distance_function`, provenance fields, and whatever
    /// else the publisher recorded. Empty when the manifest declares
    /// none. This is how a workload derives configuration from the
    /// dataset instead of restating it.
    Map<String, Object> attributes();

    /// Drives every facet of this profile to resident state, each
    /// fetched **against the window it declares** — a sized profile
    /// over a multi-terabyte base pulls what it can address and nothing
    /// more, a series included. A declared window the format cannot
    /// map is refused under [WholeFacetFallback#REFUSE] and fetched
    /// whole under [WholeFacetFallback#ALLOW], exactly as a requested
    /// window is: widening a profile's own window to its whole base in
    /// silence is not a fallback, it is the download the window existed
    /// to prevent. Every facet is planned before any is fetched, so a
    /// refusal costs nothing.
    void prebuffer(WholeFacetFallback fallback, PrebufferProgress progress);

    /// Same as [#prebuffer(WholeFacetFallback, PrebufferProgress)] under
    /// [WholeFacetFallback#REFUSE].
    default void prebuffer(PrebufferProgress progress) { prebuffer(WholeFacetFallback.REFUSE, progress); }

    /// What prefetching `window` on `facet` would cost, without
    /// fetching any of it. `window` is in **record** coordinates and is
    /// the caller's to choose: a profile's `window:` is a convenience —
    /// a name for a range someone wants repeatedly — not a fence around
    /// which ranges may be asked for. Planning fetches nothing, so it
    /// needs no [WholeFacetFallback] permission — finding out is how a
    /// caller decides.
    PrefetchPlan prefetchPlan(String facet, DSWindow window);

    /// Fetches `window` of `facet` and returns when it is resident,
    /// reporting per-range progress through `progress`. An empty window
    /// requests the whole facet. A window that cannot be resolved for
    /// the facet's format fails under [WholeFacetFallback#REFUSE]
    /// rather than quietly fetching everything. Across a series the
    /// window costs the shards it spans, and only those.
    PrefetchReport prefetch(String facet, DSWindow window, WholeFacetFallback fallback, PrebufferProgress progress);

    /// Same as [#prefetch(String, DSWindow, WholeFacetFallback, PrebufferProgress)]
    /// without progress reporting.
    default PrefetchReport prefetch(String facet, DSWindow window, WholeFacetFallback fallback) {
        return prefetch(facet, window, fallback, PrebufferProgress.NONE);
    }

    /// Starts fetching `window` of `facet` on another thread. The plan
    /// is computed before this returns — a caller deserves the cost
    /// before committing — so an exception here is a planning or
    /// permission failure; fetch failures arrive through
    /// [PrefetchHandle#join]. This is the form for warming ahead of a
    /// scan: reads that overtake the prefetch are not wrong, only
    /// slower — they fault the chunk in themselves, and the prefetch
    /// skips what is already resident.
    PrefetchHandle prefetchInBackground(String facet, DSWindow window, WholeFacetFallback fallback);
}
