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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroovyExpressionProcessorResourceIT {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldRenderMemoizedLocalsFromTemplateResource() {
        String template = readResource("expr/memoized/memoized_locals_basic.template");
        String expected = readResource("expr/memoized/memoized_locals_basic.expected");

        String rendered = processor.process(template, URI.create("nb://test/memoized/basic"), Map.of());

        assertEquals(normalize(expected), normalize(rendered));
    }

    @Test
    void shouldRejectStrictReassignmentFromResource() {
        String template = readResource("expr/memoized/memoized_locals_strict_violation.template");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> processor.process(template, URI.create("nb://test/memoized/strict-repeat"), Map.of())
        );

        assertTrue(ex.getMessage().contains("already set"));
    }

    @Test
    void shouldRejectDereferenceBeforeAssignment() {
        String template = readResource("expr/memoized/memoized_locals_missing_read.template");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> processor.process(template, URI.create("nb://test/memoized/unassigned"), Map.of())
        );

        assertTrue(ex.getMessage().contains("has not been set"));
    }

    @Test
    void shouldRejectNullValueWhenBangModifierUsed() {
        String template = readResource("expr/memoized/memoized_locals_bang_null.template");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> processor.process(template, URI.create("nb://test/memoized/non-null"), Map.of())
        );

        assertTrue(ex.getMessage().contains("was null"));
    }

    private String readResource(String resourceName) {
        InputStream input = GroovyExpressionProcessorResourceIT.class.getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(input, () -> "Could not load test resource '" + resourceName + "'");
        try (input) {
            byte[] data = input.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Error reading test resource '" + resourceName + "'", e);
        }
    }

    private String normalize(String value) {
        return value.replace("\r\n", "\n").stripTrailing();
    }
}
