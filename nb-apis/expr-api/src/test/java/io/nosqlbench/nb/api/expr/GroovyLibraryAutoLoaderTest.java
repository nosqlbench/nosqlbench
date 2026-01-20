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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Tag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Groovy library auto-loading functionality.
 * Verifies that Groovy scripts marked with @Library are automatically loaded
 * and their functions become available in expressions.
 */
@Tag("unit")
class GroovyLibraryAutoLoaderTest {

    @Test
    void shouldLoadLibraryFunctionsFromDirectory(@TempDir Path tempDir) throws IOException {
        // Create lib/groovy directory structure
        Path libGroovyDir = tempDir.resolve("lib/groovy");
        Files.createDirectories(libGroovyDir);

        // Create test library file
        Path testLibrary = libGroovyDir.resolve("test_library.groovy");
        Files.writeString(testLibrary, """
            /*
             * @Library
             * Test Groovy library for expression auto-loading.
             */

            // Define functions as closures assigned to variables
            greet = { name -> "Hello, ${name}!" }
            multiply = { a, b -> a * b }

            class Calculator {
                static int add(int a, int b) {
                    return a + b
                }
            }
            """);

        // Create math helpers library
        Path mathHelpers = libGroovyDir.resolve("math_helpers.groovy");
        Files.writeString(mathHelpers, """
            /*
             * @Library
             * Mathematical helper functions.
             */

            // Define function as closure
            square = { n -> n * n }
            """);

        // Change working directory for the test
        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());

            GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

            // Test that library functions are available
            String template = "{{= greet('World') }}";
            String result = processor.process(template, null, Map.of());
            assertEquals("Hello, World!", result);

            // Test math helper function
            template = "{{= square(5) }}";
            result = processor.process(template, null, Map.of());
            assertEquals("25", result);

            // Test Calculator class from library
            template = "{{= Calculator.add(10, 20) }}";
            result = processor.process(template, null, Map.of());
            assertEquals("30", result);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void shouldNotLoadNonLibraryFiles(@TempDir Path tempDir) throws IOException {
        // Create lib/groovy directory
        Path libGroovyDir = tempDir.resolve("lib/groovy");
        Files.createDirectories(libGroovyDir);

        // Create a file WITHOUT the library marker
        Path nonLibrary = libGroovyDir.resolve("not_a_library.groovy");
        Files.writeString(nonLibrary, """
            /*
             * This file should NOT be auto-loaded because it lacks the proper marker.
             */

            shouldNotBeLoaded = { -> "This should not be available" }
            """);

        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());

            GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

            // This function should NOT be available because not_a_library.groovy
            // lacks the proper marker
            String template = "{{= shouldNotBeLoaded() }}";

            assertThrows(Exception.class, () -> processor.process(template, null, Map.of()));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void shouldHandleMultipleLibraries(@TempDir Path tempDir) throws IOException {
        // Create lib/groovy directory
        Path libGroovyDir = tempDir.resolve("lib/groovy");
        Files.createDirectories(libGroovyDir);

        // Create first library
        Files.writeString(libGroovyDir.resolve("lib1.groovy"), """
            /* @Library */
            greet = { name -> "Hello, ${name}!" }
            multiply = { a, b -> a * b }
            """);

        // Create second library
        Files.writeString(libGroovyDir.resolve("lib2.groovy"), """
            /* @Library */
            square = { n -> n * n }
            """);

        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());

            GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

            // Use functions from different libraries in same expression
            String template = """
                {{=
                def greeting = greet('Test')
                def squared = square(4)
                def product = multiply(3, 7)
                return "${greeting} ${squared} ${product}"
                }}
                """;

            String result = processor.process(template, null, Map.of());
            assertTrue(result.contains("Hello, Test!"));
            assertTrue(result.contains("16"));
            assertTrue(result.contains("21"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void shouldHandleMissingLibraryDirectory(@TempDir Path tempDir) throws IOException {
        // Create a temp directory without lib/groovy/
        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());

            // Should not throw exception, just log that directory doesn't exist
            GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

            // Basic expression should still work
            String template = "{{= 1 + 1 }}";
            String result = processor.process(template, null, Map.of());
            assertEquals("2", result);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void shouldUseLibraryFunctionsInComplexExpressions(@TempDir Path tempDir) throws IOException {
        // Create lib/groovy directory
        Path libGroovyDir = tempDir.resolve("lib/groovy");
        Files.createDirectories(libGroovyDir);

        // Create library with multiple functions and a class
        Files.writeString(libGroovyDir.resolve("complete_lib.groovy"), """
            /* @Library */
            sum = { numbers -> numbers.sum() }
            square = { n -> n * n }

            class Calculator {
                static int subtract(int a, int b) { return a - b }
            }
            """);

        String originalDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tempDir.toString());

            GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

            // Complex expression using library functions
            String template = """
                {{=
                def numbers = [1, 2, 3, 4, 5]
                def total = sum(numbers)
                def squared_total = square(total)
                def result = Calculator.subtract(squared_total, 100)
                return "Total: ${total}, Squared: ${squared_total}, Result: ${result}"
                }}
                """;

            String result = processor.process(template, null, Map.of());
            assertTrue(result.contains("Total: 15"));
            assertTrue(result.contains("Squared: 225"));
            assertTrue(result.contains("Result: 125"));
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
}
