package io.nosqlbench.virtdata.lib.vectors.vectordata;

/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.VectorDataSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// Binding-function coverage for the vectordata mappers: canonical and
/// alias facet keys, caller-specified ordinal windows (clip + warm),
/// prefetch modes, and the generic Facet/VariableFacet access paths.
/// All warming here runs against local storage, where fetches are
/// no-ops; the remote fetch machinery is covered by the nb-vectordata
/// integration tests.
@Tag("unit")
public class VectorDataBindingsTest {

    @TempDir static Path temporary;
    static VectorDataSettings settings;
    static String priorCatalog;

    @BeforeAll
    static void publishFixture() throws IOException {
        Path dataset = Files.createDirectories(temporary.resolve("example"));
        float[][] base = new float[10][2];
        for (int record = 0; record < 10; record++) { base[record][0] = record; base[record][1] = record + 1; }
        fvec(dataset.resolve("base.fvec"), base);
        fvec(dataset.resolve("query.fvec"), new float[][] {{9f, 8f}, {7f, 6f}});
        fvec(dataset.resolve("dist.fvec"), new float[][] {{0.5f, 1.5f}});
        fvec(dataset.resolve("extra.fvec"), new float[][] {{42f, 43f}});
        ivec(dataset.resolve("gt.ivecs"), new int[][] {{3, 4}});
        ivec(dataset.resolve("results.ivecs"), new int[][] {{1, 0}, {0, 1}});
        scalarI32(dataset.resolve("labels.i32"), 7, 1, 2, 3);
        vvec(dataset, "meta.ivvec", new int[][] {{5}, {6, 7, 8}, {9, 10}, {11, 12, 13, 14}});
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: example
            profiles:
              demo:
                base_vectors: base.fvec
                query: query.fvec
                gt: gt.ivecs
                distances: dist.fvec
                metadata_results: results.ivecs
                metadata_content: meta.ivvec
                labels: labels.i32
                custom_floats: extra.fvec
              windowed:
                base_vectors: base.fvec[2..8)
            """);
        Files.writeString(temporary.resolve("catalog.yaml"), """
            datasets:
              - name: example
                path: example/dataset.yaml
            """);
        settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        priorCatalog = System.getProperty("vectordata.catalog");
        System.setProperty("vectordata.catalog", temporary.resolve("catalog.yaml").toString());
    }

    @AfterAll
    static void restoreCatalogProperty() {
        if (priorCatalog == null) System.clearProperty("vectordata.catalog");
        else System.setProperty("vectordata.catalog", priorCatalog);
    }

    @Test
    void canonicalDatasetKeysResolveThroughStandardMappers() {
        // The field failure: a Rust-published dataset.yaml declares
        // base_vectors, and the mapper used to find only the alias.
        BaseVectors vectors = new BaseVectors("example:demo", true, settings);
        assertArrayEquals(new float[] {1f, 2f}, vectors.apply(1));
    }

    @Test
    void aliasDatasetKeysResolveThroughStandardMappers() {
        assertArrayEquals(new float[] {7f, 6f}, new QueryVectors("example:demo", false, settings).apply(1));
        assertArrayEquals(new int[] {3, 4}, new NeighborIndices("example:demo", false, settings).apply(0));
        assertArrayEquals(new float[] {0.5f, 1.5f}, new NeighborDistances("example:demo", false, settings).apply(0));
        assertArrayEquals(new int[] {0, 1}, new MetadataResults("example:demo", true, settings).apply(1));
    }

    @Test
    void aWindowWarmsWithoutRenumbering() {
        // The window says which records to warm; it does not re-base
        // them. Index 1 is record 1, as it would be with no window.
        BaseVectors warmed = new BaseVectors("example:demo", "1..3", "eager", settings);
        assertArrayEquals(new float[] {1f, 2f}, warmed.apply(1), "index 1 is record 1, not the window's first record");
        assertArrayEquals(new float[] {2f, 3f}, warmed.apply(2));
    }

    @Test
    void aWindowFarFromZeroReadsTheRecordsItsCyclesName() {
        // The field failure: a window starting well above zero used to
        // re-base to 0, so a run over those same cycles indexed past the
        // end and threw.
        BaseVectors slice = new BaseVectors("example:demo", "[7..10)", "none", settings);
        assertArrayEquals(new float[] {7f, 8f}, slice.apply(7));
        assertArrayEquals(new float[] {9f, 10f}, slice.apply(9));
    }

    @Test
    void readingOutsideTheWindowStillWorks() {
        // Not an error, and load-bearing: VirtData probes a resolved
        // function with a small sample index before any cycle runs, so a
        // window starting above zero would otherwise fail to resolve.
        BaseVectors slice = new BaseVectors("example:demo", "[7..10)", "none", settings);
        assertArrayEquals(new float[] {1f, 2f}, slice.apply(1), "outside the warmed window, but readable");
    }

    @Test
    void backgroundModeExposesAJoinableHandle() {
        BaseVectors vectors = new BaseVectors("example:demo", "1..3", "background", settings);
        assertNotNull(vectors.backgroundPrefetch(), "background mode hands back the running prefetch");
        vectors.backgroundPrefetch().join();
        assertArrayEquals(new float[] {1f, 2f}, vectors.apply(1));
    }

    @Test
    void aBindingWindowComposesWithAProfileWindow() {
        // The profile clips to records [2..8), which re-bases the reader
        // it hands back; the binding window then restricts that reader's
        // indices 1..2, and warming shifts them to dataset records 3..5.
        BaseVectors vectors = new BaseVectors("example:windowed", "1..3", "eager", settings);
        assertArrayEquals(new float[] {3f, 4f}, vectors.apply(1));
        // And warming shifts the same window to absolute records 3..5.
        DSWindow effective = CoreVectors.effectiveWindow(vectors.tdv, "base_vectors", "1..3");
        assertEquals(new DSWindow.Interval(3, 5), effective.intervals().get(0));
    }

    @Test
    void theFacetMapperReadsCustomScalarAndAliasNamedFacets() {
        assertArrayEquals(new float[] {42f, 43f}, (float[]) new Facet("example:demo", "custom_floats", "", "none", settings).apply(0));
        assertEquals(7, ((Number) new Facet("example:demo", "labels", "", "none", settings).apply(0)).intValue());
        assertArrayEquals(new float[] {0f, 1f}, (float[]) new Facet("example:demo", "base", "", "none", settings).apply(0),
            "alias names resolve to the same facet");
    }

    @Test
    void theVariableFacetMapperWarmsAWindowAndReadsAbsoluteOrdinals() {
        VariableFacet facet = new VariableFacet("example:demo", "metadata_content", "1..3", "eager", settings);
        assertArrayEquals(new int[] {6, 7, 8}, (int[]) facet.apply(1), "indices remain absolute; the window only warms");
        assertArrayEquals(new int[] {5}, (int[]) facet.apply(0));
    }

    @Test
    void anUnknownPrefetchModeFailsWithTheAcceptedModes() {
        RuntimeException unknown = assertThrows(RuntimeException.class,
            () -> new BaseVectors("example:demo", "1..3", "sideways", settings));
        assertTrue(unknown.getMessage().contains("eager, background, or none"), unknown.getMessage());
    }

    private static void fvec(Path path, float[][] values) throws IOException {
        int dimension = values[0].length;
        ByteBuffer bytes = ByteBuffer.allocate(values.length * (4 + dimension * 4)).order(ByteOrder.LITTLE_ENDIAN);
        for (float[] value : values) { bytes.putInt(dimension); for (float element : value) bytes.putFloat(element); }
        Files.write(path, bytes.array());
    }

    private static void ivec(Path path, int[][] values) throws IOException {
        int dimension = values[0].length;
        ByteBuffer bytes = ByteBuffer.allocate(values.length * (4 + dimension * 4)).order(ByteOrder.LITTLE_ENDIAN);
        for (int[] value : values) { bytes.putInt(dimension); for (int element : value) bytes.putInt(element); }
        Files.write(path, bytes.array());
    }

    private static void scalarI32(Path path, int... values) throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(values.length * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int value : values) bytes.putInt(value);
        Files.write(path, bytes.array());
    }

    /// Writes ragged records plus the sentinel-form IDXFOR sidecar.
    private static void vvec(Path directory, String filename, int[][] values) throws IOException {
        int size = 0;
        for (int[] value : values) size += 4 + value.length * 4;
        ByteBuffer data = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer index = ByteBuffer.allocate((values.length + 1) * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int[] value : values) {
            index.putInt(data.position());
            data.putInt(value.length);
            for (int element : value) data.putInt(element);
        }
        index.putInt(data.position());
        Files.write(directory.resolve(filename), data.array());
        Files.write(directory.resolve("IDXFOR__" + filename + ".i32"), index.array());
    }
}
