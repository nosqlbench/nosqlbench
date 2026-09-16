/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.virtdata.lib.vectors.vectordata;

import io.nosqlbench.vectordata.VectorReader;

/// What a ground-truth facet can pay out: for each query row, how many
/// of its `k` entries name a neighbor that exists — a negative entry is
/// a sentinel for one that does not, which a filtered search over a
/// small slice leaves behind whenever a predicate matches fewer than
/// `k` rows. `ceiling` is the recall@k a perfect search would score
/// against this facet under the strict `recall` measure, so a run that
/// reads below it has found something and a run that reads at it has
/// found everything there was.
///
/// Computed once, before anything is loaded, from the facet alone: a
/// few megabytes read, against the hours a load can cost. A regime
/// that gates on it never mistakes a padded ground truth for a search
/// that missed.
public record GroundTruthCoverage(String facet, long queries, int k, long fullRows, long emptyRows, long attainableEntries, double ceiling) {

    /// Surveys a ground-truth reader whose rows are `int[]` of `k`
    /// neighbor ordinals, negative for a sentinel.
    public static GroundTruthCoverage of(String facet, VectorReader<?> reader) {
        long queries = reader.count();
        int k = reader.dimension();
        long full = 0, empty = 0, attainable = 0;
        for (long ordinal = 0; ordinal < queries; ordinal++) {
            Object row = reader.get(ordinal);
            if (!(row instanceof int[] entries))
                throw new IllegalArgumentException("ground truth facet '" + facet + "' holds " + (row == null ? "null" : row.getClass().getSimpleName()) + " rows, not int[]");
            int present = 0;
            for (int entry : entries) if (entry >= 0) present++;
            present = Math.min(present, k);
            attainable += present;
            if (present >= k) full++;
            if (present == 0) empty++;
        }
        double ceiling = queries == 0 || k == 0 ? 1.0 : (double) attainable / ((double) queries * k);
        return new GroundTruthCoverage(facet, queries, k, full, empty, attainable, ceiling);
    }

    /// Whether every query can be answered in full: no row is padded.
    public boolean isComplete() { return fullRows == queries; }

    /// Refuses a ceiling below `minimum`, naming what the facet holds
    /// and what to do about it, so a run whose recall could never be
    /// read is stopped before it loads anything.
    public GroundTruthCoverage require(double minimum) {
        if (ceiling >= minimum) return this;
        throw new IllegalStateException(String.format(
            "%s: attainable recall@%d ceiling is %.4f, below the required %.2f — %d of %d queries have all %d neighbors, %d have none; "
            + "a strict recall reading on this profile cannot rise above the ceiling. Choose a profile whose predicates match at least %d rows "
            + "(a larger size, or a uniform predicate set with selectivity x base_count >= %d), or lower min_attainable for a smoke run "
            + "and read recall_attainable instead.",
            facet, k, ceiling, minimum, fullRows, queries, k, emptyRows, k, k));
    }

    @Override public String toString() {
        return String.format("%s: %d queries, %d with all %d neighbors, %d with none; attainable recall@%d ceiling %.4f",
            facet, queries, fullRows, k, emptyRows, k, ceiling);
    }
}
