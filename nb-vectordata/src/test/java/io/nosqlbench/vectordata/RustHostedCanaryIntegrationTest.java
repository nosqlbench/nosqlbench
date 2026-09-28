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

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.*;

/// Optional release canary against a dataset published by
/// vectordata-rs, run with `-Dvectordata.canary.catalog=<location>
/// -Dvectordata.canary.dataset=<name>` and optionally
/// `-Dvectordata.canary.profile=<profile>` and
/// `-Dvectordata.canary.cache=<dir>`.
///
/// It walks the paths a workload takes: opens the profile, reads base
/// records at both ends of the profile's window — across a shard seam
/// when the base is a series — reads a query and a neighbor row, plans
/// every facet against the window it declares (a slab's included) and
/// requires that none degrades to a whole-facet fetch, then prefetches
/// a small base window and confirms it resident. Nothing large is
/// downloaded: the plan is the check.
class RustHostedCanaryIntegrationTest {
    @Test void opensConfiguredRustHostedDataset() {
        String catalog = System.getProperty("vectordata.canary.catalog");
        String dataset = System.getProperty("vectordata.canary.dataset");
        assumeTrue(catalog != null && !catalog.isBlank() && dataset != null && !dataset.isBlank(),
            "Configure vectordata.canary.catalog and vectordata.canary.dataset to enable the Rust-hosted canary");
        String profile = System.getProperty("vectordata.canary.profile", "default");
        String cache = System.getProperty("vectordata.canary.cache", System.getProperty("java.io.tmpdir") + "/nb-vectordata-canary-cache");
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(Path.of(cache)).build();
        TestDataView view = Catalog.of(CatalogSources.of(URI.create(catalog)), settings).open(dataset, profile);
        StringBuilder report = new StringBuilder("canary " + dataset + ":" + view.profile() + "\n");

        VectorReader<float[]> base = view.baseVectors();
        long count = base.count();
        assertTrue(count > 0, "canary base vectors must be non-empty");
        int dimension = base.dimension();
        assertTrue(dimension > 0);
        float[] first = base.get(0), last = base.get(count - 1);
        assertEquals(dimension, first.length);
        assertEquals(dimension, last.length);
        FacetDescriptor baseFacet = view.facet("base_vectors").orElseThrow();
        report.append("  base_vectors: ").append(count).append(" x ").append(dimension)
            .append(baseFacet.isSeries() ? " over " + baseFacet.series().entries().size() + " declared source(s)" : " in one file")
            .append(baseFacet.window() != null ? ", window " + baseFacet.window() : "").append('\n');

        if (view.facet("query_vectors").isPresent()) {
            VectorReader<float[]> queries = view.queryVectors();
            assertTrue(queries.count() > 0);
            assertEquals(dimension, queries.get(0).length, "queries share the base dimension");
            report.append("  query_vectors: ").append(queries.count()).append('\n');
        }
        if (view.facet("neighbor_indices").isPresent()) {
            VectorReader<int[]> neighbors = view.neighborIndices();
            assertTrue(neighbors.count() > 0);
            int k = neighbors.dimension();
            assertTrue(k > 0);
            int[] row = neighbors.get(0);
            assertEquals(k, row.length);
            for (int neighbor : row) assertTrue(neighbor >= 0 && neighbor < count, "neighbor " + neighbor + " names a base record");
            report.append("  neighbor_indices: ").append(neighbors.count()).append(" x ").append(k).append('\n');
        }

        // Every facet plans against the window it declares, and none of
        // them degrades: a declared window the format cannot map is the
        // download the window existed to prevent.
        for (Map.Entry<String, FacetDescriptor> facet : view.facets().entrySet()) {
            String window = facet.getValue().window();
            PrefetchPlan plan = view.prefetchPlan(facet.getKey(), window == null ? DSWindow.ALL : DSWindow.parse(window));
            assertFalse(plan.degradesToFullDownload(), facet.getKey() + ": its declared window " + window + " must map");
            assertTrue(plan.facetBytes() > 0, facet.getKey() + " has bytes");
            report.append("  plan ").append(facet.getKey()).append(window == null ? " (whole)" : " [" + window + "]")
                .append(": ").append(plan.requests()).append(" request(s), ")
                .append(plan.byteRanges().stream().mapToLong(ShardRange::length).sum()).append(" of ").append(plan.facetBytes())
                .append(" bytes, prerequisite ").append(plan.prerequisiteBytes()).append('\n');
            if (window != null) assertTrue(plan.byteRanges().stream().mapToLong(ShardRange::length).sum() <= plan.facetBytes());
        }

        DSWindow small = DSWindow.parse("0.." + Math.min(64, count));
        PrefetchReport fetched = view.prefetch("base_vectors", small, WholeFacetFallback.REFUSE);
        assertTrue(fetched.rangesFetched() >= 1);
        assertTrue(view.prefetchPlan("base_vectors", small).isResident(), "the prefetched window reads as resident");
        report.append("  attributes: ").append(view.attributes()).append('\n');
        System.out.print(report);
    }
}
