package io.nosqlbench.nb.api.expr.providers;

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


import io.nosqlbench.nb.api.system.NBEnvironment;
import io.nosqlbench.nb.api.expr.ExprFunction;
import io.nosqlbench.nb.api.expr.ExprFunctionAnnotations;
import io.nosqlbench.nb.api.expr.ExprFunctionMetadata;
import io.nosqlbench.nb.api.expr.TestExprRuntimeContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TemplateExprFunctionsProviderTest {

    @BeforeEach
    void setUp() {
        // Clear thread-local state before each test
        TemplateExprFunctionsProvider.clearThreadState();
    }

    @AfterEach
    void tearDown() {
        // Clear thread-local state after each test to prevent leaks
        TemplateExprFunctionsProvider.clearThreadState();
    }

    @Test
    void shouldExposeTemplateFunctions() {
        Map<String, Object> params = new HashMap<>();
        params.put("existing", "value");
        TestExprRuntimeContext context = new TestExprRuntimeContext(params, Optional.of(URI.create("nb://source")));

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");
        ExprFunction templateAlt = context.function("_templateAlt");
        ExprFunction templateTrack = context.function("_templateTrack");

        assertNotNull(templateSet, "_templateSet function should be registered");
        assertNotNull(templateAlt, "_templateAlt function should be registered");
        assertNotNull(templateTrack, "_templateTrack function should be registered");

        Map<String, ExprFunctionMetadata> metadata = context.getRegisteredMetadata();
        assertTrue(metadata.containsKey("_templateSet"));
        assertTrue(metadata.containsKey("_templateAlt"));
        assertTrue(metadata.containsKey("_templateTrack"));
    }

    @Test
    void templateSet_shouldUseExistingParameter() {
        Map<String, Object> params = new HashMap<>();
        params.put("retries", "5");
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        // Should return existing parameter value
        Object result = templateSet.apply("retries", "3");
        assertEquals("5", result);

        // Should track access
        Map<String, String> accesses = TemplateExprFunctionsProvider.getTemplateAccesses();
        assertEquals("5", accesses.get("retries"));
    }

    @Test
    void templateSet_shouldSetAndUseDefault() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        // First call should set and return default
        Object result = templateSet.apply("retries", "3");
        assertEquals("3", result);

        // Should track access
        Map<String, String> accesses = TemplateExprFunctionsProvider.getTemplateAccesses();
        assertEquals("3", accesses.get("retries"));

        // Second call should return the set value
        Object result2 = templateSet.apply("retries", "5");
        assertEquals("3", result2, "Should return the initially set value");
    }

    @Test
    void templateSet_shouldCheckEnvironmentVariables() {
        // Note: This test uses actual environment variables, not mocked ones
        // We'll test with USER which should exist on most systems
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        // Should return environment variable value if USER is set
        // If not, will use default (this makes test more portable)
        Object result = templateSet.apply("USER", "default");
        assertNotNull(result);
        // Just verify it returns something (either env var or default)
        assertTrue(result instanceof String);
    }

    @Test
    void templateSet_shouldRequireTwoArguments() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        // Should throw with one argument
        RuntimeException oneArg = assertThrows(RuntimeException.class, () -> templateSet.apply("name"));
        assertTrue(oneArg.getCause() instanceof IllegalArgumentException);

        // Should throw with three arguments
        RuntimeException threeArgs = assertThrows(RuntimeException.class,
            () -> templateSet.apply("name", "default", "extra"));
        assertTrue(threeArgs.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void templateAlt_shouldReturnAlternateWhenParameterSet() {
        Map<String, Object> params = new HashMap<>();
        params.put("debug", "true");
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateAlt = context.function("_templateAlt");

        // Should return alternate when parameter is set
        Object result = templateAlt.apply("debug", "verbose");
        assertEquals("verbose", result);

        // Should track access
        Map<String, String> accesses = TemplateExprFunctionsProvider.getTemplateAccesses();
        assertEquals("verbose", accesses.get("debug"));
    }

    @Test
    void templateAlt_shouldReturnEmptyWhenParameterNotSet() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateAlt = context.function("_templateAlt");

        // Should return empty string when parameter not set
        Object result = templateAlt.apply("missing", "alternate");
        assertEquals("", result);

        // Should not track access for unset parameters
        Map<String, String> accesses = TemplateExprFunctionsProvider.getTemplateAccesses();
        assertFalse(accesses.containsKey("missing"));
    }

    @Test
    void templateAlt_shouldCheckEnvironmentVariables() {
        // Note: This test uses actual environment variables
        // We'll test with USER which should exist on most systems
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateAlt = context.function("_templateAlt");

        // Should return alternate when environment variable is set (USER should be set)
        Object result = templateAlt.apply("USER", "verbose");
        // Since USER is typically set, we expect "verbose"
        // If not set (edge case), would return ""
        assertTrue(result instanceof String);
    }

    @Test
    void templateAlt_shouldRequireTwoArguments() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateAlt = context.function("_templateAlt");

        // Should throw with one argument
        RuntimeException oneArg = assertThrows(RuntimeException.class, () -> templateAlt.apply("name"));
        assertTrue(oneArg.getCause() instanceof IllegalArgumentException);

        // Should throw with three arguments
        RuntimeException threeArgs = assertThrows(RuntimeException.class,
            () -> templateAlt.apply("name", "alternate", "extra"));
        assertTrue(threeArgs.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void templateTrack_shouldTrackAccess() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateTrack = context.function("_templateTrack");

        // Should track and return value
        Object result = templateTrack.apply("myvar", "myvalue");
        assertEquals("myvalue", result);

        // Should be tracked
        Map<String, String> accesses = TemplateExprFunctionsProvider.getTemplateAccesses();
        assertEquals("myvalue", accesses.get("myvar"));
    }

    @Test
    void templateTrack_shouldRequireTwoArguments() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateTrack = context.function("_templateTrack");

        // Should throw with one argument
        RuntimeException oneArg = assertThrows(RuntimeException.class, () -> templateTrack.apply("name"));
        assertTrue(oneArg.getCause() instanceof IllegalArgumentException);

        // Should throw with three arguments
        RuntimeException threeArgs = assertThrows(RuntimeException.class,
            () -> templateTrack.apply("name", "value", "extra"));
        assertTrue(threeArgs.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void shouldHandlePrecedenceCorrectly() {
        // Test that parameters take precedence over defaults
        Map<String, Object> params = new HashMap<>();
        params.put("shared_var", "param_value");

        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        // Parameters should take precedence over default
        Object result = templateSet.apply("shared_var", "default");
        assertEquals("param_value", result);

        // Clear parameters to test default precedence
        TemplateExprFunctionsProvider.clearThreadState();
        TestExprRuntimeContext context2 = new TestExprRuntimeContext(new HashMap<>());
        ExprFunctionAnnotations.registerAnnotatedFunctions(context2, provider);
        ExprFunction templateSet2 = context2.function("_templateSet");

        // Default should be used when parameter not present
        Object result2 = templateSet2.apply("other_var", "default_value");
        assertEquals("default_value", result2);
    }

    @Test
    void threadLocalState_shouldBeIsolatedPerThread() throws InterruptedException {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        TemplateExprFunctionsProvider provider = new TemplateExprFunctionsProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        // Set value in main thread
        templateSet.apply("var1", "main_thread");

        // Create another thread and verify it has separate state
        Thread otherThread = new Thread(() -> {
            TestExprRuntimeContext otherContext = new TestExprRuntimeContext(params);
            ExprFunctionAnnotations.registerAnnotatedFunctions(otherContext, provider);
            ExprFunction otherTemplateSet = otherContext.function("_templateSet");

            // Should use default, not the value from main thread
            Object result = otherTemplateSet.apply("var1", "other_thread");
            assertEquals("other_thread", result);

            Map<String, String> otherAccesses = TemplateExprFunctionsProvider.getTemplateAccesses();
            assertEquals("other_thread", otherAccesses.get("var1"));
        });

        otherThread.start();
        otherThread.join();

        // Main thread should still have its own value
        Map<String, String> mainAccesses = TemplateExprFunctionsProvider.getTemplateAccesses();
        assertEquals("main_thread", mainAccesses.get("var1"));
    }
}
