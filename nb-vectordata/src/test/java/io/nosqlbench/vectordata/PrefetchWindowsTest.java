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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Prefetching a caller-named ordinal window against local storage.
/// Mirrors the `prefetch_windows` suite of `vectordata-rs`: a profile's
/// `window:` names a range someone wants repeatedly — a convenience,
/// not a fence — so a caller that knows it is about to read a range can
/// say so whether or not a profile was defined ahead of time.
@Tag("unit")
class PrefetchWindowsTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    /// A 100-record, dim-4 fvec: bytes per record = 4 + 4*4 = 20.
    private static final long BPR = 20;

    private TestDataView view() throws IOException { return view("default"); }

    private TestDataView view(String profile) throws IOException {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        float[][] values = new float[100][4];
        for (int record = 0; record < 100; record++) for (int dim = 0; dim < 4; dim++) values[record][dim] = record + dim;
        FixtureSupport.fvec(dataset, "base_vectors.fvec", values);
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: prefetch-test
            profiles:
              default:
                base_vectors: base_vectors.fvec
              windowed:
                base_vectors: base_vectors.fvec[10..20)
            """);
        return TestDataGroup.load(dataset.toUri(), settings()).profile(profile);
    }

    @Test void anArbitraryWindowResolvesWithoutAProfileDeclaringIt() throws Exception {
        PrefetchPlan plan = view().prefetchPlan("base_vectors", DSWindow.parse("10..20"));
        assertFalse(plan.degradesToFullDownload(), "a uniform-stride facet must be windowable on demand");
        assertEquals(List.of(ShardRange.whole(10 * BPR, 20 * BPR)), plan.byteRanges(), "records map to bytes at 4 + dim*elem_size");
    }

    @Test void aMultiIntervalWindowResolvesEveryInterval() throws Exception {
        PrefetchPlan plan = view().prefetchPlan("base_vectors", DSWindow.parse("[0..10, 50..60]"));
        assertEquals(List.of(ShardRange.whole(0, 10 * BPR), ShardRange.whole(50 * BPR, 60 * BPR)), plan.byteRanges(),
            "both intervals must survive; the reader's single-window limit is a reader limit, not a fetch limit");
    }

    @Test void thePlanSeparatesWhatWasAskedForFromWhatIsIssued() throws Exception {
        TestDataView view = view();
        PrefetchPlan touching = view.prefetchPlan("base_vectors", DSWindow.parse("[0..10, 10..20]"));
        assertEquals(2, touching.requestedRanges().size(), "two intervals were asked for");
        assertEquals(List.of(ShardRange.whole(0, 20 * BPR)), touching.byteRanges(), "and they merge into one request");
        assertEquals(1, touching.requests());
        PrefetchPlan apart = view.prefetchPlan("base_vectors", DSWindow.parse("[0..10, 80..90]"));
        assertEquals(2, apart.requestedRanges().size());
        assertEquals(List.of(ShardRange.whole(0, 10 * BPR), ShardRange.whole(80 * BPR, 90 * BPR)), apart.byteRanges(),
            "nothing bridges a gap this size on chunkless local storage");
        assertEquals(2, apart.requests());
    }

    @Test void overlappingIntervalsBecomeOneRequest() throws Exception {
        PrefetchPlan plan = view().prefetchPlan("base_vectors", DSWindow.parse("[0..30, 20..40]"));
        assertEquals(1, plan.requests());
        assertEquals(List.of(ShardRange.whole(0, 40 * BPR)), plan.byteRanges());
    }

    @Test void anEmptyWindowCoversTheWholeFacet() throws Exception {
        PrefetchPlan plan = view().prefetchPlan("base_vectors", DSWindow.ALL);
        assertEquals(List.of(ShardRange.whole(0, 100 * BPR)), plan.byteRanges());
        assertFalse(plan.degradesToFullDownload(), "asking for everything is a request, not a fallback");
    }

    @Test void aLocalFacetCostsNothingToPrefetch() throws Exception {
        TestDataView view = view();
        PrefetchPlan plan = view.prefetchPlan("base_vectors", DSWindow.parse("10..20"));
        assertTrue(plan.fills().isEmpty(), "local storage reports no chunk fill");
        assertEquals(0, plan.bytesToFetch());
        assertEquals(0, plan.chunksToFetch());
        assertTrue(plan.isResident());
        PrefetchReport report = view.prefetch("base_vectors", DSWindow.parse("10..20"), WholeFacetFallback.REFUSE);
        assertEquals(1, report.rangesFetched());
    }

    @Test void aWindowPastTheEndClampsToTheFacet() throws Exception {
        PrefetchPlan plan = view().prefetchPlan("base_vectors", DSWindow.parse("90..500"));
        assertEquals(List.of(ShardRange.whole(90 * BPR, 100 * BPR)), plan.byteRanges());
    }

    /// Ragged variable-length records with a published sentinel-form
    /// sidecar: an ordinal becomes a byte offset only through the
    /// index, and once loaded the mapping is exact.
    private record VvecFixture(TestDataView view, long[] offsets, long fileLength) { }

    private VvecFixture vvecView(int[] dims) throws IOException {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        float[][] base = new float[10][4];
        FixtureSupport.fvec(dataset, "base_vectors.fvec", base);
        int[][] records = new int[dims.length][];
        long[] offsets = new long[dims.length];
        long at = 0;
        for (int record = 0; record < dims.length; record++) {
            records[record] = new int[dims[record]];
            for (int element = 0; element < dims[record]; element++) records[record][element] = element;
            offsets[record] = at;
            at += 4 + dims[record] * 4L;
        }
        Path data = FixtureSupport.vvec(dataset, "meta.ivvec", records);
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: prefetch-vvec
            profiles:
              default:
                base_vectors: base_vectors.fvec
                metadata_content: meta.ivvec
            """);
        return new VvecFixture(TestDataGroup.load(dataset.toUri(), settings()).profile("default"), offsets, Files.size(data));
    }

    @Test void aVvecWindowResolvesThroughItsOffsetIndex() throws Exception {
        VvecFixture fixture = vvecView(new int[] {1, 7, 3, 9, 2, 5, 4, 8});
        PrefetchPlan plan = fixture.view().prefetchPlan("metadata_content", DSWindow.parse("2..5"));
        assertFalse(plan.degradesToFullDownload(), "an indexed vvec facet is windowable");
        assertEquals(List.of(ShardRange.whole(fixture.offsets()[2], fixture.offsets()[5])), plan.byteRanges(),
            "records 2..5 are exactly offsets[2]..offsets[5]");
    }

    @Test void aVvecPlanReportsTheIndexItHadToRead() throws Exception {
        VvecFixture fixture = vvecView(new int[] {1, 7, 3, 9, 2, 5, 4, 8});
        PrefetchPlan vvecPlan = fixture.view().prefetchPlan("metadata_content", DSWindow.parse("2..5"));
        // Record starts × 8: the sentinel entry is layout, not a record,
        // so both sidecar layouts report the same figure.
        assertEquals(8 * 8L, vvecPlan.prerequisiteBytes(), "the loaded index is real work and the plan says so");
        PrefetchPlan xvecPlan = fixture.view().prefetchPlan("base_vectors", DSWindow.parse("2..5"));
        assertEquals(0, xvecPlan.prerequisiteBytes(), "a uniform stride comes from a header read every reader pays anyway");
    }

    @Test void aVvecWindowPastTheEndEndsAtTheFile() throws Exception {
        VvecFixture fixture = vvecView(new int[] {1, 7, 3, 9});
        PrefetchPlan plan = fixture.view().prefetchPlan("metadata_content", DSWindow.parse("2..99"));
        assertEquals(List.of(ShardRange.whole(fixture.offsets()[2], fixture.fileLength())), plan.byteRanges());
    }

    /// A local vvec facet that published no sidecar is walked in place,
    /// as in Rust — the dimension headers are the index.
    @Test void aLocalVvecWithoutASidecarIsWalked() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dataset, "base_vectors.fvec", new float[10][4]);
        int[] dims = {3, 1, 8, 2, 6};
        long[] offsets = new long[dims.length];
        int size = 0;
        for (int record = 0; record < dims.length; record++) { offsets[record] = size; size += 4 + dims[record] * 4; }
        ByteBuffer ragged = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        for (int dim : dims) { ragged.putInt(dim); for (int element = 0; element < dim; element++) ragged.putInt(element); }
        Files.write(dataset.resolve("ragged.ivvec"), ragged.array());
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: prefetch-walk
            profiles:
              default:
                base_vectors: base_vectors.fvec
                metadata_content: ragged.ivvec
            """);
        TestDataView view = TestDataGroup.load(dataset.toUri(), settings()).profile("default");
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.parse("1..4"));
        assertFalse(plan.degradesToFullDownload());
        assertEquals(List.of(ShardRange.whole(offsets[1], offsets[4])), plan.byteRanges());
        // The walk persists its result beside the data — starts only,
        // in the width the payload size calls for — so it is paid once
        // rather than per view.
        Path persisted = dataset.resolve("IDXFOR__ragged.ivvec.i32");
        assertTrue(Files.isRegularFile(persisted), "the rebuilt index is persisted beside the data");
        assertEquals(dims.length * 4L, Files.size(persisted), "record starts only, no sentinel");
        PrefetchPlan again = TestDataGroup.load(dataset.toUri(), settings()).profile("default")
            .prefetchPlan("metadata_content", DSWindow.parse("1..4"));
        assertEquals(plan.byteRanges(), again.byteRanges(), "a later view reads the persisted index");
    }

    /// Every scalar width maps at its own stride — raw packed values
    /// with no header of any kind, so nothing has to be read to know
    /// the mapping.
    @Test void everyScalarWidthMapsAtItsOwnStride() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dataset, "base_vectors.fvec", new float[10][4]);
        String[][] scalars = {{"u8", "1"}, {"i16", "2"}, {"u32", "4"}, {"i64", "8"}};
        StringBuilder yaml = new StringBuilder("name: scalar-widths\nprofiles:\n  default:\n    base_vectors: base_vectors.fvec\n");
        for (String[] scalar : scalars) {
            Files.write(dataset.resolve("values." + scalar[0]), new byte[16 * Integer.parseInt(scalar[1])]);
            yaml.append("    facet_").append(scalar[0]).append(": values.").append(scalar[0]).append("\n");
        }
        Files.writeString(dataset.resolve("dataset.yaml"), yaml.toString());
        TestDataView view = TestDataGroup.load(dataset.toUri(), settings()).profile("default");
        for (String[] scalar : scalars) {
            int width = Integer.parseInt(scalar[1]);
            PrefetchPlan plan = view.prefetchPlan("facet_" + scalar[0], DSWindow.parse("2..5"));
            assertFalse(plan.degradesToFullDownload(), scalar[0] + " is trivially windowable");
            assertEquals(List.of(ShardRange.whole(2L * width, 5L * width)), plan.byteRanges(), scalar[0]);
            assertEquals(0, plan.prerequisiteBytes(), "a fixed stride costs nothing to know");
        }
    }

    /// Raw packed scalars have the exact stride `ordinal × width` and
    /// no header — a deliberate clarification over the Rust source,
    /// which reads the first value as a phantom xvec dimension.
    @Test void aScalarWindowMapsExactly() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dataset, "base_vectors.fvec", new float[10][4]);
        // First value 7: the phantom-dim reading would yield 4 + 7*4 = 32
        // bytes per record and map records 2..5 to bytes 64..160.
        FixtureSupport.scalarI32(dataset, "labels.i32", 7, 1, 2, 3, 4, 5, 6, 8, 9, 10);
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: prefetch-scalar
            profiles:
              default:
                base_vectors: base_vectors.fvec
                metadata_results: labels.i32
            """);
        TestDataView view = TestDataGroup.load(dataset.toUri(), settings()).profile("default");
        PrefetchPlan plan = view.prefetchPlan("metadata_results", DSWindow.parse("2..5"));
        assertFalse(plan.degradesToFullDownload());
        assertEquals(List.of(ShardRange.whole(2 * 4, 5 * 4)), plan.byteRanges(), "scalars map at ordinal times width, no header");
    }

    private TestDataView parquetView() throws IOException {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dataset, "base_vectors.fvec", new float[10][4]);
        Files.write(dataset.resolve("m.parquet"), new byte[64]);
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: prefetch-degrade
            profiles:
              default:
                base_vectors: base_vectors.fvec
                metadata_content: m.parquet
            """);
        return TestDataGroup.load(dataset.toUri(), settings()).profile("default");
    }

    @Test void anUnmappableFormatReportsThatItDegrades() throws Exception {
        PrefetchPlan plan = parquetView().prefetchPlan("metadata_content", DSWindow.parse("2..4"));
        assertTrue(plan.degradesToFullDownload(), "parquet ordinal windowing is excluded by design and must say so");
        assertTrue(plan.byteRanges().isEmpty(), "a partial plan beside the degrade flag would understate the cost");
        assertEquals(plan.facetBytes(), plan.bytesToFetch(), "the honest cost of the degrade is the whole facet");
        assertEquals(64, plan.facetBytes());
    }

    @Test void anUnresolvableWindowIsRefusedUnlessAllowed() throws Exception {
        TestDataView view = parquetView();
        DSWindow window = DSWindow.parse("2..4");
        VectorDataException refused = assertThrows(VectorDataException.class,
            () -> view.prefetch("metadata_content", window, WholeFacetFallback.REFUSE));
        assertTrue(refused.getMessage().contains("whole facet"), refused.getMessage());
        assertTrue(refused.getMessage().contains("64"), "the message carries the size, because that is the decision being asked for");
        assertThrows(VectorDataException.class,
            () -> view.prefetchInBackground("metadata_content", window, WholeFacetFallback.REFUSE));
        PrefetchReport report = view.prefetch("metadata_content", window, WholeFacetFallback.ALLOW);
        assertEquals(1, report.rangesFetched());
        view.prefetchInBackground("metadata_content", window, WholeFacetFallback.ALLOW).join();
    }

    @Test void aWindowlessPrefetchOfAnUnmappableFacetNeedsNoPermission() throws Exception {
        TestDataView view = parquetView();
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.ALL);
        assertFalse(plan.degradesToFullDownload(), "asking for everything and getting everything is not a degrade");
        assertEquals(List.of(ShardRange.whole(0, 64)), plan.byteRanges());
        view.prefetch("metadata_content", DSWindow.ALL, WholeFacetFallback.REFUSE);
    }

    @Test void anAdhocWindowMatchesWhatTheProfileFormResolvesTo() throws Exception {
        PrefetchPlan adhoc = view("default").prefetchPlan("base_vectors", DSWindow.parse("10..20"));
        PrefetchPlan declared = view("windowed").prefetchPlan("base_vectors", DSWindow.parse("10..20"));
        assertEquals(adhoc.byteRanges(), declared.byteRanges(),
            "a window someone typed and a window someone declared are the same window");
        // And the suffix-declared window clips the reader the same way.
        VectorReader<?> reader = view("windowed").openFacet("base_vectors");
        assertEquals(10, reader.count(), "the [10..20) suffix clips the reader");
        assertEquals(10f, ((float[]) reader.get(0))[0], "windowed record 0 is underlying record 10");
    }

    @Test void aBackgroundPrefetchReportsItsPlanBeforeFinishing() throws Exception {
        PrefetchHandle handle = view().prefetchInBackground("base_vectors", DSWindow.parse("10..20"), WholeFacetFallback.REFUSE);
        assertEquals(List.of(ShardRange.whole(10 * BPR, 20 * BPR)), handle.plan().byteRanges(), "the plan is known before the fetch is");
        PrefetchReport report = handle.join();
        assertEquals(List.of(ShardRange.whole(10 * BPR, 20 * BPR)), report.planned().byteRanges());
        assertTrue(handle.isDone());
    }

    @Test void joiningABackgroundPrefetchWaitsForIt() throws Exception {
        PrefetchHandle handle = view().prefetchInBackground("base_vectors", DSWindow.parse("0..100"), WholeFacetFallback.REFUSE);
        assertEquals(1, handle.join().rangesFetched(), "one range, fetched");
    }

    @Test void cancellingStopsTheWorkerAndKeepsWhatItFetched() throws Exception {
        PrefetchHandle handle = view().prefetchInBackground("base_vectors", DSWindow.parse("0..100"), WholeFacetFallback.REFUSE);
        handle.cancel();
        assertTrue(handle.isCancelled());
        // Joining a cancelled prefetch is not an error: stopping early is
        // what was asked for.
        assertTrue(handle.join().rangesFetched() <= 1);
    }

    @Test void discardingTheHandleDetachesWithoutBlocking() throws Exception {
        TestDataView view = view();
        view.prefetchInBackground("base_vectors", DSWindow.parse("0..50"), WholeFacetFallback.REFUSE);
        // Never joined; the view is still usable and reads correctly.
        assertEquals(100, view.openFacet("base_vectors").count());
    }

    @Test void severalBackgroundPrefetchesRunConcurrently() throws Exception {
        TestDataView view = view();
        long[][] windows = {{0, 20}, {30, 50}, {60, 90}};
        PrefetchHandle[] handles = new PrefetchHandle[windows.length];
        for (int i = 0; i < windows.length; i++)
            handles[i] = view.prefetchInBackground("base_vectors", DSWindow.parse(windows[i][0] + ".." + windows[i][1]), WholeFacetFallback.REFUSE);
        for (PrefetchHandle handle : handles) assertEquals(1, handle.join().rangesFetched());
    }

    @Test void anUnknownFacetStopsThePlan() throws Exception {
        assertThrows(VectorDataException.class, () -> view().prefetchPlan("not_a_facet", DSWindow.ALL));
    }

    @Test void aMalformedWindowSuffixFailsNamingTheWindow() throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve("ds"));
        FixtureSupport.fvec(dataset, "base_vectors.fvec", new float[10][4]);
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: bad-suffix
            profiles:
              default:
                base_vectors: base_vectors.fvec[0,1000)
            """);
        VectorDataException malformed = assertThrows(VectorDataException.class,
            () -> TestDataGroup.load(dataset.toUri(), settings()).profile("default"));
        assertTrue(malformed.getMessage().contains("malformed window"), malformed.getMessage());
    }
}
