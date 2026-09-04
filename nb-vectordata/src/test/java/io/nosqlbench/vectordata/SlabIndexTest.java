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

import io.nosqlbench.vectordata.internal.MappedStorage;
import io.nosqlbench.vectordata.internal.SlabIndex;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/// The slab ordinal index decides which page holds a record, and every
/// incremental read and every plan depends on it being right. Mirrors
/// the reference's index tests: every ordinal, not a sample, since a
/// seam is exactly where an off-by-one hides.
@Tag("unit")
class SlabIndexTest {
    @TempDir Path temporary;

    private SlabIndex index(Path file, String namespace) {
        return SlabIndex.read(new MappedStorage(file), namespace, file.getFileName().toString());
    }

    @Test void theIndexIsReadFromTheTail() throws Exception {
        Path file = FixtureSupport.slab(temporary, "m.slab", 600, 50, 64);
        long fileLen = Files.size(file);
        SlabIndex index = index(file, null);
        assertEquals(600, index.total());
        assertEquals(12, index.pageCount(), "the fixture must span pages");
        assertTrue(index.prerequisiteBytes() > 0);
        assertTrue(index.prerequisiteBytes() < fileLen / 4,
            "reading the index must not amount to reading the file: " + index.prerequisiteBytes() + " of " + fileLen);
    }

    @Test void everyOrdinalLandsInAPageThatContainsIt() throws Exception {
        SlabIndex index = index(FixtureSupport.slab(temporary, "m.slab", 400, 32, 48), null);
        for (long o = 0; o < index.total(); o++) {
            Integer page = index.pageOf(o);
            assertNotNull(page, "ordinal " + o);
            assertTrue(index.pageStartOrdinal(page) <= o, "page " + page + " starts after ordinal " + o);
            Long next = index.pageStartOrdinal(page + 1);
            if (next != null) assertTrue(o < next, "ordinal " + o + " belongs to page " + (page + 1));
        }
    }

    @Test void pageSeamsDivideWithoutAGap() throws Exception {
        SlabIndex index = index(FixtureSupport.slab(temporary, "m.slab", 400, 32, 48), null);
        assertTrue(index.pageCount() >= 2);
        for (int page = 1; page < index.pageCount(); page++) {
            long first = index.pageStartOrdinal(page);
            assertEquals(page, index.pageOf(first), "first ordinal of page " + page);
            assertEquals(page - 1, index.pageOf(first - 1), "the ordinal before it belongs to the previous page");
        }
    }

    @Test void anOrdinalPastTheEndDoesNotClamp() throws Exception {
        SlabIndex index = index(FixtureSupport.slab(temporary, "m.slab", 50, 8, 32), null);
        assertNotNull(index.pageOf(49));
        assertNull(index.pageOf(50), "one past the end");
        assertNull(index.pageOf(Long.MAX_VALUE));
        assertNull(index.pageOf(-1));
        assertNull(index.pageOffset(index.pageCount()));
        assertNull(index.pageStartOrdinal(index.pageCount()));
    }

    @Test void pagesAreLaidOutInOrdinalOrder() throws Exception {
        SlabIndex index = index(FixtureSupport.slab(temporary, "m.slab", 400, 32, 48), null);
        long last = 0;
        for (int page = 0; page < index.pageCount(); page++) {
            long at = index.pageOffset(page);
            assertTrue(at >= last, "page " + page + " starts before its predecessor");
            last = at;
        }
    }

    @Test void anAbsentNamespaceReadsAsAbsent() throws Exception {
        assertNull(index(FixtureSupport.slab(temporary, "single.slab", 4, 4, 32), "layout"), "an absent namespace is a normal state");
        Path multi = FixtureSupport.slabWithNamespace(temporary, "multi.slab", 30, "layout", 5, 8, 32);
        assertNull(index(multi, "nope"));
    }

    @Test void eachNamespaceHasItsOwnIndex() throws Exception {
        Path multi = FixtureSupport.slabWithNamespace(temporary, "multi.slab", 30, "layout", 5, 8, 32);
        assertEquals(30, index(multi, null).total(), "no namespace is the default one");
        assertEquals(30, index(multi, "").total());
        SlabIndex layout = index(multi, "layout");
        assertEquals(5, layout.total());
        assertEquals(1, layout.pageCount());
        assertTrue(layout.prerequisiteBytes() > index(FixtureSupport.slab(temporary, "single.slab", 5, 8, 32), null).prerequisiteBytes(),
            "reaching a pages page through the namespaces page is one more read, and the index says so");
    }

    @Test void aFileThatIsNotASlabIsRefused() throws Exception {
        Path file = temporary.resolve("not.slab");
        Files.writeString(file, "this is not a slab file at all, truly");
        VectorDataException refused = assertThrows(VectorDataException.class, () -> index(file, null));
        assertTrue(refused.getMessage().startsWith("not.slab: "), "the failure names the file: " + refused.getMessage());
    }
}
