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

import static org.junit.jupiter.api.Assertions.*;

/// A window on a slab facet prices at the pages it spans, through the
/// index the slab carries in its tail. Mirrors the reference's slab
/// planner tests: bounds rather than exact byte counts, since page
/// packing is the writer's business.
@Tag("unit")
class SlabWindowsTest {
    @TempDir Path temporary;

    private VectorDataSettings settings() {
        return VectorDataSettings.builder().cacheDirectory(temporary.resolve("cache")).build();
    }

    /// A slab of `records` records, fifty to a 5 KiB-ish page, beside a
    /// one-record base.
    private Path pagedFacet(String name, int records, String extraYaml) throws IOException {
        Path dir = Files.createDirectories(temporary.resolve(name));
        FixtureSupport.slab(dir, "metadata_content.slab", records, 50, 100);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), "name: paged\nprofiles:\n  default:\n    base_vectors: base.fvec\n"
            + "    metadata_content: metadata_content.slab\n" + extraYaml);
        return dir;
    }

    private TestDataView view(Path dir, String profile) { return TestDataGroup.load(dir.toUri(), settings()).profile(profile); }

    private static long bytes(PrefetchPlan plan) { return plan.byteRanges().stream().mapToLong(ShardRange::length).sum(); }

    @Test void aSlabWindowPricesAtThePagesItSpans() throws Exception {
        TestDataView view = view(pagedFacet("paged", 2000, ""), "default");
        long total = view.prefetchPlan("metadata_content", DSWindow.ALL).facetBytes();
        assertTrue(total > 40_000, "the fixture must span many pages: " + total);

        PrefetchPlan narrow = view.prefetchPlan("metadata_content", DSWindow.parse("10..20"));
        assertFalse(narrow.degradesToFullDownload(), "a ten-record window must not price as the whole facet");
        long narrowBytes = bytes(narrow);
        assertTrue(narrowBytes > 0 && narrowBytes < total / 4, "a ten-record window cost " + narrowBytes + " of " + total);
        assertTrue(narrow.prerequisiteBytes() > 0 && narrow.prerequisiteBytes() < total / 4, "the tail index is real work, and small");

        PrefetchPlan whole = view.prefetchPlan("metadata_content", DSWindow.parse("0..2000"));
        long wholeBytes = bytes(whole);
        assertTrue(wholeBytes > narrowBytes * 4, "every record costs more than ten: " + wholeBytes + " vs " + narrowBytes);
        assertTrue(wholeBytes <= total);
    }

    @Test void aSlabWindowPastTheEndStopsAtTheLastPage() throws Exception {
        TestDataView view = view(pagedFacet("tail", 500, ""), "default");
        long total = view.prefetchPlan("metadata_content", DSWindow.ALL).facetBytes();
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.parse("490..100000"));
        assertFalse(plan.degradesToFullDownload());
        assertTrue(bytes(plan) > 0, "the tail records are still fetchable");
        assertTrue(bytes(plan) <= total, "and the plan does not run past the file");
        assertEquals(1, plan.byteRanges().size(), "the last page, once");
    }

    @Test void aSlabWindowStartingPastTheEndMapsToNothing() throws Exception {
        TestDataView view = view(pagedFacet("past", 100, ""), "default");
        PrefetchPlan plan = view.prefetchPlan("metadata_content", DSWindow.parse("500..600"));
        assertTrue(plan.degradesToFullDownload(), "an unmappable window degrades rather than fabricating a range");
    }

    /// The case a sized profile over a slab-backed dataset presents: the
    /// metadata facet inherits a `base_count` window, and the
    /// whole-profile prebuffer must map it rather than refuse it.
    @Test void aWindowedSlabFacetIsPrebufferedNotRefused() throws Exception {
        TestDataView small = view(pagedFacet("sized", 2000, "  small:\n    base_count: 100\n"), "small");
        FacetDescriptor facet = small.facet("metadata_content").orElseThrow();
        assertEquals("0..100", facet.window(), "metadata_content inherits under the size window");
        assertDoesNotThrow(() -> small.prebuffer(WholeFacetFallback.REFUSE, PrebufferProgress.NONE));
        PrefetchPlan declared = small.prefetchPlan("metadata_content", DSWindow.parse(facet.window()));
        assertFalse(declared.degradesToFullDownload());
        assertEquals(1, declared.byteRanges().size(), "a hundred records are two pages, one contiguous range");
        assertTrue(bytes(declared) < declared.facetBytes() / 4);
    }

    @Test void aNamespacedLocatorPlansThroughItsOwnIndex() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("ns"));
        FixtureSupport.slabWithNamespace(dir, "m.slab", 200, "layout", 5, 50, 100);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: ns
            profiles:
              default:
                base_vectors: base.fvec
                metadata_content: m.slab
                metadata_layout: m.slab:layout
                custom_layout:
                  source: m.slab
                  namespace: layout
            """);
        TestDataView view = view(dir, "default");
        FacetDescriptor layout = view.facet("metadata_layout").orElseThrow();
        assertEquals("layout", layout.namespace(), "the suffix is the namespace");
        assertTrue(layout.source().toString().endsWith("/m.slab"), "and the file is named without it");
        assertEquals("layout", view.facet("custom_layout").orElseThrow().namespace(), "a namespace beside the source applies to it");

        PrefetchPlan whole = view.prefetchPlan("metadata_content", DSWindow.parse("0..200"));
        PrefetchPlan layoutPlan = view.prefetchPlan("metadata_layout", DSWindow.parse("0..5"));
        PrefetchPlan custom = view.prefetchPlan("custom_layout", DSWindow.parse("0..5"));
        assertFalse(layoutPlan.degradesToFullDownload(), "the layout namespace has its own index");
        assertEquals(layoutPlan.byteRanges(), custom.byteRanges(), "both spellings reach the same pages");
        assertEquals(1, layoutPlan.byteRanges().size());
        assertTrue(bytes(layoutPlan) < bytes(whole) / 10, "five layout records are one small page, not the content's four");
        assertTrue(view.prefetchPlan("metadata_layout", DSWindow.parse("0..200")).byteRanges().get(0).end() <= whole.facetBytes(),
            "a window past the namespace's end stops at its last page");
        assertTrue(layoutPlan.byteRanges().get(0).start() > whole.byteRanges().get(0).start(),
            "the layout pages follow the content pages in the file, so both ranges are in file coordinates");
    }

    @Test void aSlabSeriesPlansAcrossItsSeam() throws Exception {
        Path dir = Files.createDirectories(temporary.resolve("series"));
        FixtureSupport.slab(dir, "m__0000.slab", 100, 25, 64);
        FixtureSupport.slab(dir, "m__0001.slab", 50, 25, 64);
        FixtureSupport.fvec(dir, "base.fvec", new float[][] {{0f, 1f, 2f, 3f}});
        Files.writeString(dir.resolve("dataset.yaml"), """
            name: slab-series
            profiles:
              default:
                base_vectors: base.fvec
                metadata_content:
                  source: m__NNNN.slab
                  shard_stride: 100
                  shard_count: 2
                  record_count: 150
            """);
        PrefetchPlan plan = view(dir, "default").prefetchPlan("metadata_content", DSWindow.parse("90..110"));
        assertFalse(plan.degradesToFullDownload());
        assertEquals(2, plan.byteRanges().size(), "the window spans both shards: " + plan.byteRanges());
        assertEquals(0, plan.byteRanges().get(0).shard());
        assertEquals(1, plan.byteRanges().get(1).shard());
        assertEquals(0, plan.byteRanges().get(1).start(), "records 0..10 of the second shard are its first page");
    }

}
