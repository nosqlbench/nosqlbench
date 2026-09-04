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
package io.nosqlbench.vectordata.internal;

import io.nosqlbench.vectordata.DSWindow;
import io.nosqlbench.vectordata.VectorDataException;
import io.nosqlbench.vectordata.VectorDataSettings;
import io.nosqlbench.vectordata.VectorReader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// The ordinal model on its own: how lengths choose a map, how a global
/// ordinal becomes a shard and a file ordinal, how a window decomposes,
/// how a source string is read — and the descriptor budget a series
/// lives within.
@Tag("unit")
class ShardsTest {
    @TempDir Path temporary;

    private static Shards.Entry entry(long len) { return new Shards.Entry("f" + len + ".fvec", 0, len); }

    @Test void uniformLengthsMapInConstantTimeHoweverTheyWereSpelled() {
        Shards shards = Shards.of(List.of(entry(100), entry(100), entry(40)));
        assertTrue(shards.isUniform(), "uniformity is a property of the lengths, not the declaration form");
        assertEquals(240, shards.count());
        assertEquals(new Shards.Located(0, 0, 0), shards.locate(0));
        assertEquals(new Shards.Located(0, 99, 99), shards.locate(99));
        assertEquals(new Shards.Located(1, 0, 0), shards.locate(100));
        assertEquals(new Shards.Located(2, 39, 39), shards.locate(239));
        assertNull(shards.locate(240), "past the end is null, never a clamp");
        assertNull(shards.locate(-1));
    }

    @Test void unevenLengthsSearchPrefixSums() {
        Shards shards = Shards.of(List.of(entry(100), entry(50), entry(100)));
        assertFalse(shards.isUniform(), "a short interior shard is genuinely uneven");
        assertEquals(new Shards.Located(1, 49, 49), shards.locate(149));
        assertEquals(new Shards.Located(2, 0, 0), shards.locate(150));
        assertEquals(new Shards.Located(2, 99, 99), shards.locate(249));
        assertNull(shards.locate(250));
    }

    @Test void aSlicedEntryOffsetsFileOrdinalsByItsWindow() {
        Shards shards = Shards.of(List.of(new Shards.Entry("corpus.u32", 0, 10), new Shards.Entry("corpus.u32", 9990, 10)));
        assertEquals(new Shards.Located(1, 3, 9993), shards.locate(13), "local ordinal plus the entry's file base");
        assertFalse(shards.isSingleFile());
    }

    @Test void aWindowDecomposesIntoPerShardSubWindows() {
        Shards shards = Shards.of(List.of(entry(100), entry(100), entry(50)));
        assertEquals(List.of(
            new Shards.SubWindow(0, 80, 100, 80, 100),
            new Shards.SubWindow(1, 0, 100, 0, 100),
            new Shards.SubWindow(2, 0, 20, 0, 20)), shards.decompose(80, 220));
        assertEquals(List.of(new Shards.SubWindow(0, 0, 100, 0, 100)), shards.decompose(0, 100),
            "a window ending on a seam produces no trailing no-op");
        assertEquals(List.of(new Shards.SubWindow(2, 40, 50, 40, 50)), shards.decompose(240, 999), "clamped to the series");
        assertTrue(shards.decompose(250, 260).isEmpty());
        assertTrue(shards.decompose(20, 10).isEmpty());
    }

    @Test void aZeroLengthEntryIsNotAShard() {
        assertNull(Shards.of(List.of(entry(10), entry(0))));
        assertNull(Shards.of(List.of()));
    }

