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

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/// Prefetch against real remote storage, where the chunk state is real
/// and the numbers mean something: the local suite proves the plumbing,
/// but on local storage every fetch is a no-op.
class PrefetchRemoteIntegrationTest {
    @TempDir Path temporary;
    private final java.util.List<HttpServer> servers = new java.util.ArrayList<>();

    /// Small enough that a modest fixture spans many chunks, so a
    /// window covers some of them and not others.
    private static final int CHUNK = 4096;
    /// dim 16 → 68 bytes per record; 4 KiB chunks hold 60 records.
    private static final int RECORD_BYTES = 68;

    @org.junit.jupiter.api.AfterEach void stopServers() { for (HttpServer server : servers) server.stop(0); }

    private static byte[] fvecBytes(int dim, int records) {
        ByteBuffer bytes = ByteBuffer.allocate(records * (4 + dim * 4)).order(ByteOrder.LITTLE_ENDIAN);
        for (int record = 0; record < records; record++) { bytes.putInt(dim); for (int d = 0; d < dim; d++) bytes.putFloat(record + d); }
        return bytes.array();
    }

    private HttpServer serveDirectory(Path published, AtomicInteger rangeRequests, boolean acceptRanges) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            Path file = published.resolve(exchange.getRequestURI().getPath().substring(1));
            if (!Files.isRegularFile(file)) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            byte[] payload = Files.readAllBytes(file);
            if ("HEAD".equals(exchange.getRequestMethod())) {
                exchange.getResponseHeaders().add("Content-Length", String.valueOf(payload.length));
                if (acceptRanges) exchange.getResponseHeaders().add("Accept-Ranges", "bytes");
                exchange.sendResponseHeaders(200, -1); exchange.close(); return;
            }
            String range = acceptRanges ? exchange.getRequestHeaders().getFirst("Range") : null;
            if (range != null) {
                rangeRequests.incrementAndGet();
                String[] bounds = range.substring("bytes=".length()).split("-");
                int from = Integer.parseInt(bounds[0]); int to = Integer.parseInt(bounds[1]);
                byte[] section = Arrays.copyOfRange(payload, from, to + 1);
                exchange.getResponseHeaders().add("Content-Range", "bytes " + from + "-" + to + "/" + payload.length);
                exchange.sendResponseHeaders(206, section.length); exchange.getResponseBody().write(section);
            } else { exchange.sendResponseHeaders(200, payload.length); exchange.getResponseBody().write(payload); }
            exchange.close();
        });
        server.start();
        servers.add(server);
        return server;
    }

    /// Publishes an fvec, a ragged ivvec with its `IDXFOR__` sidecar,
    /// and an unwindowable parquet blob — each `.mref`-published so the
    /// chunk bitmap is real — then writes a local `dataset.yaml`
    /// pointing at them.
    private TestDataView remoteView(String name, boolean acceptRanges, boolean publishMref, AtomicInteger rangeRequests) throws Exception {
        Path published = Files.createDirectories(temporary.resolve("pub"));
        byte[] fvec = fvecBytes(16, 4000);
        Files.write(published.resolve("base.fvec"), fvec);
        int[][] ragged = new int[400][];
        for (int record = 0; record < 400; record++) { ragged[record] = new int[1 + record % 17]; }
        FixtureSupport.vvec(published, "meta.ivvec", ragged);
        byte[] blob = new byte[40_000]; Arrays.fill(blob, (byte) 7);
        Files.write(published.resolve("blob.parquet"), blob);
        if (publishMref) {
            for (String file : new String[] {"base.fvec", "meta.ivvec", "blob.parquet"}) {
                byte[] payload = Files.readAllBytes(published.resolve(file));
                Files.write(published.resolve(file + ".mref"), FixtureSupport.mref(payload, CHUNK));
            }
        }
        HttpServer server = serveDirectory(published, rangeRequests, acceptRanges);
        String base = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Path dataset = Files.createDirectories(temporary.resolve(name));
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: %s
            profiles:
              default:
                base_vectors: %sbase.fvec
                metadata_content: %smeta.ivvec
                metadata_predicates: %sblob.parquet
            """.formatted(name, base, base, base));
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        return TestDataGroup.load(dataset.resolve("dataset.yaml").toUri(), settings).profile("default");
    }

    @Test void coalescingUsesRealChunkBoundaries() throws Exception {
        TestDataView view = remoteView("coalesce-chunks", true, true, new AtomicInteger());
        int recordsPerChunk = CHUNK / RECORD_BYTES;

        // Two windows inside chunk 0: byte adjacency would keep them
        // apart; chunk adjacency merges them, because they are already
        // one fetch.
        PrefetchPlan sameChunk = view.prefetchPlan("base_vectors", DSWindow.parse("[0..10, 12..20]"));
        assertEquals(2, sameChunk.requestedRanges().size());
        assertEquals(1, sameChunk.requests(), "two ranges inside one chunk are one fetch, not two");

        // Chunk 0 and chunk 1: adjacent, contiguous on the device.
        PrefetchPlan adjacent = view.prefetchPlan("base_vectors",
            DSWindow.parse("[0..10, " + (recordsPerChunk + 1) + ".." + (recordsPerChunk + 10) + "]"));
        assertEquals(2, adjacent.requestedRanges().size());
        assertEquals(1, adjacent.requests(), "adjacent chunks merge");

        // Far apart, with whole chunks untouched between: no bridge.
        PrefetchPlan apart = view.prefetchPlan("base_vectors", DSWindow.parse("[0..10, 200..210]"));
        assertEquals(2, apart.requestedRanges().size());
        assertEquals(2, apart.requests(), "a gap of whole chunks must not be bridged");
        assertEquals(2, apart.fills().size(), "each request gets its own chunk accounting");
    }

    @Test void aRemoteWindowFetchesOnlyItsChunks() throws Exception {
        TestDataView view = remoteView("window-chunks", true, true, new AtomicInteger());
        PrefetchPlan plan = view.prefetchPlan("base_vectors", DSWindow.parse("100..200"));
        assertFalse(plan.fills().isEmpty(), "remote storage has chunk fills");
        assertTrue(plan.bytesToFetch() > 0, "nothing is resident yet, so there is work to do");
        assertTrue(plan.bytesToFetch() < plan.facetBytes(),
            "a 100-record window must cost less than the whole " + plan.facetBytes() + " byte facet");

        view.prefetchInBackground("base_vectors", DSWindow.parse("100..200"), WholeFacetFallback.REFUSE).join();

        PrefetchPlan after = view.prefetchPlan("base_vectors", DSWindow.parse("100..200"));
        assertTrue(after.isResident(), "the window the prefetch just fetched must read as resident");
        assertEquals(0, after.bytesToFetch());
    }

    @Test void aRemoteVvecWindowUsesThePublishedIndex() throws Exception {
        TestDataView view = remoteView("vvec-remote", true, true, new AtomicInteger());
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.parse("100..150"));
        assertFalse(plan.degradesToFullDownload(), "a published index makes a remote vvec windowable");
        assertEquals(1, plan.requestedRanges().size());
        assertTrue(plan.prerequisiteBytes() > 0, "the index had to be read, and the plan says so");
        ShardRange requested = plan.requestedRanges().get(0);
        assertTrue(requested.end() > requested.start() && requested.end() <= plan.facetBytes());
        assertTrue(requested.length() < plan.facetBytes(), "50 of 400 ragged records must be a fraction of the facet");

        view.prefetch("metadata_content", DSWindow.parse("100..150"), WholeFacetFallback.REFUSE);
        assertTrue(view.prefetchPlan("metadata_content", DSWindow.parse("100..150")).isResident());
    }

    @Test void readingAPrefetchedWindowFetchesNothingFurther() throws Exception {
        AtomicInteger rangeRequests = new AtomicInteger();
        TestDataView view = remoteView("read-after", true, true, rangeRequests);
        view.prefetch("base_vectors", DSWindow.parse("500..600"), WholeFacetFallback.REFUSE);

        int before = rangeRequests.get();
        VectorReader<?> reader = view.openFacet("base_vectors");
        for (long record = 500; record < 600; record++) {
            float[] vector = (float[]) reader.get(record);
            assertEquals(16, vector.length);
            assertEquals((float) record, vector[0], "record " + record + " decodes correctly");
        }
        assertEquals(before, rangeRequests.get(), "reading inside a prefetched window must not fetch another chunk");
    }

    @Test void aBackgroundPrefetchAdvancesItsCounters() throws Exception {
        TestDataView view = remoteView("progress-counters", true, true, new AtomicInteger());
        PrefetchHandle handle = view.prefetchInBackground("base_vectors",
            DSWindow.parse("[0..200, 1000..1200, 3000..3200]"), WholeFacetFallback.REFUSE);
        int plannedRanges = handle.plan().requests();
        assertTrue(handle.plan().bytesToFetch() > 0, "nothing is resident yet");

        PrefetchReport report = handle.join();
        assertEquals(plannedRanges, report.rangesFetched(), "every planned range must be accounted for");
        assertTrue(handle.bytesFetched() > 0, "the byte counter must actually advance");
        assertTrue(view.prefetchPlan("base_vectors", DSWindow.parse("[0..200, 1000..1200, 3000..3200]")).isResident());
    }

    @Test void progressIsReportedNotMerelyPlumbed() throws Exception {
        TestDataView view = remoteView("progress-cb", true, true, new AtomicInteger());
        AtomicInteger calls = new AtomicInteger();
        AtomicLong seen = new AtomicLong();
        view.prefetch("metadata_content", DSWindow.parse("0..400"), WholeFacetFallback.REFUSE,
            (cached, total) -> { calls.incrementAndGet(); seen.accumulateAndGet(cached, Math::max); });
        assertTrue(calls.get() > 0, "the progress callback must actually fire");
        assertTrue(seen.get() > 0, "and carry a byte count");
    }

    /// A server that ignores `Range` has no partial fetch to offer, so
    /// the first touch — the header read the record mapping needs —
    /// transfers the whole file. By planning time every chunk is
    /// resident: the window was resolvable, the facet simply arrived
    /// early, and prefetching it is a no-op needing no consent.
    @Test void aServerWithoutRangeSupportArrivesWholeAtFirstTouch() throws Exception {
        TestDataView view = remoteView("no-range", false, false, new AtomicInteger());
        PrefetchPlan plan = view.prefetchPlan("base_vectors", DSWindow.parse("100..200"));
        assertFalse(plan.degradesToFullDownload(), "the window was resolvable; the facet simply arrived early");
        assertTrue(plan.isResident(), "no-range storage arrives whole, so a window has nothing to fetch");
        assertEquals(0, plan.bytesToFetch());
        view.prefetch("base_vectors", DSWindow.parse("100..200"), WholeFacetFallback.REFUSE);
    }

    /// Planning must not perform the transfer it is pricing: a remote
    /// vvec with no published sidecar can only be indexed by walking
    /// the file, which downloads all of it — so the plan reports the
    /// degrade and the consent gate decides, with nothing moved yet.
    @Test void planningARemoteVvecWithoutASidecarDownloadsNothing() throws Exception {
        AtomicInteger rangeRequests = new AtomicInteger();
        Path published = Files.createDirectories(temporary.resolve("pub"));
        int[][] ragged = new int[40][];
        for (int record = 0; record < 40; record++) ragged[record] = new int[1 + record % 7];
        FixtureSupport.vvec(published, "meta.ivvec", ragged);
        Files.delete(published.resolve("IDXFOR__meta.ivvec.i32"));
        byte[] payload = Files.readAllBytes(published.resolve("meta.ivvec"));
        Files.write(published.resolve("meta.ivvec.mref"), FixtureSupport.mref(payload, CHUNK));
        var server = serveDirectory(published, rangeRequests, true);
        Path dataset = Files.createDirectories(temporary.resolve("no-sidecar"));
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: no-sidecar
            profiles:
              default:
                metadata_content: http://127.0.0.1:%d/meta.ivvec
            """.formatted(server.getAddress().getPort()));
        VectorDataSettings isolated = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        TestDataView view = TestDataGroup.load(dataset.resolve("dataset.yaml").toUri(), isolated).profile("default");

        int before = rangeRequests.get();
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.parse("10..20"));
        assertTrue(plan.degradesToFullDownload(), "no index, no ordinal mapping — the plan says so");
        assertEquals(before, rangeRequests.get(), "pricing the transfer must not perform it");

        assertThrows(VectorDataException.class,
            () -> view.prefetch("metadata_content", DSWindow.parse("10..20"), WholeFacetFallback.REFUSE));
        view.prefetch("metadata_content", DSWindow.parse("10..20"), WholeFacetFallback.ALLOW);
        assertTrue(view.prefetchPlan("metadata_content", DSWindow.ALL).isResident(),
            "consenting to the degrade fetches the whole facet");
    }

    /// In this format, `ivec` records are length-qualified but fixed
    /// throughout — ground-truth neighbor files — so a window maps at
    /// the uniform header stride with no offset index involved. Only
    /// the `*vvec` extensions carry variable-length records.
    @Test void aRemoteIvecWindowMapsAtTheHeaderStride() throws Exception {
        Path published = Files.createDirectories(temporary.resolve("pub"));
        int dim = 100, records = 50, recordBytes = 4 + dim * 4;
        ByteBuffer gt = ByteBuffer.allocate(records * recordBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int record = 0; record < records; record++) { gt.putInt(dim); for (int at = 0; at < dim; at++) gt.putInt(record + at); }
        Files.write(published.resolve("gt.ivecs"), gt.array());
        Files.write(published.resolve("gt.ivecs.mref"), FixtureSupport.mref(gt.array(), CHUNK));
        var server = serveDirectory(published, new AtomicInteger(), true);
        Path dataset = Files.createDirectories(temporary.resolve("uniform-ivec"));
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: uniform-ivec
            profiles:
              default:
                neighbor_indices: http://127.0.0.1:%d/gt.ivecs
            """.formatted(server.getAddress().getPort()));
        VectorDataSettings isolated = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        TestDataView view = TestDataGroup.load(dataset.resolve("dataset.yaml").toUri(), isolated).profile("default");

        PrefetchPlan plan = view.prefetchPlan("neighbor_indices", DSWindow.parse("[0..3)"));
        assertFalse(plan.degradesToFullDownload(), "a uniform ivec is windowable by its header stride");
        assertEquals(List.of(ShardRange.whole(0, 3L * recordBytes)), plan.requestedRanges());

        view.prefetch("neighbor_indices", DSWindow.parse("[0..3)"), WholeFacetFallback.REFUSE);
        assertArrayEquals(new int[] {2, 3}, java.util.Arrays.copyOfRange((int[]) view.neighborIndices().get(2), 0, 2),
            "the reader decodes the records the prefetch warmed");
    }

    @Test void allowingTheFallbackFetchesAWholeRemoteFacet() throws Exception {
        TestDataView view = remoteView("allow-remote", true, true, new AtomicInteger());
        DSWindow window = DSWindow.parse("2..4");
        PrefetchPlan plan = view.prefetchPlan("metadata_predicates", window);
        assertTrue(plan.degradesToFullDownload());
        assertEquals(40_000, plan.facetBytes());
        assertEquals(plan.facetBytes(), plan.bytesToFetch(), "the honest cost of the fallback is the whole facet");

        VectorDataException refused = assertThrows(VectorDataException.class,
            () -> view.prefetch("metadata_predicates", window, WholeFacetFallback.REFUSE));
        assertTrue(refused.getMessage().contains("40000"), refused.getMessage());

        view.prefetch("metadata_predicates", window, WholeFacetFallback.ALLOW);
        assertTrue(view.prefetchPlan("metadata_predicates", DSWindow.ALL).isResident(),
            "consenting to the whole facet fetches the whole facet");
    }

    // -- Facets spread across several remote files --

    /// dim 8 → 36 bytes per record.
    private static final int SERIES_BPR = 36;

    private static byte[] seriesShard(int records, int first) {
        ByteBuffer bytes = ByteBuffer.allocate(records * SERIES_BPR).order(ByteOrder.LITTLE_ENDIAN);
        for (int r = 0; r < records; r++) { bytes.putInt(8); for (int d = 0; d < 8; d++) bytes.putFloat((first + r) * 100f + d); }
        return bytes.array();
    }

    /// Publishes `big__0000..` shards of `records` each, `.mref`-published
    /// so chunk residency is real, and returns the server's base URL.
    private String publishSeries(int shards, int records, boolean mrefs) throws Exception {
        Path published = Files.createDirectories(temporary.resolve("pub"));
        for (int shard = 0; shard < shards; shard++) {
            byte[] payload = seriesShard(records, shard * records);
            Path file = published.resolve(String.format("big__%04d.fvec", shard));
            Files.write(file, payload);
            if (mrefs) Files.write(published.resolve(file.getFileName() + ".mref"), FixtureSupport.mref(payload, CHUNK));
        }
        HttpServer server = serveDirectory(published, new AtomicInteger(), true);
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    private TestDataView seriesView(String name, String yaml, String profile) throws Exception {
        Path dataset = Files.createDirectories(temporary.resolve(name));
        Files.writeString(dataset.resolve("dataset.yaml"), yaml);
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        return TestDataGroup.load(dataset.resolve("dataset.yaml").toUri(), settings).profile(profile);
    }

    private static String uniformYaml(String base, int shards, int records, String extra) {
        return """
            name: remote-series
            profiles:
              default:
                base_vectors:
                  source: %sbig__NNNN.fvec
                  shard_stride: %d
                  shard_count: %d
                  record_count: %d
            """.formatted(base, records, shards, shards * records) + extra;
    }

    @Test void aUniformSeriesReadsOverHttp() throws Exception {
        String base = publishSeries(2, 25, false);
        VectorReader<float[]> reader = seriesView("uniform-http", uniformYaml(base, 2, 25, ""), "default").baseVectors();
        assertEquals(50, reader.count(), "the count spans the series");
        assertEquals(8, reader.dimension());
        for (int i = 0; i < 50; i++) { assertEquals(i * 100f, reader.get(i)[0], "record " + i + " over HTTP"); assertEquals(i * 100f + 7, reader.get(i)[7]); }
        for (int i = 49; i >= 0; i--) assertEquals(i * 100f, reader.get(i)[0]);
    }

    @Test void anExplicitSeriesReadsOverHttp() throws Exception {
        String base = publishSeries(2, 25, false);
        VectorReader<float[]> reader = seriesView("explicit-http", """
            name: explicit
            profiles:
              default:
                base_vectors:
                  source:
                    - %sbig__0000.fvec=25
                    - %sbig__0001.fvec=25
                  record_count: 50
            """.formatted(base, base), "default").baseVectors();
        assertEquals(50, reader.count());
        for (int i : new int[] {0, 24, 25, 49}) assertEquals(i * 100f, reader.get(i)[0], "record " + i);
    }

    @Test void aWindowInsideOneShardDoesNotDegradeToAFullDownload() throws Exception {
        String base = publishSeries(2, 25, true);
        TestDataView view = seriesView("one-shard", uniformYaml(base, 2, 25, ""), "default");
        PrefetchPlan plan = view.prefetchPlan("base_vectors", DSWindow.parse("5..10"));
        assertFalse(plan.degradesToFullDownload(), "a five-record window must not price as the whole facet");
        assertEquals(1, plan.fills().size(), "a window inside shard 0 touches one shard");
        assertEquals(0, plan.byteRanges().get(0).shard());
    }

    @Test void aWindowAcrossTheSeamPlansOneFillPerShard() throws Exception {
        String base = publishSeries(2, 25, true);
        TestDataView view = seriesView("seam", uniformYaml(base, 2, 25, ""), "default");
        PrefetchPlan plan = view.prefetchPlan("base_vectors", DSWindow.parse("20..30"));
        assertFalse(plan.degradesToFullDownload());
        assertEquals(2, plan.fills().size(), "the window spans both shards");
        assertEquals(2, plan.requests());
        view.prefetch("base_vectors", DSWindow.parse("20..30"), WholeFacetFallback.REFUSE);
        assertTrue(view.prefetchPlan("base_vectors", DSWindow.parse("20..30")).isResident());
    }

    private static void assertOnlyTheWindowIsResident(TestDataView view, long windowRecords) {
        VectorReader<float[]> reader = view.baseVectors();
        CacheStats stats = reader.cacheStats();
        assertTrue(stats.cachedBytes() >= windowRecords * SERIES_BPR,
            "the window's own bytes must be resident (" + stats.cachedBytes() + " of " + stats.totalBytes() + ")");
        assertTrue(!reader.isComplete() && stats.cachedBytes() <= stats.totalBytes() * 2 / 3,
            "precaching the " + windowRecords + "-record window fetched " + stats.cachedBytes() + " of " + stats.totalBytes()
                + " bytes: the last shard was dragged in");
        assertEquals(windowRecords, reader.count());
        assertEquals((windowRecords - 1) * 100f, reader.get(windowRecords - 1)[0]);
    }

    /// The whole-profile prebuffer honours the window a facet declares,
    /// a series included: a "small part" of a sharded base costs what
    /// it can address, not the whole base.
    @Test void prebufferingASizedProfileOfASeriesFetchesOnlyItsWindow() throws Exception {
        String base = publishSeries(3, 400, true);
        TestDataView small = seriesView("sized", uniformYaml(base, 3, 400, """
              small:
                base_count: 500
                base_vectors:
                  source: %sbig__NNNN.fvec[0..500]
                  shard_stride: 400
                  shard_count: 3
                  record_count: 1200
            """.formatted(base)), "small");
        small.prebuffer(WholeFacetFallback.REFUSE, PrebufferProgress.NONE);
        assertOnlyTheWindowIsResident(small, 500);
    }

    /// A sized profile that inherits the base declares its window by
    /// `base_count`; the same rule applies through inheritance.
    @Test void anInheritedSizedWindowIsHonouredToo() throws Exception {
        String base = publishSeries(3, 400, true);
        TestDataView small = seriesView("sized-inherit", uniformYaml(base, 3, 400, "  small:\n    base_count: 500\n"), "small");
        small.prebuffer(PrebufferProgress.NONE);
        assertOnlyTheWindowIsResident(small, 500);
    }

    /// The reader a windowed facet hands back warms its window, not the
    /// files it was cut from.
    @Test void aWindowedReaderPrebuffersOnlyItsWindow() throws Exception {
        String base = publishSeries(3, 400, true);
        TestDataView small = seriesView("sized-reader", uniformYaml(base, 3, 400, "  small:\n    base_count: 500\n"), "small");
        small.baseVectors().prebuffer();
        assertOnlyTheWindowIsResident(small, 500);
    }

    @Test void aFacetByteRangePrebuffersTheShardItLivesIn() throws Exception {
        String base = publishSeries(3, 400, true);
        TestDataView view = seriesView("deep", uniformYaml(base, 3, 400, ""), "default");
        VectorReader<float[]> reader = view.baseVectors();
        long before = reader.cacheStats().cachedBytes();
        view.prefetch("base_vectors", DSWindow.parse("1150..1160"), WholeFacetFallback.REFUSE);
        CacheStats after = reader.cacheStats();
        assertTrue(after.cachedBytes() > before, "a range in the last shard fetched nothing (" + before + " → " + after.cachedBytes() + ")");
        assertFalse(reader.isComplete(), "only the range's chunks were fetched");
        assertTrue(after.cachedBytes() - before < after.totalBytes() / 4,
            "fetched " + (after.cachedBytes() - before) + " of " + after.totalBytes() + " bytes for ten records");
        assertEquals(1155 * 100f, reader.get(1155)[0]);
    }

    @Test void cacheStatsForASeriesCoverEveryShard() throws Exception {
        String base = publishSeries(3, 400, true);
        TestDataView view = seriesView("stats", uniformYaml(base, 3, 400, ""), "default");
        CacheStats stats = view.baseVectors().cacheStats();
        assertEquals(3L * 400 * SERIES_BPR, stats.totalBytes(), "every shard's bytes");
        assertEquals(CHUNK, stats.chunkSize());
        assertEquals(AccessMode.MERKLE_HASHED, stats.accessMode());
        assertFalse(stats.complete());
    }

    @Test void aMissingShardOverHttpIsNamed() throws Exception {
        String base = publishSeries(2, 25, false);
        TestDataView view = seriesView("missing", uniformYaml(base, 3, 25, ""), "default");
        VectorDataException missing = assertThrows(VectorDataException.class, view::baseVectors);
        assertTrue(missing.getMessage().contains("big__0002.fvec"), "the message must name the missing shard: " + missing.getMessage());
    }
}
