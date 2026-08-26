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

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CatalogManifestTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }
    @Test void resolvesCatalogProfilesInheritanceAliasesAndCustomFacets() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("sample"));
        FixtureSupport.fvec(dataset, "base.fvec", new float[][] {{1f, 2f}});
        FixtureSupport.fvec(dataset, "query.fvec", new float[][] {{3f, 4f}});
        FixtureSupport.ivec(dataset, "neighbors.ivecs", new int[][] {{9, 10}});
        FixtureSupport.ivec(dataset, "metadata.ivecs", new int[][] {{11, 12}});
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: sample
            profiles:
              parent:
                base: base.fvec
                custom_scores: {source: metadata.ivecs}
              demo:
                extends: parent
                query: {source: query.fvec, window: 0..1}
                neighbor_indices: neighbors.ivecs
                metadata_indices: metadata.ivecs
            """);
        Files.writeString(temporary.resolve("catalog.yaml"), """
            datasets:
              - name: sample
                path: sample/dataset.yaml
                dataset_type: dataset.yaml
            """);
        Catalog catalog = Catalog.of(CatalogSources.of(temporary.resolve("catalog.yaml").toUri()), settings());
        TestDataView view = catalog.open("sample", "demo");
        assertArrayEquals(new float[] {1f, 2f}, view.baseVectors().get(0));
        assertArrayEquals(new float[] {3f, 4f}, view.queryVectors().get(0));
        assertArrayEquals(new int[] {9, 10}, view.neighborIndices().get(0));
        assertArrayEquals(new int[] {11, 12}, view.metadataResults().get(0));
        assertTrue(view.facet("custom_scores").isPresent());
        assertEquals(1, view.queryVectors().count());
    }
    @Test void canonicalFacetKeysResolveThroughStandardAccessors() throws Exception {
        // Rust-published dataset.yaml files declare the canonical keys
        // (base_vectors, query_vectors, ...); the alias forms (base,
        // query, gt, ...) are shorthand. Both must reach the standard
        // accessors — a released dataset failed here when baseVectors()
        // looked up only the "base" alias.
        Path dataset = Files.createDirectories(temporary.resolve("canonical"));
        FixtureSupport.fvec(dataset, "base.fvec", new float[][] {{1f, 2f}});
        FixtureSupport.fvec(dataset, "query.fvec", new float[][] {{3f, 4f}});
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: canonical
            profiles:
              "100k":
                base_vectors: base.fvec
                query_vectors: query.fvec
            """);
        TestDataView view = TestDataGroup.load(dataset.toUri(), settings()).profile("100k");
        assertArrayEquals(new float[] {1f, 2f}, view.baseVectors().get(0));
        assertArrayEquals(new float[] {3f, 4f}, view.queryVectors().get(0));
        assertTrue(view.facets().containsKey("base_vectors"), "the facet map is keyed canonically");
        assertTrue(view.facet("base").isPresent(), "shorthand aliases resolve on lookup");
        assertTrue(view.facet("train").isPresent(), "every Rust alias resolves");
    }
    @Test void opensLegacyKnnEntriesProfilesWithoutDatasetManifest() throws Exception {
        FixtureSupport.fvec(temporary, "base.fvec", new float[][] {{1f, 2f}});
        FixtureSupport.fvec(temporary, "query.fvec", new float[][] {{3f, 4f}});
        FixtureSupport.ivec(temporary, "gt.ivecs", new int[][] {{7, 8}});
        Path catalogFile = temporary.resolve("knn_entries.yaml");
        Files.writeString(catalogFile, """
            "legacy:demo":
              base: base.fvec
              query: query.fvec
              gt: gt.ivecs
            """);
        TestDataView view = Catalog.of(CatalogSources.of(catalogFile.toUri()), settings()).open("legacy", "demo");
        assertArrayEquals(new float[] {1f, 2f}, view.baseVectors().get(0));
        assertArrayEquals(new float[] {3f, 4f}, view.queryVectors().get(0));
        assertArrayEquals(new int[] {7, 8}, view.neighborIndices().get(0));
    }
    @Test void directArbitraryYamlDispatchesLegacyEntriesByShape() throws Exception {
        FixtureSupport.fvec(temporary, "base.fvec", new float[][] {{1f, 2f}});
        Path file = temporary.resolve("entries.yaml");
        Files.writeString(file, "\"shape:default\":\n  base: base.fvec\n");
        assertArrayEquals(new float[] {1f, 2f}, TestDataGroup.load(file.toUri(), settings()).profile("default").baseVectors().get(0));
    }
    @Test void directoryPrefersDatasetYamlThenFallsBackToKnnEntries() throws Exception {
        Path preferred = Files.createDirectories(temporary.resolve("preferred"));
        FixtureSupport.fvec(preferred, "base.fvec", new float[][] {{1f, 2f}});
        Files.writeString(preferred.resolve("dataset.yaml"), "profiles:\n  default:\n    base: base.fvec\n");
        Files.writeString(preferred.resolve("knn_entries.yaml"), "\"ignored:default\":\n  base: missing.fvec\n");
        assertArrayEquals(new float[] {1f, 2f}, TestDataGroup.load(preferred.toUri(), settings()).profile("default").baseVectors().get(0));
        Path legacy = Files.createDirectories(temporary.resolve("legacy"));
        FixtureSupport.fvec(legacy, "base.fvec", new float[][] {{3f, 4f}});
        Files.writeString(legacy.resolve("knn_entries.yaml"), "\"legacy:default\":\n  base: base.fvec\n");
        assertArrayEquals(new float[] {3f, 4f}, TestDataGroup.load(legacy.toUri(), settings()).profile("default").baseVectors().get(0));
    }
    @Test void catalogDirectoryPrefersJsonOverYaml() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("json-dataset"));
        FixtureSupport.fvec(dataset, "base.fvec", new float[][] {{1f, 2f}}); Files.writeString(dataset.resolve("dataset.yaml"), "profiles:\n  default:\n    base: base.fvec\n");
        Files.writeString(temporary.resolve("catalog.json"), "[{\"name\":\"json\",\"path\":\"json-dataset/dataset.yaml\",\"dataset_type\":\"dataset.yaml\"}]");
        Files.writeString(temporary.resolve("catalog.yaml"), "- name: yaml\n  path: absent.yaml\n");
        assertArrayEquals(new float[] {1f, 2f}, Catalog.of(CatalogSources.of(temporary.toUri()), settings()).open("json", "default").baseVectors().get(0));
    }
}
