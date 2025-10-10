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


import groovy.lang.GroovyShell;
import io.nosqlbench.nb.api.expr.annotations.ExprExampleContext;
import io.nosqlbench.nb.api.expr.ExprFunctionAnnotations;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ExprFunctionExamplesIT {

    private final GroovyShell shell = new GroovyShell();

    @Test
    void shouldExecuteAllDeclaredExamples() {
        ServiceLoader<ExprFunctionProvider> loader = ServiceLoader.load(ExprFunctionProvider.class);
        Set<String> functionsMissingExamples = new HashSet<>();
        boolean foundProvider = false;

        for (ExprFunctionProvider provider : loader) {
            foundProvider = true;
            ExprFunctionAnnotations.extractMetadata(provider.getClass()).forEach(metadata -> {
                if (metadata.examples().size() < 2) {
                    functionsMissingExamples.add(metadata.name());
                }
                for (ExprFunctionExample example : metadata.examples()) {
                    TestExprRuntimeContext context = createContext(example.context());
                    ExprFunctionAnnotations.registerAnnotatedFunctions(context, provider);

                    ExprFunction function = context.function(metadata.name());
                    assertNotNull(function, () -> "Function '" + metadata.name() + "' was not registered by " + provider.getClass().getSimpleName());

                    Map<String, String> previousProperties = applySystemProperties(example.systemProperties());
                    try {
                        Object result = invoke(function, example.args());
                        verifyExpectation(metadata.name(), example, result);
                    } finally {
                        restoreSystemProperties(previousProperties);
                    }
                }
            });
        }

        assertTrue(foundProvider, "No expression function providers were discovered");
        if (!functionsMissingExamples.isEmpty()) {
            fail("Function(s) missing minimum example coverage: " + functionsMissingExamples);
        }
    }

    private TestExprRuntimeContext createContext(ExprExampleContext contextMode) {
        Map<String, Object> params = new HashMap<>();
        params.put("threshold", 42);
        params.put("mode", "strict");
        params.put("nullable", null);

        Optional<URI> source = switch (contextMode) {
            case NO_SOURCE_URI -> Optional.empty();
            case DEFAULT -> Optional.of(URI.create("nb://example"));
        };
        return new TestExprRuntimeContext(params, source);
    }

    private Map<String, String> applySystemProperties(List<String> entries) {
        Map<String, String> previous = new HashMap<>();
        for (String entry : entries) {
            String key;
            String value;
            int idx = entry.indexOf('=');
            if (idx >= 0) {
                key = entry.substring(0, idx);
                value = entry.substring(idx + 1);
            } else {
                key = entry;
                value = "";
            }
            String prior = System.getProperty(key);
            previous.put(key, prior);
            if (value.isEmpty()) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        }
        return previous;
    }

    private void restoreSystemProperties(Map<String, String> previous) {
        previous.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }

    private Object invoke(ExprFunction function, List<String> argExpressions) {
        Object[] args = argExpressions.stream()
            .map(this::evaluateLiteral)
            .toArray(Object[]::new);
        return function.apply(args);
    }

    private Object evaluateLiteral(String expression) {
        if (expression == null || expression.isBlank()) {
            return expression;
        }
        return shell.evaluate(expression);
    }

    private void verifyExpectation(String functionName, ExprFunctionExample example, Object result) {
        if (example.expectNull()) {
            assertNull(result, () -> functionName + " example expected null but was " + result);
            return;
        }
        if (example.expectNotNull()) {
            assertNotNull(result, () -> functionName + " example expected non-null result" );
        }
        if (!example.expect().isBlank()) {
            Object expectedValue = evaluateLiteral(example.expect());
            if (expectedValue == null) {
                assertNull(result, () -> functionName + " example expected null but was " + result);
            } else {
                assertNotNull(result, () -> functionName + " example expected " + expectedValue + " but was null");
                assertEquals(expectedValue, result,
                    () -> functionName + " example expected " + expectedValue + " but was " + result);
            }
        }
        if (!example.matches().isBlank()) {
            Pattern pattern = Pattern.compile(example.matches());
            String rendered = String.valueOf(result);
            assertTrue(pattern.matcher(rendered).matches(),
                () -> functionName + " example expected result matching /" + example.matches() + "/ but was '" + rendered + "'");
        }
    }
}
