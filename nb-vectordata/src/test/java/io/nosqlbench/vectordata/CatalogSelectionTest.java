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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Selectors on the catalog surfaces: a single-profile open resolves
/// exactly one, the set surface returns every match size-ordered, a
/// spec splits by its head's shape, and a catalog entry resolves the
/// same selection before anything is fetched.
@Tag("unit")
class CatalogSelectionTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    private Catalog catalog() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("tess"));
        FixtureSupport.fvec(dataset, "base.fvec", new float[][] {{0f, 1f}, {1f, 2f}, {2f, 3f}, {3f, 4f}});
        FixtureSupport.fvec(dataset, "query.fvec", new float[][] {{9f, 8f}});
        FixtureSupport.ivec(dataset, "gt_2.ivecs", new int[][] {{1, 2}});
        Files.writeString(dataset.resolve("dataset.yaml"), """
            format_version: 3
            name: tess
            profile_tags:
              size: ~
              family: stratified
            profiles:
              default:
                maxk: 10
                base_vectors: base.fvec
                query_vectors: query.fvec
              2r:
                inherits: default
                base_count: 2
                neighbor_indices: gt_2.ivecs
                attributes: { size: 2r, family: stratified }
              2r-uniform:
                inherits: 2r
                attributes: { size: 2r, family: uniform, selectivity: 0.01 }
              3r:
                inherits: default
                base_count: 3
                attributes: { size: 3r }
            """);
        Files.writeString(temporary.resolve("catalog.yaml"), """
            datasets:
              - name: tess
                path: tess/dataset.yaml
                dataset_type: dataset.yaml
                layout:
                  format_version: 3
                  profile_tags: { size: ~, family: stratified }
                  profiles:
                    default: { maxk: 10, base_vectors: base.fvec, query_vectors: query.fvec }
                    2r: { inherits: default, base_count: 2, neighbor_indices: gt_2.ivecs, attributes: { size: 2r, family: stratified } }
                    2r-uniform: { inherits: 2r, attributes: { size: 2r, family: uniform, selectivity: 0.01 } }
                    3r: { inherits: default, base_count: 3, attributes: { size: 3r } }
              - name: future
                path: nowhere/dataset.yaml
                layout: { format_version: 99 }
            """);
        return Catalog.of(CatalogSources.of(temporary.resolve("catalog.yaml").toUri()), settings());
    }

    @Test void aSingleOpenResolvesExactlyOneProfile() throws Exception {
        Catalog catalog = catalog();
        assertEquals("default", catalog.open("tess", null).profile(), "no selector is default");
        assertEquals("2r", catalog.open("tess", "2R").profile(), "a bare name as it always was, case folded");
        assertEquals("2r-uniform", catalog.open("tess", "family=uniform").profile(), "an expression that names one");
        SelectionException ambiguous = assertThrows(SelectionException.class, () -> catalog.open("tess", "size=2r"));
        assertEquals(SelectionException.Kind.AMBIGUOUS, ambiguous.kind());
        assertTrue(ambiguous.getMessage().startsWith("dataset 'tess': "), ambiguous.getMessage());
        assertEquals(List.of("2r", "2r-uniform"), ambiguous.matches());
        SelectionException none = assertThrows(SelectionException.class, () -> catalog.open("tess", "size=9r"));
        assertEquals(SelectionException.Kind.NO_MATCH, none.kind());
        assertTrue(none.getMessage().contains("3r (base_count=3, maxk=10, inherits=default, size=3r)"), "the offer is listed as loaded: " + none.getMessage());
    }

    @Test void theSetSurfaceReturnsEveryMatchSizeOrdered() throws Exception {
        Catalog catalog = catalog();
        List<TestDataView> all = catalog.openProfiles("tess", "profile=*");
        assertEquals(List.of("default", "2r", "2r-uniform", "3r"), all.stream().map(TestDataView::profile).toList());
        assertEquals(List.of("2r", "2r-uniform"), catalog.openProfiles("tess", "base_count=2").stream().map(TestDataView::profile).toList(),
            "an inherited base_count is as selectable as a declared one");
        assertEquals(List.of("default"), catalog.openProfiles("tess", null).stream().map(TestDataView::profile).toList());
        assertEquals(2, catalog.openProfiles("tess", "2r-uniform").get(0).baseVectors().count(), "each view is the profile as loaded");
    }

    @Test void aSpecSplitsByItsHeadsShape() throws Exception {
        Catalog catalog = catalog();
        assertEquals("2r", catalog.openProfile("tess:2r").profile());
        assertEquals("default", catalog.openProfile("tess").profile());
        assertEquals("2r-uniform", catalog.openProfile("tess:profile=^2r-.*$").profile(), "a selector may carry what a last-colon split would have cut");
        SelectionException bad = assertThrows(SelectionException.class, () -> catalog.openProfile("tess:size>abc"));
        assertEquals(SelectionException.Kind.SYNTAX, bad.kind());
        assertTrue(bad.position() >= "tess:".length(), "the position is into the spec: " + bad.position());
    }

    @Test void aCatalogEntrySelectsBeforeFetching() throws Exception {
        Catalog catalog = catalog();
        CatalogEntry entry = catalog.entries().get("tess");
        assertEquals(3, entry.formatVersion());
        assertEquals(List.of("size", "family"), List.copyOf(entry.profileTags().keySet()));
        assertEquals(List.of("default", "2r", "2r-uniform", "3r"), entry.profileNames());
        assertEquals(List.of("2r", "2r-uniform"), entry.select("base_count=2"), "the declared form reads structure through the chain");
        assertEquals("2r-uniform", entry.selectOne("selectivity=1e-2"));
        assertEquals(List.of("default"), entry.select(null));
        ProfileFacts uniform = entry.profileFacts().stream().filter(f -> f.name().equals("2r-uniform")).findFirst().orElseThrow();
        assertEquals(2L, uniform.baseCount());
        assertEquals(10, uniform.maxk(), "maxk crosses every step");
        assertEquals(entry.select("family=uniform"), catalog.openGroup("tess").select("family=uniform"), "the entry and the loaded dataset agree");
        assertEquals(99, catalog.entries().get("future").formatVersion(), "a consumer can refuse a listed dataset before fetching it");
        assertEquals(1, new CatalogEntry("bare", temporary.toUri(), null, java.util.Map.of()).formatVersion(), "absent means 1");
    }
}
