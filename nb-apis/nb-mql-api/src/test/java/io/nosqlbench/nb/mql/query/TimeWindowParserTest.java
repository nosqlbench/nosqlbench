/*
 * Copyright (c) nosqlbench
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

package io.nosqlbench.nb.mql.query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TimeWindowParserTest {

    @Test
    void testParseSeconds() throws InvalidQueryException {
        assertEquals(5000L, TimeWindowParser.parseToMillis("5s"));
        assertEquals(30000L, TimeWindowParser.parseToMillis("30s"));
        assertEquals(1000L, TimeWindowParser.parseToMillis("1s"));
    }

    @Test
    void testParseMinutes() throws InvalidQueryException {
        assertEquals(60000L, TimeWindowParser.parseToMillis("1m"));
        assertEquals(300000L, TimeWindowParser.parseToMillis("5m"));
        assertEquals(900000L, TimeWindowParser.parseToMillis("15m"));
    }

    @Test
    void testParseHours() throws InvalidQueryException {
        assertEquals(3600000L, TimeWindowParser.parseToMillis("1h"));
        assertEquals(7200000L, TimeWindowParser.parseToMillis("2h"));
        assertEquals(86400000L, TimeWindowParser.parseToMillis("24h"));
    }

    @Test
    void testParseDays() throws InvalidQueryException {
        assertEquals(86400000L, TimeWindowParser.parseToMillis("1d"));
        assertEquals(604800000L, TimeWindowParser.parseToMillis("7d"));
    }

    @Test
    void testParseWeeks() throws InvalidQueryException {
        assertEquals(604800000L, TimeWindowParser.parseToMillis("1w"));
        assertEquals(1209600000L, TimeWindowParser.parseToMillis("2w"));
    }

    @Test
    void testParseYears() throws InvalidQueryException {
        assertEquals(31536000000L, TimeWindowParser.parseToMillis("1y"));
    }

    @Test
    void testParseDecimal() throws InvalidQueryException {
        assertEquals(1500L, TimeWindowParser.parseToMillis("1.5s"));
        assertEquals(90000L, TimeWindowParser.parseToMillis("1.5m"));
    }

    @Test
    void testCaseInsensitive() throws InvalidQueryException {
        assertEquals(5000L, TimeWindowParser.parseToMillis("5S"));
        assertEquals(300000L, TimeWindowParser.parseToMillis("5M"));
        assertEquals(3600000L, TimeWindowParser.parseToMillis("1H"));
    }

    @Test
    void testWhitespace() throws InvalidQueryException {
        assertEquals(5000L, TimeWindowParser.parseToMillis("  5s  "));
        assertEquals(300000L, TimeWindowParser.parseToMillis("\t5m\n"));
    }

    @Test
    void testInvalidFormat() {
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("5"));
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("s"));
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("5x"));
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("five seconds"));
    }

    @Test
    void testNullOrEmpty() {
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis(null));
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis(""));
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("   "));
    }

    @Test
    void testNegativeValue() {
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("-5s"));
    }

    @Test
    void testZeroValue() {
        assertThrows(InvalidQueryException.class, () ->
            TimeWindowParser.parseToMillis("0s"));
    }

    @Test
    void testFormatMillis() {
        assertEquals("5s", TimeWindowParser.formatMillis(5000L));
        assertEquals("5m", TimeWindowParser.formatMillis(300000L));
        assertEquals("1h", TimeWindowParser.formatMillis(3600000L));
        assertEquals("1d", TimeWindowParser.formatMillis(86400000L));
        assertEquals("1w", TimeWindowParser.formatMillis(604800000L));
    }

    @Test
    void testFormatMillisNonExact() {
        // Should fall back to smallest unit that works
        assertEquals("1500ms", TimeWindowParser.formatMillis(1500L));
        assertEquals("90s", TimeWindowParser.formatMillis(90000L));
    }

    @Test
    void testRoundTrip() throws InvalidQueryException {
        String[] inputs = {"5s", "10m", "2h", "1d", "1w"};
        for (String input : inputs) {
            long millis = TimeWindowParser.parseToMillis(input);
            String formatted = TimeWindowParser.formatMillis(millis);
            assertEquals(input, formatted, "Round trip failed for: " + input);
        }
    }
}
