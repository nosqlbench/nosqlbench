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

import groovy.lang.Binding;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple unit tests for Groovy library loading functionality.
 */
@Tag("unit")
class SimpleLibraryLoadingTest {

    @Test
    void testLibraryLoadingDirectly(@TempDir Path tempDir) throws IOException {
        // Create a library file with closure-based functions
        Path libraryFile = tempDir.resolve("test_lib.groovy");
        Files.writeString(libraryFile, """
            /* @Library */
            // Define functions as closures assigned to variables
            testFunc = { name -> "Hello from library, ${name}!" }
            """);

        // Create a GroovyShell and load the library
        groovy.lang.Binding binding = new groovy.lang.Binding();
        groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell(binding, new CompilerConfiguration());
        GroovyLibraryAutoLoader loader = new GroovyLibraryAutoLoader();
        loader.loadLibrariesFromPathWithShell(shell, tempDir.toString());

        // Test that the function can be called in a subsequent script evaluation
        Object result = shell.evaluate("testFunc('World')");
        assertEquals("Hello from library, World!", result.toString());
    }

    @Test
    void testIntegrationWithProcessor(@TempDir Path tempDir) throws IOException {
        // Create lib/groovy directory
        Path libGroovyDir = tempDir.resolve("lib/groovy");
        Files.createDirectories(libGroovyDir);

        // Create a library file with closure-based function
        Files.writeString(libGroovyDir.resolve("greeting_lib.groovy"), """
            /* @Library */
            // Define function as closure
            sayHello = { name -> "Greetings, ${name}!" }
            """);

        // Save and restore working directory
        String originalDir = System.getProperty("user.dir");
        try {
            // Set working directory to temp
            System.setProperty("user.dir", tempDir.toString());

            // Create processor - should auto-load libraries
            GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

            // Use the library function in an expression
            String result = processor.process("{{= sayHello('Test') }}", null, java.util.Map.of());
            assertEquals("Greetings, Test!", result);
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }
}
