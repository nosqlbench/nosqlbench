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


import io.nosqlbench.nb.api.expr.ExprFunction;
import io.nosqlbench.nb.api.expr.ExprFunctionAnnotations;
import io.nosqlbench.nb.api.expr.ExprFunctionMetadata;
import io.nosqlbench.nb.api.expr.TestExprRuntimeContext;
import io.nosqlbench.nb.api.expr.TemplateContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for UnifiedParameterProvider that validates all canonical
 * test requirements from both ParameterExprFunctionsProvider and TemplateExprFunctionsProvider.
 */
class UnifiedParameterProviderTest {

    // ============================================================
    // User-Facing Parameter API Tests
    // ============================================================

    @Test
    void shouldExposeParameterHelpers() {
        Map<String, Object> params = new HashMap<>();
        params.put("threshold", 42);
        params.put("mode", "strict");
        params.put("nullable", null);
        TestExprRuntimeContext context = new TestExprRuntimeContext(params, Optional.of(URI.create("nb://source")));

        UnifiedParameterProvider provider = new UnifiedParameterProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction param = context.function("param");
        ExprFunction paramOr = context.function("paramOr");
        ExprFunction hasParam = context.function("hasParam");

        assertEquals(42, param.apply("threshold"));
        assertEquals("strict", param.apply("mode"));
        RuntimeException missingParam = assertThrows(RuntimeException.class, () -> param.apply("missing"));
        assertTrue(missingParam.getCause() instanceof IllegalArgumentException);
        RuntimeException extraArg = assertThrows(RuntimeException.class, () -> param.apply("threshold", "extra"));
        assertTrue(extraArg.getCause() instanceof IllegalArgumentException);

        assertEquals("fallback", paramOr.apply("missing", "fallback"));
        assertEquals(42, paramOr.apply("threshold", "fallback"));
        assertEquals("default", paramOr.apply("nullable", "default"));
        RuntimeException noArgs = assertThrows(RuntimeException.class, paramOr::apply);
        assertTrue(noArgs.getCause() instanceof IllegalArgumentException);
        RuntimeException tooManyArgs = assertThrows(RuntimeException.class, () -> paramOr.apply("name", "default", "extra"));
        assertTrue(tooManyArgs.getCause() instanceof IllegalArgumentException);

        assertTrue((Boolean) hasParam.apply("threshold"));
        assertFalse((Boolean) hasParam.apply("missing"));
        RuntimeException hasParamNoArgs = assertThrows(RuntimeException.class, hasParam::apply);
        assertTrue(hasParamNoArgs.getCause() instanceof IllegalArgumentException);
        RuntimeException hasParamTooMany = assertThrows(RuntimeException.class, () -> hasParam.apply("threshold", "extra"));
        assertTrue(hasParamTooMany.getCause() instanceof IllegalArgumentException);

        Map<String, ExprFunctionMetadata> metadata = context.getRegisteredMetadata();
        assertTrue(metadata.containsKey("param"));
        assertTrue(metadata.containsKey("paramOr"));
        assertTrue(metadata.containsKey("hasParam"));
    }

    // ============================================================
    // Internal Template API Tests
    // ============================================================

    @Test
    void shouldExposeTemplateFunctions() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            params.put("existing", "value");
            TestExprRuntimeContext context = new TestExprRuntimeContext(params, Optional.of(URI.create("nb://source")));

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");
            ExprFunction templateAlt = context.function("_templateAlt");
            ExprFunction templateTrack = context.function("_templateTrack");
            ExprFunction templateGet = context.function("_templateGet");

            assertNotNull(templateSet, "_templateSet function should be registered");
            assertNotNull(templateAlt, "_templateAlt function should be registered");
            assertNotNull(templateTrack, "_templateTrack function should be registered");
            assertNotNull(templateGet, "_templateGet function should be registered");

