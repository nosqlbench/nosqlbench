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


import io.nosqlbench.nb.annotations.Service;
import io.nosqlbench.nb.api.expr.annotations.ExprFunctionSpec;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test function shadowing behavior and provider ordering.
 * When multiple providers define the same function, the last one wins (with a warning).
 */
class DuplicateFunctionDetectionTest {

    /**
     * Provider that registers a function called "testFunc"
     */
    static class FirstProvider implements ExprFunctionProvider {
        @ExprFunctionSpec(
            name = "testFunc",
            synopsis = "testFunc()",
            description = "First implementation of testFunc"
        )
        private Object testFunc(ExprRuntimeContext context, Object[] args) {
            return "first";
        }
    }

    /**
     * Another provider that also tries to register "testFunc"
     */
    static class SecondProvider implements ExprFunctionProvider {
        @ExprFunctionSpec(
            name = "testFunc",
            synopsis = "testFunc()",
            description = "Second implementation of testFunc"
        )
        private Object testFunc(ExprRuntimeContext context, Object[] args) {
            return "second";
        }
    }

    /**
     * Provider with a different function name (no conflict)
     */
    static class ThirdProvider implements ExprFunctionProvider {
        @ExprFunctionSpec(
            name = "otherFunc",
            synopsis = "otherFunc()",
            description = "Different function, no conflict"
        )
        private Object otherFunc(ExprRuntimeContext context, Object[] args) {
            return "other";
        }
    }

    @Test
    void shouldAllowDuplicatesWithLastOneWinning() {
        // When both providers register the same function, second one wins
        GroovyExpressionProcessor processor = new GroovyExpressionProcessor(
            List.of(new FirstProvider(), new SecondProvider()),
            new org.codehaus.groovy.control.CompilerConfiguration()
        );

        String source = "test: {{= testFunc() }}";
        String result = processor.process(source, URI.create("test://duplicate"), Map.of());

        assertTrue(result.contains("test: second"),
            "Second provider should win when registered last");
    }

    @Test
    void shouldRespectProviderOrder() {
        // First provider should win when registered last
        GroovyExpressionProcessor processor = new GroovyExpressionProcessor(
            List.of(new SecondProvider(), new FirstProvider()),
            new org.codehaus.groovy.control.CompilerConfiguration()
        );

        String source = "test: {{= testFunc() }}";
        String result = processor.process(source, URI.create("test://order"), Map.of());

        assertTrue(result.contains("test: first"),
            "First provider should win when registered last");
    }

    @Test
    void shouldAllowDifferentFunctionNames() {
        GroovyExpressionProcessor processor = new GroovyExpressionProcessor(
            List.of(new FirstProvider(), new ThirdProvider()),
            new org.codehaus.groovy.control.CompilerConfiguration()
        );

        String source = "test1: {{= testFunc() }}\ntest2: {{= otherFunc() }}";
        String result = processor.process(source, URI.create("test://nodup"), Map.of());

        assertTrue(result.contains("test1: first"), "Should execute testFunc from FirstProvider");
        assertTrue(result.contains("test2: other"), "Should execute otherFunc from ThirdProvider");
    }

    @Test
    void shouldAllowDuplicatesInSingleProvider() {
        // Last registration wins even within the same provider
        class DuplicateProvider implements ExprFunctionProvider {
            @ExprFunctionSpec(
                name = "duplicate",
                synopsis = "duplicate()",
                description = "First duplicate"
            )
            private Object duplicate1(ExprRuntimeContext context, Object[] args) {
                return "first";
            }

            @ExprFunctionSpec(
                name = "duplicate",
                synopsis = "duplicate()",
                description = "Second duplicate"
            )
            private Object duplicate2(ExprRuntimeContext context, Object[] args) {
                return "second";
            }
        }

        GroovyExpressionProcessor processor = new GroovyExpressionProcessor(
            List.of(new DuplicateProvider()),
            new org.codehaus.groovy.control.CompilerConfiguration()
        );

        String source = "test: {{= duplicate() }}";
        String result = processor.process(source, URI.create("test://sameProvider"), Map.of());

        // Last method registered wins
        assertTrue(result.contains("test: second"),
            "Last registered function should win");
    }

