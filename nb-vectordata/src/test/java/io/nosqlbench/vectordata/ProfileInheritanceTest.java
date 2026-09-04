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
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// What a profile inherits depends on the axis it varies along. Across
/// the size axis, per-profile outputs cannot be shared — ground truth is
/// derived from `base_count` — so a sized profile that omits its own
/// neighbor facets fails loudly instead of reading the full base's.
/// Across any other axis every facet is invariant and inherits as is.
@Tag("unit")
class ProfileInheritanceTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    private Path dataset(String profiles) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f}, {1f, 2f}, {2f, 3f}, {3f, 4f}});
        FixtureSupport.fvec(dir, "part.fvec", new float[][] {{7f, 7f}});
        FixtureSupport.fvec(dir, "query.fvec", new float[][] {{9f, 8f}});
        FixtureSupport.ivec(dir, "gt_default.ivecs", new int[][] {{1, 2}});
        FixtureSupport.ivec(dir, "gt_small.ivecs", new int[][] {{3, 4}});
        Files.writeString(dir.resolve("dataset.yaml"), "name: axes\nprofiles:\n" + profiles);
        return dir;
    }

    private TestDataView view(Path dir, String profile) { return TestDataGroup.load(dir.toUri(), settings()).profile(profile); }

    @Test void aSizedProfileDoesNotInheritGroundTruthAcrossTheSizeAxis() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
                neighbor_indices: gt_default.ivecs
              small:
                base_count: 2
            """);
        TestDataView small = view(dir, "small");
        assertEquals(2, small.baseVectors().count(), "the base inherits under the child's base_count window");
        assertArrayEquals(new float[] {9f, 8f}, small.queryVectors().get(0), "query vectors are invariant across sizes");
        assertTrue(small.facet("neighbor_indices").isEmpty(), "ground truth for the full base is not ground truth for 2 records");
        VectorDataException lacks = assertThrows(VectorDataException.class, small::neighborIndices);
        assertTrue(lacks.getMessage().contains("neighbor_indices"), lacks.getMessage());
    }

    @Test void aNamedParentSharesItsOutputsAcrossAnotherAxis() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
                neighbor_indices: gt_default.ivecs
              2r:
                base_count: 2
                neighbor_indices: gt_small.ivecs
              2r_sel:
                inherits: 2r
                attributes: { selectivity: 0.5 }
            """);
        TestDataView selective = view(dir, "2r_sel");
        assertArrayEquals(new int[] {3, 4}, selective.neighborIndices().get(0), "ground truth is invariant across a selectivity family");
        assertEquals(2, selective.baseVectors().count(), "the base arrives already windowed by the parent");
        assertArrayEquals(new float[] {9f, 8f}, selective.queryVectors().get(0), "and default's facets come through the parent");
    }

    @Test void aPartitionProfileInheritsNothing() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
              part:
                partition: true
                base_vectors: part.fvec
            """);
        TestDataView part = view(dir, "part");
        assertEquals(1, part.baseVectors().count(), "an oracle partition has independent base vectors");
        assertTrue(part.facet("query_vectors").isEmpty(), "and is not a windowed subset of default");
    }

    @Test void anAlreadyWindowedBaseIsNotReWindowed() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec[0..3]
              small:
                base_count: 2
            """);
        assertEquals(3, view(dir, "small").baseVectors().count(), "a window the parent carries is kept as is");
    }

    @Test void withoutABaseCountTheBaseInheritsAsIs() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
              variant:
                attributes: { flavour: plain }
            """);
        assertEquals(4, view(dir, "variant").baseVectors().count());
    }

    @Test void theOlderExtendsKeyStillNamesAParent() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
              other:
                base_count: 2
                neighbor_indices: gt_small.ivecs
              child:
                extends: other
            """);
        assertArrayEquals(new int[] {3, 4}, view(dir, "child").neighborIndices().get(0));
    }

    @Test void anUnknownOrSelfParentFallsBackToDefault() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
              typo:
                inherits: nope
              selfish:
                inherits: selfish
            """);
        assertArrayEquals(new float[] {9f, 8f}, view(dir, "typo").queryVectors().get(0));
        assertArrayEquals(new float[] {9f, 8f}, view(dir, "selfish").queryVectors().get(0));
    }

    @Test void aCycleLeavesItsMembersWithWhatTheyDeclare() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
              a:
                inherits: b
                base_vectors: part.fvec
              b:
                inherits: a
                query_vectors: query.fvec
              c:
                inherits: a
            """);
        assertTrue(view(dir, "a").facet("query_vectors").isEmpty(), "a cycle never settles, so nothing is inherited through it");
        assertTrue(view(dir, "b").facet("base_vectors").isEmpty());
        assertTrue(view(dir, "c").facet("base_vectors").isEmpty(), "nor by a profile whose parent is in one");
    }
}
