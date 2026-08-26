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
        FixtureSupport.vvec(published, "meta.ivecs", ragged);
        byte[] blob = new byte[40_000]; Arrays.fill(blob, (byte) 7);
        Files.write(published.resolve("blob.parquet"), blob);
        if (publishMref) {
            for (String file : new String[] {"base.fvec", "meta.ivecs", "blob.parquet"}) {
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
                metadata_content: %smeta.ivecs
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
        ByteRange requested = plan.requestedRanges().get(0);
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
        FixtureSupport.vvec(published, "meta.ivecs", ragged);
        Files.delete(published.resolve("IDXFOR__meta.ivecs.i32"));
        byte[] payload = Files.readAllBytes(published.resolve("meta.ivecs"));
        Files.write(published.resolve("meta.ivecs.mref"), FixtureSupport.mref(payload, CHUNK));
        var server = serveDirectory(published, rangeRequests, true);
        Path dataset = Files.createDirectories(temporary.resolve("no-sidecar"));
        Files.writeString(dataset.resolve("dataset.yaml"), """
            name: no-sidecar
            profiles:
              default:
                metadata_content: http://127.0.0.1:%d/meta.ivecs
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
}
