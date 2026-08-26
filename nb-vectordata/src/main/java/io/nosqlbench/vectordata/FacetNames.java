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

/// Canonical facet names and their accepted shorthand aliases, ported
/// from the `vectordata-rs` `StandardFacet` table. The canonical key is
/// what `dataset.yaml` profile definitions use (`base_vectors`,
/// `query_vectors`, `neighbor_indices`, ...); the aliases are the
/// shorter names YAML authors and legacy `knn_entries` catalogs use
/// (`base`, `query`, `gt`, ...). Every facet lookup and every manifest
/// key resolves through this one table, so a dataset published with
/// canonical keys and one published with aliases read identically.
public final class FacetNames {
    private FacetNames() { }

    /// Resolves a facet name to its canonical key. Canonical keys and
    /// unrecognized (custom-facet) names pass through unchanged.
    public static String canonical(String name) {
        return switch (name) {
            case "base", "train" -> "base_vectors";
            case "query", "queries", "test" -> "query_vectors";
            case "indices", "neighbors", "ground_truth", "gt" -> "neighbor_indices";
            case "distances" -> "neighbor_distances";
            case "content", "meta_content", "meta_base" -> "metadata_content";
            case "meta_predicates" -> "metadata_predicates";
            case "meta_results", "predicate_results", "metadata_indices" -> "metadata_results";
            case "layout", "meta_layout" -> "metadata_layout";
            case "prefiltered_indices", "prefiltered_gt", "prefiltered_ground_truth", "prefilter_indices",
                 "filtered_neighbor_indices", "filtered_indices", "filtered_gt", "filtered_ground_truth" ->
                "prefiltered_neighbor_indices";
            case "prefiltered_distances", "prefilter_distances", "prefiltered_neighbors",
                 "filtered_neighbor_distances", "filtered_distances", "filtered_neighbors" ->
                "prefiltered_neighbor_distances";
            case "postfiltered_indices", "postfiltered_gt", "postfiltered_ground_truth", "postfilter_indices" ->
                "postfiltered_neighbor_indices";
            case "postfiltered_distances", "postfilter_distances", "postfiltered_neighbors" ->
                "postfiltered_neighbor_distances";
            // Retained pre-table Java aliasing, documented in
            // RUST_COMPATIBILITY.md before the full table was ported.
            case "filtered" -> "prefiltered";
            default -> name;
        };
    }
}
