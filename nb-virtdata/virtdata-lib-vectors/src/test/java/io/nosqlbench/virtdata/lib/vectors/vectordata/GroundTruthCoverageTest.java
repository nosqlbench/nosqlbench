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

import io.nosqlbench.vectordata.CacheStats;
import io.nosqlbench.vectordata.ElementType;
import io.nosqlbench.vectordata.PrebufferProgress;
import io.nosqlbench.vectordata.VectorReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// The ceiling a padded ground truth imposes on strict recall, read
/// from the facet before anything is loaded.
@Tag("unit")
class GroundTruthCoverageTest {

    /// A ground-truth reader over fixed rows.
    private static VectorReader<int[]> rows(List<int[]> rows) {
        return new VectorReader<>() {
            @Override public long count() { return rows.size(); }
            @Override public int dimension() { return rows.get(0).length; }
            @Override public int[] get(long ordinal) { return rows.get((int) ordinal); }
            @Override public void prebuffer(PrebufferProgress progress) { }
            @Override public boolean isComplete() { return true; }
            @Override public CacheStats cacheStats() { return null; }
            @Override public ElementType elementType() { return ElementType.I32; }
        };
    }

    @Test void aFullFacetHasACeilingOfOne() {
        GroundTruthCoverage coverage = GroundTruthCoverage.of("gt", rows(List.of(new int[] {1, 2, 3, 4}, new int[] {5, 6, 7, 8})));
        assertEquals(1.0, coverage.ceiling(), 1e-9);
        assertTrue(coverage.isComplete());
        assertEquals(2, coverage.fullRows());
        assertEquals(0, coverage.emptyRows());
        assertSame(coverage, coverage.require(0.95), "a complete facet passes any gate up to one");
    }

    @Test void sentinelsLowerTheCeilingByTheNeighborsTheyStandFor() {
        // 4 + 2 + 0 + 4 attainable of 16: half the neighbors exist.
        GroundTruthCoverage coverage = GroundTruthCoverage.of("gt", rows(List.of(
            new int[] {1, 2, 3, 4}, new int[] {5, 6, -1, -1}, new int[] {-1, -1, -1, -1}, new int[] {9, 10, 11, 12})));
        assertEquals(0.625, coverage.ceiling(), 1e-9);
        assertEquals(2, coverage.fullRows());
        assertEquals(1, coverage.emptyRows());
        assertEquals(10, coverage.attainableEntries());
        assertFalse(coverage.isComplete());
        IllegalStateException refused = assertThrows(IllegalStateException.class, () -> coverage.require(0.95));
        assertTrue(refused.getMessage().contains("0.6250"), refused.getMessage());
        assertTrue(refused.getMessage().contains("2 of 4 queries"), refused.getMessage());
        assertTrue(refused.getMessage().contains("recall_attainable"), "names the measure that reads through padding: " + refused.getMessage());
        assertSame(coverage, coverage.require(0.5), "a lowered gate lets a smoke run through");
        assertTrue(coverage.toString().contains("ceiling 0.6250"), coverage.toString());
    }
}