    @Test void sourceStringsCarryWindowsAndCounts() {
        SourceSpec bare = SourceSpec.parse("a.u8");
        assertEquals("a.u8", bare.path()); assertTrue(bare.window().isEmpty()); assertNull(bare.declaredCount());
        SourceSpec counted = SourceSpec.parse("corpus-part-a.u8=4194304");
        assertEquals("corpus-part-a.u8", counted.path()); assertEquals(4_194_304L, counted.declaredCount());
        SourceSpec sliced = SourceSpec.parse("a.u8[0..1M]=1M");
        assertEquals("a.u8", sliced.path()); assertEquals(1_000_000L, sliced.declaredCount());
        assertEquals(new DSWindow.Interval(0, 1_000_000), sliced.window().intervals().get(0));
        SourceSpec query = SourceSpec.parse("https://h/f.fvec?token=12345");
        assertEquals("https://h/f.fvec?token=12345", query.path(), "a query string is never split on '='");
        assertNull(query.declaredCount());
        SourceSpec weird = SourceSpec.parse("weird=name.u8");
        assertEquals("weird=name.u8", weird.path(), "an '=' whose tail is not a count stays in the path");
        for (String raw : new String[] {"a.u8", "a.u8[0..100]", "a.u8=500", "a.u8[0..1000000]=1000000", "https://h/f.fvec", "b__0000.fvec"})
            assertEquals(raw, SourceSpec.parse(raw).render(), "round trip of " + raw);
    }

    @Test void sourceLocatorsCarryANamespace() {
        SourceSpec plain = SourceSpec.parse("m.slab:content");
        assertEquals("m.slab", plain.path()); assertEquals("content", plain.namespace());
        SourceSpec windowed = SourceSpec.parse("m.slab:ns:[0..1K]");
        assertEquals("m.slab", windowed.path()); assertEquals("ns", windowed.namespace());
        assertEquals(new DSWindow.Interval(0, 1000), windowed.window().intervals().get(0));
        assertEquals("m.slab:ns[0..1K]", windowed.render(), "rendered once, without the separator colon");
        assertEquals(windowed, SourceSpec.parse(windowed.render()), "and the rendering parses back to itself");
        assertNull(SourceSpec.parse("https://h/f.fvec").namespace(), "a URL scheme is not a namespace");
        assertEquals("ns", SourceSpec.parse("https://h/f.slab:ns").namespace());
        assertNull(SourceSpec.parse("C:\\data\\x.fvec").namespace(), "nor is a drive letter");
        assertEquals("m.slab:ns", SourceSpec.parse("m.slab:ns").locator());
        assertEquals("m.slab:ns", new Shards.Entry("m.slab", "ns", 0, 1).locator(), "a shard entry addresses its file the same way");
        assertEquals("m.slab", new Shards.Entry("m.slab", 0, 1).locator());
    }

    @Test void shardNamesAreFourDigitsExactly() {
        assertEquals("base__0007.fvec", Shards.shardFilename("base__NNNN.fvec", 7));
        assertEquals("base__0000.fvec", Shards.shardFilename("base__NNNN.fvec", 0));
        assertTrue(Shards.hasShardField("x/base__NNNN.fvec"));
        assertFalse(Shards.hasShardField("base__0007.fvec"));
        assertEquals("1234", Shards.ambiguousTokenBeforeShardField("p__1234__NNNN.fvec"));
        assertNull(Shards.ambiguousTokenBeforeShardField("p__abc__NNNN.fvec"));
        assertNull(Shards.ambiguousTokenBeforeShardField("base__NNNN.fvec"));
    }

    @Test void realizationChecksWhatItCanWithoutAFile() {
        Shards.Declaration remoteBare = new Shards.Declaration(List.of("http://h/a.fvec", "http://h/b.fvec"), true, null, null, 2L);
        assertThrows(VectorDataException.class, () -> Shards.validate("base", remoteBare));
        Shards.Declaration localBare = new Shards.Declaration(List.of("a.fvec", "b.fvec"), true, null, null, 2L);
        assertDoesNotThrow(() -> Shards.validate("base", localBare), "a local bare entry is deferred to open, not a fault");
        Shards.Declaration single = new Shards.Declaration(List.of("base.fvec"), false, null, null, null);
        assertDoesNotThrow(() -> Shards.validate("base", single));
        Shards.Declaration uniform = new Shards.Declaration(List.of("b__NNNN.fvec"), false, 100L, 3, 250L);
        Shards realized = Shards.realize("base", uniform, source -> { throw new AssertionError("a uniform series opens nothing"); });
        assertEquals(List.of(new Shards.Entry("b__0000.fvec", 0, 100), new Shards.Entry("b__0001.fvec", 0, 100), new Shards.Entry("b__0002.fvec", 0, 50)),
            realized.entries());
        VectorDataException short_ = assertThrows(VectorDataException.class,
            () -> Shards.realize("base", new Shards.Declaration(List.of("b__NNNN.fvec"), false, 100L, 3, 200L), s -> 0));
        assertTrue(short_.getMessage().contains("record_count"), "a total that leaves the last shard empty disagrees with itself: " + short_.getMessage());
    }

