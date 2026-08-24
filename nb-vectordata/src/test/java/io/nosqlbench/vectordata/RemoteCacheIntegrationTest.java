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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RemoteCacheIntegrationTest {
    @TempDir Path temporary;
    @Test void rangeCachesAndVerifiesMerkleLeaves() throws Exception {
        Path source = FixtureSupport.fvec(temporary, "remote.fvec", new float[][] {{1f, 2f}, {3f, 4f}});
        byte[] payload = Files.readAllBytes(source); byte[] mref = FixtureSupport.mref(payload, 8); AtomicInteger ranges = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/remote.fvec", exchange -> serve(exchange, payload, ranges));
        server.createContext("/remote.fvec.mref", exchange -> { exchange.getResponseHeaders().add("Content-Length", String.valueOf(mref.length)); exchange.sendResponseHeaders(200, mref.length); exchange.getResponseBody().write(mref); exchange.close(); });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/remote.fvec");
            VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
            VectorReader<float[]> reader = VectorReaders.f32(uri, settings, "remote-test");
            assertArrayEquals(new float[] {3f, 4f}, reader.get(1));
            assertEquals(AccessMode.MERKLE_HASHED, reader.cacheStats().accessMode());
            int downloaded = ranges.get(); assertTrue(downloaded > 0);
            assertArrayEquals(new float[] {1f, 2f}, reader.get(0));
            int afterFirstPass = ranges.get();
            assertArrayEquals(new float[] {3f, 4f}, reader.get(1));
            assertEquals(afterFirstPass, ranges.get(), "repeated access must use cached chunks");
            reader.prebuffer(); assertTrue(reader.isComplete());
        } finally { server.stop(0); }
    }
    @Test void rejectsMalformedPublishedMerkleReference() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "bad.fvec", new float[][] {{1f, 2f}}));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/bad.fvec", exchange -> serve(exchange, payload, new AtomicInteger()));
        server.createContext("/bad.fvec.mref", exchange -> { byte[] invalid = new byte[] {1, 2, 3}; exchange.sendResponseHeaders(200, invalid.length); exchange.getResponseBody().write(invalid); exchange.close(); });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/bad.fvec");
            assertThrows(VectorDataException.class, () -> VectorReaders.f32(uri, VectorDataSettings.builder().cacheDirectory(temporary.resolve("bad-cache")).build(), "bad"));
        } finally { server.stop(0); }
    }
    @Test void fallsBackToOneFullTransferWhenRangesAreUnavailable() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "full.fvec", new float[][] {{1f, 2f}, {3f, 4f}}));
        AtomicInteger fullRequests = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/full.fvec", exchange -> {
            if (exchange.getRequestURI().getPath().endsWith(".mref")) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            if ("HEAD".equals(exchange.getRequestMethod())) { exchange.getResponseHeaders().add("Content-Length", String.valueOf(payload.length)); exchange.sendResponseHeaders(200, -1); exchange.close(); return; }
            fullRequests.incrementAndGet(); exchange.sendResponseHeaders(200, payload.length); exchange.getResponseBody().write(payload); exchange.close();
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/full.fvec");
            VectorReader<float[]> reader = VectorReaders.f32(uri, VectorDataSettings.builder().cacheDirectory(temporary.resolve("full-cache")).build(), "full");
            assertArrayEquals(new float[] {3f, 4f}, reader.get(1));
            assertEquals(AccessMode.FULL_TRANSFER, reader.cacheStats().accessMode());
            assertEquals(1, fullRequests.get());
            assertArrayEquals(new float[] {1f, 2f}, reader.get(0));
            assertEquals(1, fullRequests.get());
        } finally { server.stop(0); }
    }
    @Test void appliesBearerTokenToReferenceAndDataRequests() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "secure.fvec", new float[][] {{1f, 2f}}));
        byte[] mref = FixtureSupport.mref(payload, payload.length); AtomicInteger authorized = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/secure.fvec", exchange -> {
            if (!"Bearer test-token".equals(exchange.getRequestHeaders().getFirst("Authorization"))) { exchange.sendResponseHeaders(401, -1); exchange.close(); return; }
            authorized.incrementAndGet(); serve(exchange, payload, new AtomicInteger());
        });
        server.createContext("/secure.fvec.mref", exchange -> {
            if (!"Bearer test-token".equals(exchange.getRequestHeaders().getFirst("Authorization"))) { exchange.sendResponseHeaders(401, -1); exchange.close(); return; }
            authorized.incrementAndGet(); exchange.sendResponseHeaders(200, mref.length); exchange.getResponseBody().write(mref); exchange.close();
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/secure.fvec");
            VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("secure-cache")).bearerToken("test-token").build();
            assertArrayEquals(new float[] {1f, 2f}, VectorReaders.f32(uri, settings, "secure").get(0));
            assertTrue(authorized.get() >= 3, "mref, HEAD probe, and range read must be authorized");
        } finally { server.stop(0); }
    }
    @Test void retriesTransientRangeFailure() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "retry.fvec", new float[][] {{1f, 2f}}));
        AtomicInteger attempts = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/retry.fvec", exchange -> {
            if (exchange.getRequestURI().getPath().endsWith(".mref")) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            if ("HEAD".equals(exchange.getRequestMethod())) { exchange.getResponseHeaders().add("Content-Length", String.valueOf(payload.length)); exchange.getResponseHeaders().add("Accept-Ranges", "bytes"); exchange.sendResponseHeaders(200, -1); exchange.close(); return; }
            if (attempts.getAndIncrement() == 0) { exchange.sendResponseHeaders(503, -1); exchange.close(); return; }
            serve(exchange, payload, new AtomicInteger());
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/retry.fvec");
            assertArrayEquals(new float[] {1f, 2f}, VectorReaders.f32(uri, VectorDataSettings.builder().cacheDirectory(temporary.resolve("retry-cache")).build(), "retry").get(0));
            assertTrue(attempts.get() >= 2);
        } finally { server.stop(0); }
    }
    @Test void fallsBackWhenOptionalMerkleSidecarIsForbidden() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "forbidden.fvec", new float[][] {{1f, 2f}}));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/forbidden.fvec", exchange -> {
            if (exchange.getRequestURI().getPath().endsWith(".mref")) { exchange.sendResponseHeaders(403, -1); exchange.close(); return; }
            serve(exchange, payload, new AtomicInteger());
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/forbidden.fvec");
            VectorReader<float[]> reader = VectorReaders.f32(uri, VectorDataSettings.builder().cacheDirectory(temporary.resolve("forbidden-cache")).build(), "forbidden");
            assertArrayEquals(new float[] {1f, 2f}, reader.get(0));
            assertEquals(AccessMode.MERKLE_CHUNKED, reader.cacheStats().accessMode());
        } finally { server.stop(0); }
    }
    @Test void httpDirectoryFallsBackToLegacyEntries() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "http-base.fvec", new float[][] {{9f, 10f}}));
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/remote/knn_entries.yaml", exchange -> {
            byte[] yaml = "\"remote:default\":\n  base: base.fvec\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, yaml.length); exchange.getResponseBody().write(yaml); exchange.close();
        });
        server.createContext("/remote/base.fvec", exchange -> {
            if (exchange.getRequestURI().getPath().endsWith(".mref")) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            serve(exchange, payload, new AtomicInteger());
        });
        server.start();
        try {
            URI root = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/remote");
            TestDataView view = TestDataGroup.load(root, VectorDataSettings.builder().cacheDirectory(temporary.resolve("http-legacy-cache")).build()).profile("default");
            assertArrayEquals(new float[] {9f, 10f}, view.baseVectors().get(0));
        } finally { server.stop(0); }
    }
    @Test void concurrentReadsDeduplicateTheMissingChunkFetch() throws Exception {
        byte[] payload = Files.readAllBytes(FixtureSupport.fvec(temporary, "concurrent.fvec", new float[][] {{1f, 2f}}));
        AtomicInteger ranges = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/concurrent.fvec", exchange -> {
            if (exchange.getRequestURI().getPath().endsWith(".mref")) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
            serve(exchange, payload, ranges);
        });
        server.start();
        try {
            URI uri = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/concurrent.fvec");
            VectorReader<float[]> reader = VectorReaders.f32(uri, VectorDataSettings.builder().cacheDirectory(temporary.resolve("concurrent-cache")).build(), "concurrent");
            java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8);
            try {
                var futures = java.util.stream.IntStream.range(0, 16).mapToObj(ignored -> pool.submit(() -> reader.get(0))).toList();
                for (var future : futures) assertArrayEquals(new float[] {1f, 2f}, future.get());
            } finally { pool.shutdownNow(); }
            assertEquals(1, ranges.get(), "all concurrent reads must share the first cached chunk");
        } finally { server.stop(0); }
    }
    private static void serve(HttpExchange exchange, byte[] payload, AtomicInteger ranges) throws IOException {
        if ("HEAD".equals(exchange.getRequestMethod())) { exchange.getResponseHeaders().add("Content-Length", String.valueOf(payload.length)); exchange.getResponseHeaders().add("Accept-Ranges", "bytes"); exchange.sendResponseHeaders(200, -1); exchange.close(); return; }
        String range = exchange.getRequestHeaders().getFirst("Range");
        if (range != null) {
            ranges.incrementAndGet(); String[] bounds = range.substring("bytes=".length()).split("-"); int from = Integer.parseInt(bounds[0]); int to = Integer.parseInt(bounds[1]);
            byte[] section = java.util.Arrays.copyOfRange(payload, from, to + 1); exchange.getResponseHeaders().add("Content-Range", "bytes " + from + "-" + to + "/" + payload.length); exchange.sendResponseHeaders(206, section.length); exchange.getResponseBody().write(section);
        } else { exchange.sendResponseHeaders(200, payload.length); exchange.getResponseBody().write(payload); }
        exchange.close();
    }
}
