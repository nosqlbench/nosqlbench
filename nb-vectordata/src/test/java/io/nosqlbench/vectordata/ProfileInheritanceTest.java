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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/// What a profile inherits depends on the axis it varies along, and the
/// axis is derived from `base_count`: a step whose count differs from
/// its parent's is a size step, whatever the parent is called. Across
/// the size axis the base facets are re-cut to the child's count and
/// the per-size outputs — ground truth and the results index — do not
/// cross, so a sized profile that omits its own fails loudly instead of
/// reading the full base's. Across a step at one size every facet is
/// invariant and inherits as is. From `format_version` 3 every parent
/// is stated and real, or the load is refused naming the profiles.
@Tag("unit")
class ProfileInheritanceTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    private Path dataset(String header, String profiles) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f}, {1f, 2f}, {2f, 3f}, {3f, 4f}});
        FixtureSupport.fvec(dir, "part.fvec", new float[][] {{7f, 7f}});
        FixtureSupport.fvec(dir, "query.fvec", new float[][] {{9f, 8f}});
        FixtureSupport.ivec(dir, "gt_default.ivecs", new int[][] {{1, 2}});
        FixtureSupport.ivec(dir, "gt_small.ivecs", new int[][] {{3, 4}});
        FixtureSupport.ivec(dir, "results_default.ivecs", new int[][] {{0, 1, 2, 3}});
        FixtureSupport.ivec(dir, "results_small.ivecs", new int[][] {{0, 1}});
        FixtureSupport.ivec(dir, "predicates.ivecs", new int[][] {{5}});
        Files.writeString(dir.resolve("dataset.yaml"), header + "name: axes\nprofiles:\n" + profiles);
        return dir;
    }

    private Path dataset(String profiles) throws IOException { return dataset("", profiles); }
    private Path layered(String profiles) throws IOException { return dataset("format_version: 3\n", profiles); }

    private TestDataGroup group(Path dir) { return TestDataGroup.load(dir.toUri(), settings()); }
    private TestDataView view(Path dir, String profile) { return group(dir).profile(profile); }
    private VectorDataException refused(Path dir) { return assertThrows(VectorDataException.class, () -> group(dir)); }

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
        Path dir = layered("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
                neighbor_indices: gt_default.ivecs
              2r:
                inherits: default
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

    /// The axis of a step is derived from `base_count`: a `3r` that
    /// names `2r` re-cuts the window to three and takes no ground
    /// truth, exactly as it would under `default`.
    @Test void theAxisIsDerivedFromBaseCount() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
                neighbor_indices: gt_default.ivecs
              2r:
                inherits: default
                base_count: 2
                neighbor_indices: gt_small.ivecs
              3r:
                inherits: 2r
                base_count: 3
            """);
        TestDataView bigger = view(dir, "3r");
        assertEquals(3, bigger.baseVectors().count(), "the window is re-cut to the child's count, not the parent's");
        assertEquals("0..3", bigger.facet("base_vectors").orElseThrow().window());
        assertArrayEquals(new float[] {9f, 8f}, bigger.queryVectors().get(0));
        assertTrue(bigger.facet("neighbor_indices").isEmpty(), "ground truth does not cross a size step, whatever the parent is called");
    }

    /// Layers stack: `default` → `2r` → a set at the same count inherits
    /// the layer's windows and unfiltered ground truth unchanged and
    /// declares its predicate group; the invariant predicate facet
    /// reaches the set through the layer.
    @Test void stackedLayersDeriveEachAxisFromBaseCount() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
                query_vectors: query.fvec
                metadata_predicates: predicates.ivecs
                neighbor_indices: gt_default.ivecs
              2r:
                inherits: default
                base_count: 2
                neighbor_indices: gt_small.ivecs
              2r-mixed:
                inherits: 2r
                base_count: 2
                metadata_results: results_small.ivecs
              2r-uniform:
                inherits: 2r
                metadata_predicates: predicates.ivecs
                metadata_results: results_small.ivecs
            """);
        TestDataView set = view(dir, "2r-mixed");
        assertEquals(2, set.baseVectors().count());
        assertArrayEquals(new int[] {3, 4}, set.neighborIndices().get(0), "the unfiltered ground truth crosses a same-size step");
        assertTrue(set.facet("metadata_predicates").isPresent(), "the invariant facet reaches the set through the layer");
        assertArrayEquals(new int[] {0, 1}, set.metadataResults().get(0));
        TestDataView uniform = view(dir, "2r-uniform");
        assertArrayEquals(new int[] {3, 4}, uniform.neighborIndices().get(0));
        assertTrue(view(dir, "2r").facet("metadata_results").isEmpty(), "a layer holds no predicate group of its own");
        List<ProfileFacts> facts = group(dir).profileFacts();
        ProfileFacts noCount = facts.stream().filter(f -> f.name().equals("2r-uniform")).findFirst().orElseThrow();
        assertEquals(2L, noCount.baseCount(), "a set without a count is at its layer's");
    }

    /// A results index is derived from `base_count` like the ground
    /// truth: across a size step it is declared or absent, never the
    /// parent's; across a step at one size it inherits like any
    /// invariant facet.
    @Test void metadataResultsDoesNotCrossTheSizeAxis() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
                metadata_predicates: predicates.ivecs
                metadata_results: results_default.ivecs
              2r:
                inherits: default
                base_count: 2
                neighbor_indices: gt_small.ivecs
              2r-sel:
                inherits: 2r
                metadata_results: results_small.ivecs
              2r-again:
                inherits: 2r-sel
            """);
        assertTrue(view(dir, "2r").facet("metadata_results").isEmpty(), "a sized child must not take default's results index at another size");
        assertTrue(view(dir, "2r").facet("metadata_predicates").isPresent(), "the predicate facet is the same at every size and crosses");
        assertArrayEquals(new int[] {0, 1}, view(dir, "2r-again").metadataResults().get(0), "across a step at one size the results index inherits");
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

    @Test void aWindowTheParentCarriesIsReCutToTheChildsCount() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec[0..3]
              small:
                base_count: 2
            """);
        assertEquals(2, view(dir, "small").baseVectors().count(), "the child reads the first two of the same file, not its parent's three");
    }

    @Test void withoutABaseCountEveryFacetInheritsAsIs() throws Exception {
        Path dir = dataset("""
              default:
                base_vectors: base.fvec
                neighbor_indices: gt_default.ivecs
              variant:
                attributes: { flavour: plain }
            """);
        TestDataView variant = view(dir, "variant");
        assertEquals(4, variant.baseVectors().count());
        assertArrayEquals(new int[] {1, 2}, variant.neighborIndices().get(0), "a step at the parent's size is not a size step");
    }

    @Test void theOlderExtendsKeyStillNamesAParent() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
              other:
                inherits: default
                base_count: 2
                neighbor_indices: gt_small.ivecs
              child:
                extends: other
            """);
        assertArrayEquals(new int[] {3, 4}, view(dir, "child").neighborIndices().get(0));
    }

    @Test void aNumericRungNamesTheProfileItSpells() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
              100:
                inherits: default
                base_count: 2
                neighbor_indices: gt_small.ivecs
              child:
                inherits: 100
            """);
        assertArrayEquals(new int[] {3, 4}, view(dir, "child").neighborIndices().get(0), "a parent YAML read as a number is the name it spells");
        assertEquals(List.of("default", "100", "child"), group(dir).profileNames());
    }

    // -- format_version 3: stated parents --

    @Test void versionThreeRefusesAnUnstatedParent() throws Exception {
        VectorDataException e = refused(layered("""
              default:
                base_vectors: base.fvec
              small:
                base_count: 2
            """));
        assertTrue(e.getMessage().contains("`small` names no parent") && e.getMessage().contains("inherits: default"), e.getMessage());
    }

    @Test void versionThreeRefusesAnUnknownOrSelfParent() throws Exception {
        VectorDataException unknown = refused(layered("""
              default:
                base_vectors: base.fvec
              child:
                inherits: nonexistent
                query_vectors: query.fvec
            """));
        assertTrue(unknown.getMessage().contains("`child`") && unknown.getMessage().contains("unknown parent `nonexistent`"), unknown.getMessage());
        VectorDataException selfish = refused(layered("""
              default:
                base_vectors: base.fvec
              a:
                inherits: a
            """));
        assertTrue(selfish.getMessage().contains("`a` names itself"), selfish.getMessage());
    }

    @Test void versionThreeRefusesACycleNamingItsMembers() throws Exception {
        VectorDataException e = refused(layered("""
              default:
                base_vectors: base.fvec
              a:
                inherits: b
                base_vectors: part.fvec
              b:
                inherits: a
                query_vectors: query.fvec
              c:
                inherits: a
            """));
        assertTrue(e.getMessage().contains("`a`, `b` close an inheritance cycle"), e.getMessage());
        assertEquals(1, e.getMessage().split("cycle", -1).length - 1, "one cycle, reported once: " + e.getMessage());
    }

    @Test void versionThreeRefusesAPartitionThatNamesAParentAndAcceptsOneAlone() throws Exception {
        VectorDataException both = refused(layered("""
              default:
                base_vectors: base.fvec
              p:
                partition: true
                inherits: default
                base_vectors: part.fvec
            """));
        assertTrue(both.getMessage().contains("both `partition: true` and `inherits:`"), both.getMessage());
        Path alone = layered("""
              default:
                base_vectors: base.fvec
              p:
                partition: true
                base_vectors: part.fvec
            """);
        assertEquals(1, view(alone, "p").baseVectors().count());
    }

    @Test void aNamedParentIsHeldToVersionOneWhenUnversioned() throws Exception {
        VectorDataException e = refused(dataset("""
              default:
                base_vectors: base.fvec
              2r:
                base_count: 2
              2r_sel:
                inherits: 2r
                query_vectors: query.fvec
            """));
        assertTrue(e.getMessage().contains("no format_version") && e.getMessage().contains("format_version: 3"), e.getMessage());
        VectorDataException understated = refused(dataset("format_version: 2\n", """
              default:
                base_vectors: base.fvec
              2r:
                base_count: 2
              2r_sel:
                inherits: 2r
            """));
        assertTrue(understated.getMessage().contains("understate"), understated.getMessage());
        assertNotNull(group(dataset("format_version: 2\n", """
              default:
                base_vectors: base.fvec
              2r:
                inherits: default
                base_count: 2
            """)), "a parent of default needs nothing above 1");
    }

    // -- format_version 3: the tag schema --

    @Test void aTagSchemaLoadsInNamingOrderAndNeedsVersionThree() throws Exception {
        Path dir = dataset("format_version: 3\nprofile_tags:\n  size: ~\n  predicates: ~\n  selectivity: ~\n  family: stratified\n", """
              default:
                base_vectors: base.fvec
            """);
        Map<String, Object> tags = group(dir).profileTags();
        assertEquals(List.of("size", "predicates", "selectivity", "family"), List.copyOf(tags.keySet()), "the schema loads in naming order");
        assertTrue(tags.containsKey("size") && tags.get("size") == null, "a naming tag carries no default");
        assertEquals("stratified", tags.get("family"));
        assertEquals(3, group(dir).formatVersion());
        VectorDataException unversioned = refused(dataset("profile_tags:\n  size: ~\n", "  default:\n    base_vectors: base.fvec\n"));
        assertTrue(unversioned.getMessage().contains("format_version: 3"), "a schema alone needs version 3: " + unversioned.getMessage());
        assertTrue(refused(dataset("format_version: 2\nprofile_tags:\n  size: ~\n", "  default:\n    base_vectors: base.fvec\n")).getMessage().contains("understate"));
        assertTrue(group(dataset("  default:\n    base_vectors: base.fvec\n")).profileTags().isEmpty(), "a dataset without a schema declares none");
    }

    // -- what a selector reads --

    /// Attributes never inherit: a child of a named parent selects on
    /// its own attributes and on the structure it inherits.
    @Test void attributesNeverInheritButStructureDoes() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
                maxk: 100
              1m:
                inherits: default
                base_count: 2
                attributes:
                  family: sized
              1m-sel:
                inherits: 1m
                query_vectors: query.fvec
                attributes:
                  selectivity: 0.01
            """);
        TestDataGroup group = group(dir);
        ProfileFacts child = group.profileFacts().stream().filter(f -> f.name().equals("1m-sel")).findFirst().orElseThrow();
        assertEquals(2L, child.baseCount(), "structure is read after inheritance");
        assertEquals(100, child.maxk());
        assertEquals("1m", child.inherits());
        assertEquals(Map.of("selectivity", 0.01), child.attributes(), "a parent's attributes are not the child's");
        assertTrue(ProfileSelector.parse("family=sized").matches(group.profileFacts().stream().filter(f -> f.name().equals("1m")).findFirst().orElseThrow()));
        assertFalse(ProfileSelector.parse("family=sized").matches(child));
        assertTrue(ProfileSelector.parse("base_count=2,selectivity=1e-2").matches(child));
        assertEquals(List.of("1m-sel"), group.select("selectivity=1e-2"));
        assertEquals("1m", group.selectOne("1M"));
        assertEquals(List.of("default", "1m", "1m-sel"), group.select("profile=*"));
        assertEquals(List.of("default"), group.select(null));
    }

    @Test void profileNamesAreSizeOrdered() throws Exception {
        Path dir = layered("""
              default:
                base_vectors: base.fvec
              label_10:
                partition: true
                base_vectors: part.fvec
              label_2:
                partition: true
                base_vectors: part.fvec
              3r:
                inherits: default
                base_count: 3
              2r:
                inherits: default
                base_count: 2
              10m:
                inherits: default
            """);
        assertEquals(List.of("default", "2r", "3r", "10m", "label_2", "label_10"), group(dir).profileNames(),
            "default first, then by base_count or the name read as a count, then naturally by name");
    }
}
