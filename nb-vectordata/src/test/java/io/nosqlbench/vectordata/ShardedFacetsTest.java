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

import static org.junit.jupiter.api.Assertions.*;

/// A facet spread across several files reads as one dense ordinal
/// space, plans by the shards a window spans, and refuses a declaration
/// that disagrees with itself or with its files. Mirrors the
/// `sharded_facets` suite of `vectordata-rs`.
@Tag("unit")
class ShardedFacetsTest {
    @TempDir Path temporary;

    /// dim 4 → 4 + 4×4 bytes per record.
    private static final long BPR = 20;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    /// Records numbered from `first`: element `d` of record `r` is `r*100 + d`.
    private static void fvec(Path dir, String name, int records, int first) throws IOException {
        float[][] values = new float[records][4];
        for (int r = 0; r < records; r++) for (int d = 0; d < 4; d++) values[r][d] = (first + r) * 100f + d;
        FixtureSupport.fvec(dir, name, values);
    }

    private Path dataset(String name, String yaml) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve(name));
        Files.writeString(dir.resolve("dataset.yaml"), yaml);
        return dir;
    }

    /// Three shards of 100, 100, and 40 records.
    private Path uniformSeries(String extra) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve("uniform"));
        fvec(dir, "base__0000.fvec", 100, 0);
        fvec(dir, "base__0001.fvec", 100, 100);
        fvec(dir, "base__0002.fvec", 40, 200);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: sharded
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 100
                  shard_count: 3
                  record_count: 240
            """ + extra);
        return dir;
    }

    private TestDataView view(Path dir) { return view(dir, "default"); }
    private TestDataView view(Path dir, String profile) { return TestDataGroup.load(dir.toUri(), settings()).profile(profile); }

    @Test void aUniformSeriesOpensAndReportsTheWholeFacet() throws Exception {
        TestDataView view = view(uniformSeries(""));
        VectorReader<float[]> base = view.baseVectors();
        assertEquals(240, base.count(), "the count spans the series");
        assertEquals(4, base.dimension());
        assertEquals(0f, base.get(0)[0]);
        assertEquals(99 * 100f, base.get(99)[0], "the last record of shard 0");
        assertEquals(100 * 100f, base.get(100)[0], "the first record of shard 1");
        assertEquals(239 * 100f + 3, base.get(239)[3], "the last record of the last, short shard");
        assertThrows(IndexOutOfBoundsException.class, () -> base.get(240), "past the series end is out of bounds, never a clamp");
        PrefetchPlan whole = view.prefetchPlan("base_vectors", DSWindow.ALL);
        assertEquals(240 * BPR, whole.facetBytes(), "the facet is every shard's bytes, not the first shard's");
        assertTrue(whole.isResident());
    }

    @Test void aSeriesReadsIdenticallyToTheSingleFileItWasSplitFrom() throws Exception {
        TestDataView series = view(uniformSeries(""));
        Path single = Files.createDirectories(temporary.resolve("single"));
        fvec(single, "base.fvec", 240, 0);
        TestDataView whole = view(dataset("single", "name: whole\nprofiles:\n  default:\n    base_vectors: base.fvec\n"));
        VectorReader<float[]> a = series.baseVectors(), b = whole.baseVectors();
        assertEquals(b.count(), a.count());
        for (long i = 0; i < a.count(); i++) assertArrayEquals(b.get(i), a.get(i), "record " + i);
        for (long i = a.count() - 1; i >= 0; i -= 7) assertArrayEquals(b.get(i), a.get(i), "record " + i + ", read backwards");
    }

    @Test void aSeriesWhoseFilesContradictItsDeclarationIsRefused() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("bad"));
        fvec(dir, "base__0000.fvec", 100, 0);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: bad
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 100
                  shard_count: 3
                  record_count: 9999
            """);
        VectorDataException refused = assertThrows(VectorDataException.class, () -> TestDataGroup.load(dir.toUri(), settings()));
        assertTrue(refused.getMessage().contains("record_count"), "the message must name the disagreement: " + refused.getMessage());
    }

    @Test void anExplicitSeriesOpensFromNamedFiles() throws Exception {
        Path dir = uniformSeries("");
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: explicit
            profiles:
              default:
                base_vectors:
                  source:
                    - base__0000.fvec=100
                    - base__0001.fvec=100
                    - base__0002.fvec=40
                  record_count: 240
            """);
        VectorReader<float[]> base = view(dir).baseVectors();
        assertEquals(240, base.count());
        for (long i : new long[] {0, 99, 100, 199, 200, 239}) assertEquals(i * 100f, base.get(i)[0], "record " + i);
    }

    @Test void bareNamesResolveToTheSameFacetAsCountedOnes() throws Exception {
        Path dir = uniformSeries("");
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: bare
            profiles:
              default:
                base_vectors:
                  source: [ base__0000.fvec, base__0001.fvec, base__0002.fvec ]
                  record_count: 240
            """);
        VectorReader<float[]> base = view(dir).baseVectors();
        assertEquals(240, base.count(), "a local bare entry is measured by opening its file");
        assertEquals(239 * 100f, base.get(239)[0]);
    }

    @Test void twoShardsOfOneFileReadTheirOwnWindows() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("sliced"));
        int[] values = new int[10_000];
        for (int i = 0; i < values.length; i++) values[i] = i;
        FixtureSupport.scalarI32(dir, "corpus.u32", values);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: sliced
            profiles:
              default:
                metadata_layout:
                  source:
                    - corpus.u32[0..10]=10
                    - corpus.u32[9990..10000]=10
                  record_count: 20
            """);
        TestDataView view = view(dir);
        VectorReader<?> reader = view.openFacet("metadata_layout");
        assertEquals(20, reader.count(), "the global space is the entries' lengths laid end to end");
        assertEquals(0L, ((Number) reader.get(0)).longValue());
        assertEquals(9L, ((Number) reader.get(9)).longValue());
        assertEquals(9990L, ((Number) reader.get(10)).longValue(), "the second entry starts at its window's lower bound");
        assertEquals(9999L, ((Number) reader.get(19)).longValue());
        assertTrue(reader.isComplete(), "a local sliced facet is complete");
        view.prebuffer(PrebufferProgress.NONE);
        PrefetchPlan plan = view.prefetchPlan("metadata_layout", DSWindow.ALL);
        assertEquals(2, plan.byteRanges().size(), "two shards, one file: byte ranges are per shard");
        assertEquals(new ShardRange(0, 0, 10_000 * 4), plan.byteRanges().get(0), "a whole-facet plan names each shard by its file's extent");
        assertEquals(new ShardRange(1, 0, 10_000 * 4), plan.byteRanges().get(1));
        PrefetchPlan windowed = view.prefetchPlan("metadata_layout", DSWindow.parse("5..15"));
        assertEquals(List.of(new ShardRange(0, 5 * 4, 10 * 4), new ShardRange(1, 9990 * 4, 9995 * 4)), windowed.byteRanges(),
            "a window maps each shard's local ordinals through its entry's file base");
    }

    @Test void aMissingShardIsNamedRatherThanReadAsEmptiness() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("gap"));
        fvec(dir, "base__0000.fvec", 100, 0);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: gap
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 100
                  shard_count: 2
                  record_count: 150
            """);
        TestDataView view = view(dir);
        VectorDataException missing = assertThrows(VectorDataException.class, view::baseVectors,
            "a declared shard that is absent must be reported");
        assertTrue(missing.getMessage().contains("base__0001.fvec"), "the message must name the missing shard: " + missing.getMessage());
        assertEquals(0, view.prefetchPlan("base_vectors", DSWindow.ALL).facetBytes(),
            "a facet that cannot be sized answers 0 through the infallible accessor, never a shorter facet");
    }

    @Test void aProfileWindowClipsTheSeriesNotAShard() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("win"));
        fvec(dir, "base__0000.fvec", 100, 0);
        fvec(dir, "base__0001.fvec", 100, 100);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: win
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 100
                  shard_count: 2
                  record_count: 200
                  window: 50..150
            """);
        VectorReader<float[]> windowed = view(dir).baseVectors();
        assertEquals(100, windowed.count(), "the window spans a shard boundary");
        assertEquals(50 * 100f, windowed.get(0)[0]);
        assertEquals(99 * 100f, windowed.get(49)[0]);
        assertEquals(100 * 100f, windowed.get(50)[0]);
        assertEquals(149 * 100f, windowed.get(99)[0]);
        assertThrows(IndexOutOfBoundsException.class, () -> windowed.get(100));
    }

    /// A window suffix on a uniform pattern is the facet window: a
    /// pattern names no file, so there is no entry for it to bound.
    @Test void aWindowOnAUniformPatternBoundsTheReader() throws Exception {
        Path dir = uniformSeries("");
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: suffix
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec[0..150]
                  shard_stride: 100
                  shard_count: 3
                  record_count: 240
            """);
        VectorReader<float[]> base = view(dir).baseVectors();
        assertEquals(150, base.count(), "the suffix bounds the facet, not shard 0");
        assertEquals(149 * 100f, base.get(149)[0], "record 149 lives in shard 1");
        assertEquals("0..150", view(dir).facet("base_vectors").orElseThrow().window(), "the suffix is reported as the facet window");
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: twice
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec[0..150]
                  shard_stride: 100
                  shard_count: 3
                  record_count: 240
                  window: 0..100
            """);
        VectorDataException twice = assertThrows(VectorDataException.class, () -> TestDataGroup.load(dir.toUri(), settings()));
        assertTrue(twice.getMessage().contains("twice"), twice.getMessage());
    }

    @Test void aWindowInsideOneShardPlansOnlyThatShard() throws Exception {
        PrefetchPlan plan = view(uniformSeries("")).prefetchPlan("base_vectors", DSWindow.parse("5..10"));
        assertFalse(plan.degradesToFullDownload());
        assertEquals(List.of(new ShardRange(0, 5 * BPR, 10 * BPR)), plan.byteRanges());
    }

    @Test void aWindowAcrossASeamPlansOneRangePerShard() throws Exception {
        PrefetchPlan plan = view(uniformSeries("")).prefetchPlan("base_vectors", DSWindow.parse("80..220"));
        assertEquals(List.of(
            new ShardRange(0, 80 * BPR, 100 * BPR),
            new ShardRange(1, 0, 100 * BPR),
            new ShardRange(2, 0, 20 * BPR)), plan.byteRanges(), "the window touches three shards");
        assertEquals(140 * BPR, plan.byteRanges().stream().mapToLong(ShardRange::length).sum());
    }

    @Test void rangesInDifferentShardsNeverMerge() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("nm"));
        fvec(dir, "base__0000.fvec", 10, 0);
        fvec(dir, "base__0001.fvec", 10, 10);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: nm
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 10
                  shard_count: 2
                  record_count: 20
            """);
        PrefetchPlan plan = view(dir).prefetchPlan("base_vectors", DSWindow.parse("9..11"));
        assertEquals(2, plan.byteRanges().size(), "adjacent bytes in different files are two fetches: " + plan.byteRanges());
        assertEquals(0, plan.byteRanges().get(0).shard());
        assertEquals(1, plan.byteRanges().get(1).shard());
        assertEquals(1, plan.byteRanges().get(0).length() / BPR);
        assertEquals(1, plan.byteRanges().get(1).length() / BPR);
    }

    @Test void aWholeFacetPlanNamesEveryShard() throws Exception {
        PrefetchPlan plan = view(uniformSeries("")).prefetchPlan("base_vectors", DSWindow.ALL);
        assertEquals(List.of(new ShardRange(0, 0, 100 * BPR), new ShardRange(1, 0, 100 * BPR), new ShardRange(2, 0, 40 * BPR)),
            plan.byteRanges());
        assertFalse(plan.degradesToFullDownload(), "asking for everything is a request, not a fallback");
    }

    @Test void aVvecSeriesReadsThroughPerFileIndexes() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("vvec"));
        int[][] first = {{0}, {1, 1}, {2, 2, 2}, {3}, {4, 4}};
        int[][] second = {{5, 5, 5}, {6}, {7, 7}};
        FixtureSupport.vvec(dir, "meta__0000.ivvec", first);
        FixtureSupport.vvec(dir, "meta__0001.ivvec", second);
        fvec(dir, "base.fvec", 8, 0);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: vvec
            profiles:
              default:
                base_vectors: base.fvec
                metadata_content:
                  source: meta__NNNN.ivvec
                  shard_stride: 5
                  shard_count: 2
                  record_count: 8
            """);
        TestDataView view = view(dir);
        VvecReader<?> reader = view.openVariableFacet("metadata_content");
        assertEquals(8, reader.count());
        assertEquals(3, reader.dimensionAt(2));
        assertArrayEquals(new int[] {4, 4}, (int[]) reader.get(4), "the last record of shard 0");
        assertArrayEquals(new int[] {5, 5, 5}, (int[]) reader.get(5), "the first record of shard 1, through its own index");
        assertArrayEquals(new int[] {7, 7}, (int[]) reader.get(7));
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.parse("3..7"));
        assertFalse(plan.degradesToFullDownload(), "each file's sidecar makes its shard windowable");
        assertEquals(2, plan.byteRanges().size(), "the window spans both shards");
        // Record starts × 8 per touched file, summed: 5 + 3 records.
        assertEquals((5 + 3) * 8L, plan.prerequisiteBytes(), "each shard's index is a separate read, and a window touching two pays for two");
        long firstFile = Files.size(dir.resolve("meta__0000.ivvec"));
        assertEquals(new ShardRange(0, 4 + 4 + (4 + 8) + (4 + 12), firstFile), plan.byteRanges().get(0), "records 3..5 of file 0 run to its end");
        assertEquals(new ShardRange(1, 0, (4 + 12) + (4 + 4)), plan.byteRanges().get(1), "records 0..2 of file 1");
    }

    // -- Declarations that disagree with themselves are refused at load --

    private VectorDataException refusedAtLoad(String name, String facet) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve(name));
        Files.writeString(dir.resolve("dataset.yaml"), "name: " + name + "\nprofiles:\n  default:\n    base_vectors:\n" + facet);
        return assertThrows(VectorDataException.class, () -> TestDataGroup.load(dir.toUri(), settings()));
    }

    @Test void anArraySourceCannotAlsoCarryAUniformLayout() throws Exception {
        VectorDataException mixed = refusedAtLoad("mixed", "      source: [ a.fvec=1, b.fvec=1 ]\n      shard_stride: 1\n      record_count: 2\n");
        assertTrue(mixed.getMessage().contains("array"), mixed.getMessage());
    }

    @Test void aPatternWithoutItsLayoutIsIncomplete() throws Exception {
        VectorDataException incomplete = refusedAtLoad("nolayout", "      source: base__NNNN.fvec\n      record_count: 2\n");
        assertTrue(incomplete.getMessage().contains("shard_stride"), incomplete.getMessage());
        VectorDataException noPattern = refusedAtLoad("nopattern", "      source: base.fvec\n      shard_stride: 1\n      shard_count: 2\n      record_count: 2\n");
        assertTrue(noPattern.getMessage().contains("NNNN"), noPattern.getMessage());
        VectorDataException noTotal = refusedAtLoad("nototal", "      source: base__NNNN.fvec\n      shard_stride: 1\n      shard_count: 2\n");
        assertTrue(noTotal.getMessage().contains("record_count"), noTotal.getMessage());
    }

    @Test void aSliceCountThatDisagreesWithItsIntervalIsRefused() throws Exception {
        VectorDataException mismatch = refusedAtLoad("slice", "      source:\n        - a.fvec[0..10]=11\n        - b.fvec=5\n      record_count: 16\n");
        assertTrue(mismatch.getMessage().contains("declared count 11"), mismatch.getMessage());
    }

    @Test void aRemoteBareEntryInASeriesIsRefusedWithoutFetching() throws Exception {
        VectorDataException unbounded = refusedAtLoad("remote",
            "      source: [ http://127.0.0.1:9/a.fvec, http://127.0.0.1:9/b.fvec ]\n      record_count: 2\n");
        assertTrue(unbounded.getMessage().contains("without fetching"), unbounded.getMessage());
    }

    @Test void anAllDigitTokenBeforeTheShardFieldIsRefused() throws Exception {
        VectorDataException ambiguous = refusedAtLoad("ambiguous",
            "      source: p__1234__NNNN.fvec\n      shard_stride: 1\n      shard_count: 2\n      record_count: 2\n");
        assertTrue(ambiguous.getMessage().contains("two readings"), ambiguous.getMessage());
    }

    @Test void aPlainFacetIsUntouchedByAnyOfThis() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("plain"));
        fvec(dir, "base.fvec", 10, 0);
        TestDataView view = view(dataset("plain", "name: plain\nprofiles:\n  default:\n    base_vectors: base.fvec\n"));
        FacetDescriptor facet = view.facet("base_vectors").orElseThrow();
        assertFalse(facet.isSeries());
        assertNotNull(facet.source(), "a single file keeps its source");
        assertEquals(List.of(ShardRange.whole(0, 10 * BPR)), view.prefetchPlan("base_vectors", DSWindow.ALL).byteRanges(),
            "a single file is shard 0");
    }

    // -- format_version --

    @Test void aDatasetFromTheFutureIsRefusedWithBothNumbers() throws Exception {
        Path dir = dataset("future", "name: future\nformat_version: 99\nprofiles:\n  default:\n    base_vectors: base.fvec\n");
        VectorDataException refused = assertThrows(VectorDataException.class, () -> TestDataGroup.load(dir.toUri(), settings()),
            "a dataset above this build's version must not load at all");
        assertTrue(refused.getMessage().contains("99"), "names what the dataset needs: " + refused.getMessage());
        assertTrue(refused.getMessage().contains(String.valueOf(FormatVersion.SUPPORTED)), "names what this build supports: " + refused.getMessage());
    }

    @Test void aStatedVersionBelowTheContentIsRefused() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("understated"));
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: understated
            format_version: 1
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 1
                  shard_count: 2
                  record_count: 2
            """);
        VectorDataException refused = assertThrows(VectorDataException.class, () -> TestDataGroup.load(dir.toUri(), settings()));
        assertTrue(refused.getMessage().contains("understate"), refused.getMessage());
    }

    @Test void anAbsentVersionIsNotAnUnderstatement() throws Exception {
        TestDataView view = view(uniformSeries(""));
        assertEquals(240, view.baseVectors().count(), "an unannotated sharded dataset loads: absence is not a claim");
        Path generous = uniformSeries("");
        Files.writeString(generous.resolve("dataset.yaml"), "format_version: 2\n" + Files.readString(generous.resolve("dataset.yaml")));
        assertEquals(240, view(generous).baseVectors().count(), "a version equal to the content's requirement is accepted");
        Path plain = dataset("generous", "format_version: 2\nname: g\nprofiles:\n  default:\n    base_vectors: base.fvec\n");
        assertNotNull(TestDataGroup.load(plain.toUri(), settings()), "a version higher than the content requires is merely generous");
    }

    // -- Files that are not one facet, and what a series promises --

    @Test void aSeriesWithADisagreeingShardIsRefused() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("disagree"));
        fvec(dir, "base__0000.fvec", 10, 0);
        FixtureSupport.fvec(dir, "base__0001.fvec", new float[10][8]);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: disagree
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 10
                  shard_count: 2
                  record_count: 20
            """);
        VectorDataException refused = assertThrows(VectorDataException.class, () -> view(dir).baseVectors());
        assertTrue(refused.getMessage().contains("share one shape"), "shards of different dimension are not one facet: " + refused.getMessage());
        assertTrue(refused.getMessage().contains("base__0001.fvec"), "and the message names the odd one out: " + refused.getMessage());
    }

    @Test void aGapInTheMiddleOfASeriesIsReportedByName() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("midgap"));
        fvec(dir, "base__0000.fvec", 10, 0);
        fvec(dir, "base__0001.fvec", 10, 10);
        fvec(dir, "base__0003.fvec", 5, 30);
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: midgap
            profiles:
              default:
                base_vectors:
                  source: base__NNNN.fvec
                  shard_stride: 10
                  shard_count: 4
                  record_count: 35
            """);
        VectorDataException missing = assertThrows(VectorDataException.class, () -> view(dir).baseVectors());
        assertTrue(missing.getMessage().contains("base__0002.fvec"), "a hole in the middle is named, never skipped: " + missing.getMessage());
    }

    @Test void aLocalSeriesReportsLocalThroughItsReader() throws Exception {
        VectorReader<float[]> base = view(uniformSeries("")).baseVectors();
        CacheStats stats = base.cacheStats();
        assertEquals(AccessMode.LOCAL, stats.accessMode(), "every file is local, so the facet's promise is local");
        assertEquals(240 * BPR, stats.totalBytes(), "bytes are summed over every shard");
        assertTrue(stats.complete());
        assertTrue(base.isComplete());
    }

    @Test void theWeakestAccessModeIsTheOneTrueOfEveryFile() {
        assertEquals(AccessMode.MERKLE_HASHED, AccessMode.weakest(List.of(AccessMode.LOCAL, AccessMode.MERKLE_HASHED)));
        assertEquals(AccessMode.MERKLE_CHUNKED, AccessMode.weakest(List.of(AccessMode.MERKLE_HASHED, AccessMode.MERKLE_CHUNKED)),
            "a trusted-bytes shard is weaker than a verified one");
        assertEquals(AccessMode.FULL_TRANSFER, AccessMode.weakest(List.of(AccessMode.LOCAL, AccessMode.MERKLE_HASHED, AccessMode.FULL_TRANSFER)),
            "one shard that must download whole makes the facet's promise that");
        assertEquals(AccessMode.LOCAL, AccessMode.weakest(List.of(AccessMode.LOCAL, AccessMode.LOCAL)));
        assertNull(AccessMode.weakest(List.of()), "a facet with no files makes no promise, which is not the weakest promise");
    }

    @Test void prefetchingAMappedSeriesWindowNeedsNoConsent() throws Exception {
        TestDataView view = view(uniformSeries(""));
        PrefetchReport report = view.prefetch("base_vectors", DSWindow.parse("80..220"), WholeFacetFallback.REFUSE);
        assertEquals(3, report.rangesFetched(), "one range per shard the window spans, fetched without a fallback in play");
        assertFalse(report.planned().degradesToFullDownload());
        view.prefetchInBackground("base_vectors", DSWindow.parse("0..240"), WholeFacetFallback.REFUSE).join();
    }
}
