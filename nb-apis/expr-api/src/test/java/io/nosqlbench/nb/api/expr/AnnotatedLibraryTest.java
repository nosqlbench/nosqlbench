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
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for annotated library functions in Groovy scripts.
 */
@Tag("unit")
public class AnnotatedLibraryTest {

    @Test
    public void testAnnotatedLibraryFunctionsWork() {
        GroovyLibraryAutoLoader loader = new GroovyLibraryAutoLoader();
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());

        // Load libraries
        loader.loadLibrariesFromPathWithShell(shell, "src/test/resources/lib/groovy");

        // Test that functions work
        Object multiplyResult = shell.evaluate("multiply(2, 3)");
        assertThat(multiplyResult).isEqualTo(6);

        Object addResult = shell.evaluate("add(10, 20)");
        assertThat(addResult).isEqualTo(30);

        Object squareResult = shell.evaluate("square(5)");
        assertThat(squareResult).isEqualTo(25);
    }

    @Test
    public void testMetadataIsExtracted() {
        GroovyLibraryAutoLoader loader = new GroovyLibraryAutoLoader();
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());

        // Load libraries
        loader.loadLibrariesFromPathWithShell(shell, "src/test/resources/lib/groovy");

        // Check that metadata was extracted
        Map<String, ExprFunctionMetadata> metadata = loader.getLibraryMetadata();

        assertThat(metadata).containsKey("multiply");
        assertThat(metadata).containsKey("add");
        assertThat(metadata).containsKey("square");

        ExprFunctionMetadata multiplyMeta = metadata.get("multiply");
        assertThat(multiplyMeta.name()).isEqualTo("multiply");
        assertThat(multiplyMeta.synopsis()).isEqualTo("multiply(a, b)");
        assertThat(multiplyMeta.description()).isEqualTo("Multiplies two numbers and returns the result");
        assertThat(multiplyMeta.provider()).contains("test_annotated_library.groovy");
        assertThat(multiplyMeta.examples()).hasSize(2);
    }

    @Test
    public void testMethodBasedCollectionFunctions() {
        GroovyLibraryAutoLoader loader = new GroovyLibraryAutoLoader();
        Binding binding = new Binding();
        GroovyShell shell = new GroovyShell(binding, new CompilerConfiguration());

        // Load libraries
        loader.loadLibrariesFromPathWithShell(shell, "src/main/resources/lib/groovy");

        // Test collection functions
        Object takeResult = shell.evaluate("take([1, 2, 3, 4, 5], 3)");
        assertThat(takeResult.toString()).isEqualTo("[1, 2, 3]");

        Object uniqueResult = shell.evaluate("unique([1, 2, 2, 3, 3, 3])");
        assertThat(uniqueResult.toString()).isEqualTo("[1, 2, 3]");
    }

    @Test
    public void testMetadataInExpressionProcessor() {
        GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

        String template = "Result: {{= multiply(6, 7) }}";
        String result = processor.process(template, null, Map.of());

        assertThat(result).isEqualTo("Result: 42");

        // Test that metadata is available
        ProcessingResult processingResult = processor.processWithContext(template, null, Map.of());
        Binding binding = processingResult.getBinding();

        @SuppressWarnings("unchecked")
        Map<String, ExprFunctionMetadata> metadata =
            (Map<String, ExprFunctionMetadata>) binding.getVariable("__expr_function_metadata");

        assertThat(metadata).isNotNull();
        // Should contain library functions plus core functions
        assertThat(metadata).containsKey("multiply");
        assertThat(metadata).containsKey("env"); // from CoreExprFunctionsProvider
    }
}
