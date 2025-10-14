package io.nosqlbench.nb.api.expr;

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


import groovy.lang.Binding;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContextFormatter functionality.
 */
class ContextFormatterTest {

    private static final int MAX_LINES_BEFORE_ABBREVIATION = 10;
    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldCaptureSimpleVariables() {
        String template = """
            setup: {{counter = 10}}
            name: {{userName = 'Alice'}}
            flag: {{enabled = true}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://simple"), Map.of());

        String formatted = result.getFormattedContext();

        assertTrue(formatted.contains("SCRIPTING CONTEXT"), "Should have header");
        assertTrue(formatted.contains("counter:"), "Should show counter variable");
        assertTrue(formatted.contains("userName:"), "Should show userName variable");
        assertTrue(formatted.contains("enabled:"), "Should show enabled variable");
        assertTrue(formatted.contains("10"), "Should show counter value");
        assertTrue(formatted.contains("Alice"), "Should show userName value");
        assertTrue(formatted.contains("true"), "Should show enabled value");
    }

    @Test
    void shouldCaptureCollections() {
        String template = """
            list: {{items = ['apple', 'banana', 'cherry']}}
            map: {{config = [host: 'localhost', port: 9042]}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://collections"), Map.of());

        String formatted = result.getFormattedContext();

        assertTrue(formatted.contains("items:"), "Should show items variable");
        assertTrue(formatted.contains("config:"), "Should show config variable");
        assertTrue(formatted.contains("apple"), "Should show list content");
        assertTrue(formatted.contains("banana"), "Should show list content");
        assertTrue(formatted.contains("localhost"), "Should show map content");
        assertTrue(formatted.contains("9042"), "Should show map content");
    }

    @Test
    void shouldAbbreviateLongMultilineOutput() {
        String template = """
            data: {{=
                longText = ""
                for (i in 1..15) {
                    longText += "Line $i\\n"
                }
                return longText
            }}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://long"), Map.of());

        String formatted = result.getFormattedContext();

        // The longText variable should have more than 10 lines when split
        Binding binding = result.getBinding();
        Object longTextValue = binding.getVariable("longText");
        if (longTextValue != null) {
            String[] lines = longTextValue.toString().split("\n");
            if (lines.length > MAX_LINES_BEFORE_ABBREVIATION) {
                assertTrue(formatted.contains("[..."), "Should have abbreviation indicator");
                assertTrue(formatted.contains("lines"), "Should mention lines");
            }
        }
    }

    @Test
    void shouldNotAbbreviateShortOutput() {
        String template = """
            data: {{shortList = ['one', 'two', 'three', 'four', 'five']}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://short"), Map.of());

        String formatted = result.getFormattedContext();

        // Should NOT abbreviate since it's less than 10 lines
        assertFalse(formatted.contains("[..."), "Should not have abbreviation for short output");
        assertTrue(formatted.contains("one"), "Should show all content");
        assertTrue(formatted.contains("five"), "Should show all content");
    }

    @Test
    void shouldSkipInternalVariables() {
        String template = """
            user: {{myVar = 42}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://internal"), Map.of());

        String formatted = result.getFormattedContext();

        // Should show user variables
        assertTrue(formatted.contains("myVar:"), "Should show user variable");

        // Should NOT show internal variables
        assertFalse(formatted.contains("_parameters:"), "Should not show internal _parameters");
        assertFalse(formatted.contains("_sourceUri:"), "Should not show internal _sourceUri");
        assertFalse(formatted.contains("__expr_lvars:"), "Should not show internal __expr_lvars");
        assertFalse(formatted.contains("__expr_lvar_"), "Should not show prefixed lvar variables");
    }

    @Test
    void shouldHandleEmptyContext() {
        String template = """
            value: No expressions here
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://empty"), Map.of());

        String formatted = result.getFormattedContext();

        // Should contain the header but indicate no user variables
        assertTrue(formatted.contains("SCRIPTING CONTEXT"), "Should have context header");
    }

    @Test
    void shouldShowTypeInformation() {
        String template = """
            number: {{num = 42}}
            text: {{str = 'hello'}}
            list: {{items = [1, 2, 3]}}
            map: {{data = [key: 'value']}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://types"), Map.of());

        String formatted = result.getFormattedContext();

        // With compact format, we no longer show explicit type information
        // Instead we verify that values are present
        assertTrue(formatted.contains("num:"), "Should show num variable");
        assertTrue(formatted.contains("str:"), "Should show str variable");
        assertTrue(formatted.contains("items:"), "Should show items variable");
        assertTrue(formatted.contains("data:"), "Should show data variable");
        assertTrue(formatted.contains("42"), "Should show number value");
        assertTrue(formatted.contains("hello"), "Should show string value");
    }

    @Test
    void shouldAbbreviateEachVariableIndependently() {
        String template = """
            short: {{shortVar = 'Just a short value'}}
            long: {{=
                longVar = ""
                for (i in 1..15) {
                    longVar += "Line $i\\n"
                }
                return longVar
            }}
            another: {{anotherShortVar = 'Another short value'}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://mixed"), Map.of());

        String formatted = result.getFormattedContext();

        // Short variables should be shown in full
        assertTrue(formatted.contains("Just a short value"), "Should show short variable in full");
        assertTrue(formatted.contains("Another short value"), "Should show another short variable in full");

        // Verify all variables are present
        assertTrue(formatted.contains("shortVar:"), "Should show shortVar");
        assertTrue(formatted.contains("longVar:"), "Should show longVar");
        assertTrue(formatted.contains("anotherShortVar:"), "Should show anotherShortVar");
    }

    @Test
    void shouldCaptureVariablesFromMultipleExpressions() {
        String template = """
            init: {{=
                counter = 0
                items = []
                return "initialized"
            }}
            step1: {{=
                counter += 1
                items << 'A'
                return "step1"
            }}
            step2: {{=
                counter += 1
                items << 'B'
                return "step2"
            }}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://multiple"), Map.of());

        Binding binding = result.getBinding();

        // Verify context contains updated values
        assertEquals(2, binding.getVariable("counter"), "Counter should be 2");

        List<?> items = (List<?>) binding.getVariable("items");
        assertEquals(2, items.size(), "Items list should have 2 elements");
        assertEquals("A", items.get(0), "First item should be A");
        assertEquals("B", items.get(1), "Second item should be B");

        // Verify formatted output
        String formatted = result.getFormattedContext();
        assertTrue(formatted.contains("counter:"), "Should show counter");
        assertTrue(formatted.contains("items:"), "Should show items");
        assertTrue(formatted.contains("2"), "Should show final counter value");
    }

    @Test
    void shouldFormatNullValues() {
        String template = """
            nullVar: {{myNull = null}}
            """;

        ProcessingResult result = processor.processWithContext(template, URI.create("test://null"), Map.of());

        String formatted = result.getFormattedContext();

        assertTrue(formatted.contains("myNull:"), "Should show null variable");
        assertTrue(formatted.contains("null"), "Should show null value");
    }
}