    @Test void twoShardsOfOneFileShareOneStorage() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("shared"));
        ByteBuffer values = ByteBuffer.allocate(10_000 * 4).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < 10_000; i++) values.putInt(i);
        Files.write(dir.resolve("corpus.u32"), values.array());
        String file = dir.resolve("corpus.u32").toUri().toString();
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        Shards shards = Shards.realize("layout",
            new Shards.Declaration(List.of(file + "[0..10]=10", file + "[9990..10000]=10"), true, null, null, 20L), s -> 0);
        FacetSeries series = new FacetSeries(shards, settings, "shared");
        assertEquals(2, shards.shardCount(), "two shards");
        assertEquals(1, series.fileCount(), "one file");
        assertEquals(series.fileIndexOfShard(0), series.fileIndexOfShard(1));
        assertSame(series.file(0), series.file(series.fileIndexOfShard(1)), "and one storage between them");
        assertArrayEquals(new long[] {0, 40}, series.shardByteExtent(0), "each shard addresses its own window of the file");
        assertArrayEquals(new long[] {9990 * 4, 10_000 * 4}, series.shardByteExtent(1));
        assertEquals(10_000 * 4, series.tryTotalSize(), "the file is counted once");
    }

    @Test void theOpenFileCapIsDerivedAndLeavesHeadroom() {
        assertEquals(256, FacetSeries.resolveOpenFileCap(null, 1024L), "a quarter of the soft limit");
        assertEquals(256, FacetSeries.resolveOpenFileCap(null, null), "1024 when the limit is unknown");
        assertEquals(8, FacetSeries.resolveOpenFileCap(null, 8L), "never below the floor");
        assertEquals(3, FacetSeries.resolveOpenFileCap("3", 1024L), "the environment overrides");
        assertEquals(256, FacetSeries.resolveOpenFileCap("nonsense", 1024L));
        assertTrue(FacetSeries.openFileCap() >= 8);
    }

    @Test void aSeriesWiderThanTheDescriptorBudgetStillReads() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("wide"));
        for (int shard = 0; shard < 5; shard++) {
            ByteBuffer bytes = ByteBuffer.allocate(10 * (4 + 4)).order(ByteOrder.LITTLE_ENDIAN);
            for (int r = 0; r < 10; r++) { bytes.putInt(1); bytes.putFloat(shard * 10 + r); }
            Files.write(dir.resolve(String.format("w__%04d.fvec", shard)), bytes.array());
        }
        VectorDataSettings settings = VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
        Shards shards = Shards.realize("w", new Shards.Declaration(List.of(dir.toUri() + "w__NNNN.fvec"), false, 10L, 5, 50L), s -> 0);
        FacetSeries series = new FacetSeries(shards, settings, "wide", 2);
        VectorReader<float[]> reader = new ShardedVectorReader<>(series);
        assertEquals(50, reader.count());
        for (int i = 0; i < 50; i++) assertEquals((float) i, reader.get(i)[0], "record " + i);
        for (int i = 49; i >= 0; i--) assertEquals((float) i, reader.get(i)[0], "record " + i + " backwards");
        assertTrue(series.openFileCount() <= 2, "the series holds no more files than its budget: " + series.openFileCount());
        assertEquals(50 * 8, series.tryTotalSize());
    }
}
