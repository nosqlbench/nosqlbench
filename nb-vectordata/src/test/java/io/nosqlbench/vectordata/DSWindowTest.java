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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class DSWindowTest {
    private static DSWindow.Interval only(String spec) {
        DSWindow window = DSWindow.parse(spec);
        assertEquals(1, window.intervals().size(), spec);
        return window.intervals().get(0);
    }

    @Test void parsesTheDocumentedIntervalForms() {
        assertEquals(new DSWindow.Interval(0, 1_000_000), only("1M"));
        assertEquals(new DSWindow.Interval(0, 1000), only("0..1000"));
        assertEquals(new DSWindow.Interval(0, 1000), only("[0..1000)"));
        assertEquals(new DSWindow.Interval(0, 1000), only("0..1K"));
        assertEquals(new DSWindow.Interval(10_000, Long.MAX_VALUE), only("[10k..]"));
        // A matched outer [..] pair is structural at the window level and
        // stripped before interval semantics apply, as in parse_window.
        assertEquals(new DSWindow.Interval(0, 10_000), only("[..10k]"));
        assertEquals(new DSWindow.Interval(0, 10_000), only("[..10k)"));
        assertEquals(new DSWindow.Interval(10_001, Long.MAX_VALUE), only("(10k..]"));
        assertEquals(new DSWindow.Interval(1, 11), only("(0..10]"));
        assertEquals(new DSWindow.Interval(0, Long.MAX_VALUE), only("[..]"));
    }

    @Test void parsesSuffixesUnderscoresAndCompounds() {
        assertEquals(1_000, DSWindow.parseNumberWithSuffix("1_0_00"));
        assertEquals(1L << 20, DSWindow.parseNumberWithSuffix("1MiB"));
        assertEquals(1_000_000, DSWindow.parseNumberWithSuffix("1MB"));
        assertEquals(1L << 30, DSWindow.parseNumberWithSuffix("1gi"));
        assertEquals(1_000_000_000L, DSWindow.parseNumberWithSuffix("1b"));
        assertEquals(1_000_000_000_000L, DSWindow.parseNumberWithSuffix("1T"));
        assertEquals(1_024_000_000L, DSWindow.parseNumberWithSuffix("1g24m"));
    }

    @Test void parsesMultiIntervalWindows() {
        DSWindow window = DSWindow.parse("[0..1K, 2K..3K]");
        assertEquals(List.of(new DSWindow.Interval(0, 1000), new DSWindow.Interval(2000, 3000)), window.intervals());
        assertTrue(DSWindow.parse("  ").isEmpty());
        assertEquals("ALL", DSWindow.ALL.toString());
    }

    @Test void rejectsIntervalsThatSelectNoRecords() {
        // A comma where '..' belongs is the mistake this exists to catch:
        // '0,1000' parses as two intervals and the first selects nothing.
        VectorDataException comma = assertThrows(VectorDataException.class, () -> DSWindow.parse("0,1000"));
        assertTrue(comma.getMessage().contains(".."), comma.getMessage());
        assertThrows(VectorDataException.class, () -> DSWindow.parse("5..5"));
        VectorDataException reversed = assertThrows(VectorDataException.class, () -> DSWindow.parse("10..5"));
        assertTrue(reversed.getMessage().contains("must exceed"), reversed.getMessage());
        assertThrows(VectorDataException.class, () -> DSWindow.parse("abc"));
    }
}