            Map<String, ExprFunctionMetadata> metadata = context.getRegisteredMetadata();
            assertTrue(metadata.containsKey("_templateSet"));
            assertTrue(metadata.containsKey("_templateAlt"));
            assertTrue(metadata.containsKey("_templateTrack"));
            assertTrue(metadata.containsKey("_templateGet"));
        }
    }

    @Test
    void templateSet_shouldUseExistingParameter() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            params.put("retries", "5");
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");

            // Should return existing parameter value
            Object result = templateSet.apply("retries", "3");
            assertEquals("5", result);

            // Should track access
            Map<String, String> accesses = ctx.getAccesses();
            assertEquals("5", accesses.get("retries"));
        }
    }

    @Test
    void templateSet_shouldSetAndUseDefault() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");

            // First call should set and return default
            Object result = templateSet.apply("retries", "3");
            assertEquals("3", result);

            // Should track access
            Map<String, String> accesses = ctx.getAccesses();
            assertEquals("3", accesses.get("retries"));

            // Second call should return the set value
            Object result2 = templateSet.apply("retries", "5");
            assertEquals("3", result2, "Should return the initially set value");
        }
    }

    @Test
    void templateSet_shouldCheckEnvironmentVariables() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");

            // Should return environment variable value if USER is set
            Object result = templateSet.apply("USER", "default");
            assertNotNull(result);
            assertTrue(result instanceof String);
        }
    }

    @Test
    void templateSet_shouldRequireTwoArguments() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        UnifiedParameterProvider provider = new UnifiedParameterProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateSet = context.function("_templateSet");

        RuntimeException oneArg = assertThrows(RuntimeException.class, () -> templateSet.apply("name"));
        assertTrue(oneArg.getCause() instanceof IllegalArgumentException);

        RuntimeException threeArgs = assertThrows(RuntimeException.class,
            () -> templateSet.apply("name", "default", "extra"));
        assertTrue(threeArgs.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void templateAlt_shouldReturnAlternateWhenParameterSet() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            params.put("debug", "true");
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateAlt = context.function("_templateAlt");

            Object result = templateAlt.apply("debug", "verbose");
            assertEquals("verbose", result);

            Map<String, String> accesses = ctx.getAccesses();
            assertEquals("verbose", accesses.get("debug"));
        }
    }

    @Test
    void templateAlt_shouldReturnEmptyWhenParameterNotSet() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateAlt = context.function("_templateAlt");

            Object result = templateAlt.apply("missing", "alternate");
            assertEquals("", result);

            Map<String, String> accesses = ctx.getAccesses();
            assertFalse(accesses.containsKey("missing"));
        }
    }

    @Test
    void templateAlt_shouldCheckEnvironmentVariables() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateAlt = context.function("_templateAlt");

            Object result = templateAlt.apply("USER", "verbose");
            assertTrue(result instanceof String);
        }
    }

    @Test
    void templateAlt_shouldRequireTwoArguments() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        UnifiedParameterProvider provider = new UnifiedParameterProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateAlt = context.function("_templateAlt");

        RuntimeException oneArg = assertThrows(RuntimeException.class, () -> templateAlt.apply("name"));
        assertTrue(oneArg.getCause() instanceof IllegalArgumentException);

        RuntimeException threeArgs = assertThrows(RuntimeException.class,
            () -> templateAlt.apply("name", "alternate", "extra"));
        assertTrue(threeArgs.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void templateTrack_shouldTrackAccess() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateTrack = context.function("_templateTrack");

            Object result = templateTrack.apply("myvar", "myvalue");
            assertEquals("myvalue", result);

            Map<String, String> accesses = ctx.getAccesses();
            assertEquals("myvalue", accesses.get("myvar"));
        }
    }

    @Test
    void templateTrack_shouldRequireTwoArguments() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        UnifiedParameterProvider provider = new UnifiedParameterProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction templateTrack = context.function("_templateTrack");

        RuntimeException oneArg = assertThrows(RuntimeException.class, () -> templateTrack.apply("name"));
        assertTrue(oneArg.getCause() instanceof IllegalArgumentException);

        RuntimeException threeArgs = assertThrows(RuntimeException.class,
            () -> templateTrack.apply("name", "value", "extra"));
        assertTrue(threeArgs.getCause() instanceof IllegalArgumentException);
    }

    @Test
    void templateGet_shouldRespectOverrides() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");
            ExprFunction templateGet = context.function("_templateGet");

            // Set an override via _templateSet
            templateSet.apply("myvar", "first");

            // _templateGet should return the override
            Object result = templateGet.apply("myvar", "second");
            assertEquals("first", result);

            Map<String, String> accesses = ctx.getAccesses();
            assertEquals("first", accesses.get("myvar"));
        }
    }

    // ============================================================
    // Unified Resolution Tests
    // ============================================================

    @Test
    void shouldHandlePrecedenceCorrectly() {
        // Test that parameters take precedence over defaults
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            params.put("shared_var", "param_value");

            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");

            // Parameters should take precedence over default
            Object result = templateSet.apply("shared_var", "default");
            assertEquals("param_value", result);
        }

        // Test default precedence in a new context
        try (TemplateContext ctx2 = TemplateContext.enter()) {
            TestExprRuntimeContext context2 = new TestExprRuntimeContext(new HashMap<>());
            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context2, provider);
            ExprFunction templateSet2 = context2.function("_templateSet");

            Object result2 = templateSet2.apply("other_var", "default_value");
            assertEquals("default_value", result2);
        }
    }

    @Test
    void threadLocalState_shouldBeIsolatedPerThread() throws InterruptedException {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");

            // Set value in main thread
            templateSet.apply("var1", "main_thread");

            // Create another thread and verify it has separate state
            Thread otherThread = new Thread(() -> {
                try (TemplateContext otherCtx = TemplateContext.enter()) {
                    TestExprRuntimeContext otherContext = new TestExprRuntimeContext(params);
                    ExprFunctionAnnotations.registerAnnotatedFunctions(otherContext, provider);
                    ExprFunction otherTemplateSet = otherContext.function("_templateSet");

                    // Should use default, not the value from main thread
                    Object result = otherTemplateSet.apply("var1", "other_thread");
                    assertEquals("other_thread", result);

                    Map<String, String> otherAccesses = otherCtx.getAccesses();
                    assertEquals("other_thread", otherAccesses.get("var1"));
                }
            });

            otherThread.start();
            otherThread.join();

            // Main thread should still have its own value
            Map<String, String> mainAccesses = ctx.getAccesses();
            assertEquals("main_thread", mainAccesses.get("var1"));
        }
    }

    @Test
    void paramOr_shouldSupportLexicalScoping() {
        Map<String, Object> params = new HashMap<>();
        TestExprRuntimeContext context = new TestExprRuntimeContext(params);

        UnifiedParameterProvider provider = new UnifiedParameterProvider();
        ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

        ExprFunction paramOr = context.function("paramOr");

        // First call with default
        Object result1 = paramOr.apply("scoped_var", "initial_value");
        assertEquals("initial_value", result1);

        // Second call should reuse the cached value
        Object result2 = paramOr.apply("scoped_var", "different_value");
        assertEquals("initial_value", result2, "Should return cached value from first call");
    }

    @Test
    void templateContext_shouldRespectOverridesPrecedence() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            params.put("myvar", "from_params");
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");

            // Set override manually
            ctx.setOverride("myvar", "from_override");

            // Should use override, not params
            Object result = templateSet.apply("myvar", "from_default");
            assertEquals("from_override", result);
        }
    }

    @Test
    void unifiedResolution_shouldCheckEnvironmentOnlyForTemplateFunctions() {
        try (TemplateContext ctx = TemplateContext.enter()) {
            Map<String, Object> params = new HashMap<>();
            TestExprRuntimeContext context = new TestExprRuntimeContext(params);

            UnifiedParameterProvider provider = new UnifiedParameterProvider();
            ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

            ExprFunction templateSet = context.function("_templateSet");
            ExprFunction hasParam = context.function("hasParam");

            // Template function should check environment
            Object templateResult = templateSet.apply("USER", "default");
            assertNotNull(templateResult);

            // User-facing function should NOT check environment
            Object hasParamResult = hasParam.apply("USER");
            // Since USER is not in params, should return false even if env var exists
            assertFalse((Boolean) hasParamResult);
        }
    }
}
