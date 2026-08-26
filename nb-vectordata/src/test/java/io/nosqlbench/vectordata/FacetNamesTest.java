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

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// Pins the complete `StandardFacet` canonical/alias table from
/// `vectordata-rs` so any drift between the reference implementation
/// and this port is a deliberate change, not an accident.
@Tag("unit")
class FacetNamesTest {
    @Test void everyRustAliasResolvesToItsCanonicalKey() {
        Map<String, List<String>> table = Map.ofEntries(
            Map.entry("base_vectors", List.of("base", "train")),
            Map.entry("query_vectors", List.of("query", "queries", "test")),
            Map.entry("neighbor_indices", List.of("indices", "neighbors", "ground_truth", "gt")),
            Map.entry("neighbor_distances", List.of("distances")),
            Map.entry("metadata_content", List.of("content", "meta_content", "meta_base")),
            Map.entry("metadata_predicates", List.of("meta_predicates")),
            Map.entry("metadata_results", List.of("meta_results", "predicate_results", "metadata_indices")),
            Map.entry("metadata_layout", List.of("layout", "meta_layout")),
            Map.entry("prefiltered_neighbor_indices", List.of(
                "prefiltered_indices", "prefiltered_gt", "prefiltered_ground_truth", "prefilter_indices",
                "filtered_neighbor_indices", "filtered_indices", "filtered_gt", "filtered_ground_truth")),
            Map.entry("prefiltered_neighbor_distances", List.of(
                "prefiltered_distances", "prefilter_distances", "prefiltered_neighbors",
                "filtered_neighbor_distances", "filtered_distances", "filtered_neighbors")),
            Map.entry("postfiltered_neighbor_indices", List.of(
                "postfiltered_indices", "postfiltered_gt", "postfiltered_ground_truth", "postfilter_indices")),
            Map.entry("postfiltered_neighbor_distances", List.of(
                "postfiltered_distances", "postfilter_distances", "postfiltered_neighbors")));
        table.forEach((canonical, aliases) -> {
            assertEquals(canonical, FacetNames.canonical(canonical), "canonical keys pass through");
            for (String alias : aliases) assertEquals(canonical, FacetNames.canonical(alias), alias);
        });
    }

    @Test void customFacetNamesPassThroughUnchanged() {
        assertEquals("custom_scores", FacetNames.canonical("custom_scores"));
    }
}
