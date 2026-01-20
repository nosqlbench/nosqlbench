/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.nosqlbench.nb.api.expr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for bundled Groovy libraries in src/main/resources/lib/groovy/.
 * These libraries should be automatically loaded and available in expressions.
 */
@Tag("unit")
class BundledLibrariesTest {

    private GroovyExpressionProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new GroovyExpressionProcessor();
    }

    // ========== String Library Tests ==========

    @Test
    void testPadLeftFunction() {
        String result = processor.process("{{= padLeft('42', 5, '0') }}", null, Map.of());
        assertEquals("00042", result);
    }

    @Test
    void testPadRightFunction() {
        String result = processor.process("{{= padRight('test', 10, '.') }}", null, Map.of());
        assertEquals("test......", result);
    }

    @Test
    void testTruncateFunction() {
        String result = processor.process("{{= truncate('very long string', 10, true) }}", null, Map.of());
        assertEquals("very lo...", result);
    }

    @Test
    void testToCamelCase() {
        String result = processor.process("{{= toCamelCase('hello world') }}", null, Map.of());
        assertEquals("helloWorld", result);
    }

    @Test
    void testToSnakeCase() {
        String result = processor.process("{{= toSnakeCase('helloWorld') }}", null, Map.of());
        assertEquals("hello_world", result);
    }

    @Test
    void testToKebabCase() {
        String result = processor.process("{{= toKebabCase('helloWorld') }}", null, Map.of());
        assertEquals("hello-world", result);
    }

    @Test
    void testReverseString() {
        String result = processor.process("{{= reverseString('hello') }}", null, Map.of());
        assertEquals("olleh", result);
    }

    @Test
    void testCountOccurrences() {
        String result = processor.process("{{= countOccurrences('hello world', 'l') }}", null, Map.of());
        assertEquals("3", result);
    }

    @Test
    void testRepeatString() {
        String result = processor.process("{{= repeatString('ab', 3) }}", null, Map.of());
        assertEquals("ababab", result);
    }

    @Test
    void testTitleCase() {
        String result = processor.process("{{= titleCase('hello world') }}", null, Map.of());
        assertEquals("Hello World", result);
    }

    // ========== DateTime Library Tests ==========

    @Test
    void testNowMillis() {
        String result = processor.process("{{= nowMillis() }}", null, Map.of());
        long millis = Long.parseLong(result);
        assertTrue(millis > 0);
    }

    @Test
    void testCurrentDate() {
        String result = processor.process("{{= currentDate() }}", null, Map.of());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testCurrentTime() {
        String result = processor.process("{{= currentTime() }}", null, Map.of());
        assertTrue(result.matches("\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testCurrentDateTime() {
        String result = processor.process("{{= currentDateTime() }}", null, Map.of());
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"));
    }

    @Test
    void testAddDays() {
        String result = processor.process("{{= addDays('2024-01-01', 7) }}", null, Map.of());
        assertEquals("2024-01-08", result);
    }

    @Test
    void testAddMonths() {
        String result = processor.process("{{= addMonths('2024-01-01', 3) }}", null, Map.of());
        assertEquals("2024-04-01", result);
    }

    @Test
    void testDaysBetween() {
        String result = processor.process("{{= daysBetween('2024-01-01', '2024-01-15') }}", null, Map.of());
        assertEquals("14", result);
    }

    @Test
    void testDayOfWeek() {
        String result = processor.process("{{= dayOfWeek('2024-01-01') }}", null, Map.of());
        assertEquals("1", result); // Monday
    }

    @Test
    void testStartOfMonth() {
        String result = processor.process("{{= startOfMonth('2024-01-15') }}", null, Map.of());
        assertEquals("2024-01-01", result);
    }

    @Test
    void testEndOfMonth() {
        String result = processor.process("{{= endOfMonth('2024-01-15') }}", null, Map.of());
        assertEquals("2024-01-31", result);
    }

    @Test
    void testIsLeapYear() {
        String result = processor.process("{{= isLeapYear(2024) }}", null, Map.of());
        assertEquals("true", result);
    }

    // ========== Collections Library Tests ==========

    @Test
    void testTake() {
        String result = processor.process("{{= take([1, 2, 3, 4, 5], 3) }}", null, Map.of());
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    void testTakeLast() {
        String result = processor.process("{{= takeLast([1, 2, 3, 4, 5], 2) }}", null, Map.of());
        assertEquals("[4, 5]", result);
    }

    @Test
    void testSkip() {
        String result = processor.process("{{= skip([1, 2, 3, 4, 5], 2) }}", null, Map.of());
        assertEquals("[3, 4, 5]", result);
    }

    @Test
    void testChunk() {
        String result = processor.process("{{= chunk([1, 2, 3, 4, 5], 2) }}", null, Map.of());
        assertEquals("[[1, 2], [3, 4], [5]]", result);
    }

    @Test
    void testFlattenList() {
        String result = processor.process("{{= flattenList([[1, 2], [3, [4, 5]]]) }}", null, Map.of());
        assertEquals("[1, 2, 3, 4, 5]", result);
    }

    @Test
    void testUnique() {
        String result = processor.process("{{= unique([1, 2, 2, 3, 3, 3]) }}", null, Map.of());
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    void testReverseList() {
        String result = processor.process("{{= reverseList([1, 2, 3]) }}", null, Map.of());
        assertEquals("[3, 2, 1]", result);
    }

    @Test
    void testSortList() {
        String result = processor.process("{{= sortList([3, 1, 4, 1, 5]) }}", null, Map.of());
        assertEquals("[1, 1, 3, 4, 5]", result);
    }

    @Test
    void testIntersect() {
        String result = processor.process("{{= intersect([1, 2, 3], [2, 3, 4]) }}", null, Map.of());
        assertEquals("[2, 3]", result);
    }

    @Test
    void testUnion() {
        String result = processor.process("{{= union([1, 2], [2, 3]) }}", null, Map.of());
        assertEquals("[1, 2, 3]", result);
    }

    @Test
    void testJoinList() {
        String result = processor.process("{{= joinList([1, 2, 3], ', ') }}", null, Map.of());
        assertEquals("1, 2, 3", result);
    }

    @Test
    void testRangeInts() {
        String result = processor.process("{{= rangeInts(1, 5) }}", null, Map.of());
        assertEquals("[1, 2, 3, 4, 5]", result);
    }

    // ========== Math Library Tests ==========

    @Test
    void testSum() {
        String result = processor.process("{{= sum([1, 2, 3, 4, 5]) }}", null, Map.of());
        assertEquals("15", result);
    }

    @Test
    void testAverage() {
        String result = processor.process("{{= average([1, 2, 3, 4, 5]) }}", null, Map.of());
        assertEquals("3", result);
    }

    @Test
    void testMedian() {
        String result = processor.process("{{= median([1, 2, 3, 4, 5]) }}", null, Map.of());
        assertEquals("3", result);
    }

    @Test
    void testMinValue() {
        String result = processor.process("{{= minValue([3, 1, 4, 1, 5]) }}", null, Map.of());
        assertEquals("1", result);
    }

    @Test
    void testMaxValue() {
        String result = processor.process("{{= maxValue([3, 1, 4, 1, 5]) }}", null, Map.of());
        assertEquals("5", result);
    }

    @Test
    void testProduct() {
        String result = processor.process("{{= product([2, 3, 4]) }}", null, Map.of());
        assertEquals("24", result);
    }

    @Test
    void testAbs() {
        String result = processor.process("{{= abs(-5) }}", null, Map.of());
        assertEquals("5", result);
    }

    @Test
    void testRoundTo() {
        String result = processor.process("{{= roundTo(3.14159, 2) }}", null, Map.of());
        assertEquals("3.14", result);
    }

    @Test
    void testPower() {
        String result = processor.process("{{= power(2, 10) }}", null, Map.of());
        assertEquals("1024.0", result);
    }

    @Test
    void testSqrt() {
        String result = processor.process("{{= sqrt(16) }}", null, Map.of());
        assertEquals("4.0", result);
    }

    @Test
    void testFactorial() {
        String result = processor.process("{{= factorial(5) }}", null, Map.of());
        assertEquals("120", result);
    }

    @Test
    void testGcd() {
        String result = processor.process("{{= gcd(48, 18) }}", null, Map.of());
        assertEquals("6", result);
    }

    @Test
    void testLcm() {
        String result = processor.process("{{= lcm(12, 18) }}", null, Map.of());
        assertEquals("36", result);
    }

    @Test
    void testIsPrime() {
        String result = processor.process("{{= isPrime(17) }}", null, Map.of());
        assertEquals("true", result);
    }

    @Test
    void testIsEven() {
        String result = processor.process("{{= isEven(4) }}", null, Map.of());
        assertEquals("true", result);
    }

    @Test
    void testIsOdd() {
        String result = processor.process("{{= isOdd(3) }}", null, Map.of());
        assertEquals("true", result);
    }

    @Test
    void testClamp() {
        String result = processor.process("{{= clamp(15, 1, 10) }}", null, Map.of());
        assertEquals("10", result);
    }

    @Test
    void testPercentage() {
        String result = processor.process("{{= percentage(25, 200) }}", null, Map.of());
        assertEquals("12.500", result);
    }

    @Test
    void testCumulativeSum() {
        String result = processor.process("{{= cumulativeSum([1, 2, 3, 4]) }}", null, Map.of());
        assertEquals("[1, 3, 6, 10]", result);
    }

    // ========== Integration Tests ==========

    @Test
    void testMultipleLibrariesInOneExpression() {
        String result = processor.process("""
            {{=
            def numbers = rangeInts(1, 5)
            def total = sum(numbers)
            def padded = padLeft(total.toString(), 3, '0')
            return padded
            }}
            """, null, Map.of());
        assertEquals("015", result.trim());
    }

    @Test
    void testComplexWorkloadScenario() {
        String template = """
            Date: {{= currentDate() }}
            Time: {{= currentTime() }}
            Week Start: {{= startOfWeek('2024-01-15') }}
            Week End: {{= endOfWeek('2024-01-15') }}
            Total: {{= sum([10, 20, 30]) }}
            """;

        String result = processor.process(template, null, Map.of());
        assertNotNull(result);
        assertTrue(result.contains("Date:"));
        assertTrue(result.contains("Time:"));
        assertTrue(result.contains("Week Start:"));
        assertTrue(result.contains("Week End:"));
        assertTrue(result.contains("Total:"));
    }
}