    @Test
    void shouldListAvailableProviders() {
        // List all available providers via ServiceLoader
        List<String> providers = GroovyExpressionProcessor.getAvailableProviderNames();

        assertNotNull(providers, "Provider list should not be null");
        assertFalse(providers.isEmpty(), "Should have at least some providers");

        // Check for known providers (registered via META-INF/services)
        assertTrue(providers.contains("core"), "Should include core provider");
        assertTrue(providers.contains("unified-parameters"), "Should include unified-parameters provider");
        assertTrue(providers.contains("groovy-libraries"), "Should include groovy-libraries provider");
    }

    @Test
    void shouldLoadProvidersInSpecifiedOrder() {
        // Load only specific providers in specified order
        List<ExprFunctionProvider> ordered = GroovyExpressionProcessor.loadProvidersInOrder("core,unified-parameters");

        assertNotNull(ordered, "Ordered list should not be null");
        assertEquals(2, ordered.size(), "Should have exactly 2 providers");

        // Verify order by checking selectors
        String firstSelector = ordered.get(0).getClass().getAnnotation(Service.class).selector();
        String secondSelector = ordered.get(1).getClass().getAnnotation(Service.class).selector();

        assertEquals("core", firstSelector, "First provider should be core");
        assertEquals("unified-parameters", secondSelector, "Second provider should be unified-parameters");
    }

    @Test
    void shouldLoadProvidersFromCommaSeparatedString() {
        // Test reverse order
        List<ExprFunctionProvider> ordered = GroovyExpressionProcessor.loadProvidersInOrder("unified-parameters,core");

        assertNotNull(ordered, "Ordered list should not be null");
        assertEquals(2, ordered.size(), "Should have exactly 2 providers");

        // Verify order is reversed from previous test
        String firstSelector = ordered.get(0).getClass().getAnnotation(Service.class).selector();
        String secondSelector = ordered.get(1).getClass().getAnnotation(Service.class).selector();

        assertEquals("unified-parameters", firstSelector, "First provider should be unified-parameters");
        assertEquals("core", secondSelector, "Second provider should be core");
    }

    @Test
    void shouldGetProviderMetadataWithoutInstantiation() {
        // Get metadata without instantiating providers
        Map<String, Class<?>> metadata = GroovyExpressionProcessor.getProviderMetadata();

        assertNotNull(metadata, "Metadata map should not be null");
        assertFalse(metadata.isEmpty(), "Should have provider metadata");

        // Check for known providers
        assertTrue(metadata.containsKey("core"), "Should have core provider metadata");
        assertTrue(metadata.containsKey("unified-parameters"), "Should have unified-parameters metadata");
        assertTrue(metadata.containsKey("groovy-libraries"), "Should have groovy-libraries metadata");

        // Verify class types without instantiating
        Class<?> coreClass = metadata.get("core");
        assertNotNull(coreClass, "Core provider class should not be null");
        assertTrue(ExprFunctionProvider.class.isAssignableFrom(coreClass),
            "Core provider class should implement ExprFunctionProvider");
    }

    @Test
    void shouldGetFunctionMetadataFromProviderClassWithoutInstantiation() {
        // Get provider metadata first
        Map<String, Class<?>> providers = GroovyExpressionProcessor.getProviderMetadata();
        Class<?> coreProviderClass = providers.get("core");

        assertNotNull(coreProviderClass, "Core provider class should exist");

        // Get function metadata without instantiating the provider
        List<ExprFunctionMetadata> functions =
            GroovyExpressionProcessor.getProviderFunctionMetadata(coreProviderClass);

        assertNotNull(functions, "Functions list should not be null");
        assertFalse(functions.isEmpty(), "Core provider should have functions");

        // Verify some known functions exist
        List<String> functionNames = functions.stream()
            .map(ExprFunctionMetadata::name)
            .toList();

        assertTrue(functionNames.contains("env"), "Should have env() function");
        assertTrue(functionNames.contains("prop"), "Should have prop() function");
        assertTrue(functionNames.contains("uuid"), "Should have uuid() function");
    }
}
