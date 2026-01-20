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
import org.junit.jupiter.api.Tag;

import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests verifying that variables can be accessed directly in Groovy expressions
 * without requiring special prefixes or reference syntax.
 */
@Tag("unit")
class DirectVariableAccessTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldAccessVariableDirectlyAfterAssignment() {
        String template = """
            set: {{myVar = 42}}
            direct: {{= myVar}}
            """;

        String processed = processor.process(template, URI.create("test://direct"), Map.of());

        assertTrue(processed.contains("set: 42"), "Assignment should work");
        assertTrue(processed.contains("direct: 42"), "Direct variable access should work");
    }

    @Test
    void shouldAccessVariableInArithmeticExpression() {
        String template = """
            init: {{counter = 10}}
            add: {{= counter + 5}}
            multiply: {{= counter * 2}}
            """;

        String processed = processor.process(template, URI.create("test://arithmetic"), Map.of());

        assertTrue(processed.contains("init: 10"), "Init should work");
        assertTrue(processed.contains("add: 15"), "Addition should work");
        assertTrue(processed.contains("multiply: 20"), "Multiplication should work");
    }

    @Test
    void shouldAccessVariableInStringInterpolation() {
        String template = """
            name: {{userName = 'Alice'}}
            greeting: {{= "Hello, ${userName}!"}}
            """;

        String processed = processor.process(template, URI.create("test://interpolation"), Map.of());

        assertTrue(processed.contains("name: Alice"), "Name assignment should work");
        assertTrue(processed.contains("greeting: Hello, Alice!"), "String interpolation should work");
    }

    @Test
    void shouldAccessListVariableDirectly() {
        String template = """
            init: {{items = ['apple', 'banana', 'cherry']}}
            size: {{= items.size()}}
            first: {{= items[0]}}
            join: {{= items.join(', ')}}
            """;

        String processed = processor.process(template, URI.create("test://list"), Map.of());

        assertTrue(processed.contains("size: 3"), "List size should be accessible");
        assertTrue(processed.contains("first: apple"), "List element should be accessible");
        assertTrue(processed.contains("join: apple, banana, cherry"), "List join should work");
    }

    @Test
    void shouldAccessMapVariableDirectly() {
        String template = """
            init: {{config = [mode: 'test', timeout: 5000]}}
            mode: {{= config.mode}}
            timeout: {{= config.timeout}}
            """;

        String processed = processor.process(template, URI.create("test://map"), Map.of());

        assertTrue(processed.contains("mode: test"), "Map value should be accessible");
        assertTrue(processed.contains("timeout: 5000"), "Map value should be accessible");
    }

    @Test
    void shouldUpdateVariableAndAccessNewValue() {
        String template = """
            init: {{value = 1}}
            read1: {{= value}}
            update: {{value = value * 10}}
            read2: {{= value}}
            """;

        String processed = processor.process(template, URI.create("test://update"), Map.of());

        assertTrue(processed.contains("init: 1"), "Init should work");
        assertTrue(processed.contains("read1: 1"), "First read should see initial value");
        assertTrue(processed.contains("update: 10"), "Update should work");
        assertTrue(processed.contains("read2: 10"), "Second read should see updated value");
    }

    @Test
    void shouldAccessVariableSetInGroovyBlock() {
        String template = """
            block: {{=
                myValue = 100
                return "Set to " + myValue
            }}
            access: {{= myValue}}
            modify: {{= myValue + 50}}
            """;

        String processed = processor.process(template, URI.create("test://block"), Map.of());

        assertTrue(processed.contains("block: Set to 100"), "Block should work");
        assertTrue(processed.contains("access: 100"), "Variable from block should be accessible");
        assertTrue(processed.contains("modify: 150"), "Variable from block should be usable in expressions");
    }
}
