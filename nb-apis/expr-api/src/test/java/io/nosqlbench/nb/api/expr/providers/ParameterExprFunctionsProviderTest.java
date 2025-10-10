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
import io.nosqlbench.nb.api.expr.ExprFunctionMetadata;
import io.nosqlbench.nb.api.expr.ExprFunctionAnnotations;
import io.nosqlbench.nb.api.expr.TestExprRuntimeContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParameterExprFunctionsProviderTest {

    @Test
    void shouldExposeParameterHelpers() {
        Map<String, Object> params = new HashMap<>();
        params.put("threshold", 42);
        params.put("mode", "strict");
        params.put("nullable", null);
        TestExprRuntimeContext context = new TestExprRuntimeContext(params, Optional.of(URI.create("nb://source")));

        ParameterExprFunctionsProvider provider = new ParameterExprFunctionsProvider();
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
}
