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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests demonstrating execution of Groovy block statements within expression syntax.
 */
@Tag("unit")
class GroovyBlockStatementsTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();

    @Test
    void shouldExecuteSimpleGroovyBlockWithMultipleStatements() {
        String template = """
            result={{=
                def x = 10
                def y = 20
                return x + y
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("result=30", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithLoopAndCollections() {
        String template = """
            squares={{=
                def data = []
                for (int i = 1; i <= 5; i++) {
                    data << i * i
                }
                return data.join(',')
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("squares=1,4,9,16,25", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithConditionalLogic() {
        String template = """
            status={{=
                def score = 85
                if (score >= 90) {
                    return 'excellent'
                } else if (score >= 70) {
                    return 'good'
                } else {
                    return 'needs improvement'
                }
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("status=good", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithClosures() {
        String template = """
            doubled={{=
                def numbers = [1, 2, 3, 4, 5]
                def doubled = numbers.collect { it * 2 }
                return doubled.join('-')
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("doubled=2-4-6-8-10", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithStringManipulation() {
        String template = """
            formatted={{=
                def words = ['hello', 'world', 'groovy']
                def capitalized = words.collect { it.capitalize() }
                return capitalized.join(' ')
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("formatted=Hello World Groovy", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithMapOperations() {
        String template = """
            summary={{=
                def inventory = [apples: 5, oranges: 8, bananas: 3]
                def total = inventory.values().sum()
                return "Total: ${total} items"
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("summary=Total: 16 items", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithMethodDefinition() {
        String template = """
            factorial={{=
                def factorial(n) {
                    return n <= 1 ? 1 : n * factorial(n - 1)
                }
                return factorial(5)
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("factorial=120", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithTryCatch() {
        String template = """
            safe={{=
                try {
                    def result = 100 / 5
                    return "Success: ${result}"
                } catch (Exception e) {
                    return "Error: ${e.message}"
                }
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("safe=Success: 20", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithGStringInterpolation() {
        String template = """
            message={{=
                def name = 'Alice'
                def age = 30
                return "${name} is ${age} years old"
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("message=Alice is 30 years old", rendered);
    }

    @Test
    void shouldExecuteGroovyBlockWithMultilineStatements() {
        String template = """
            prefix={{=
                def lines = []
                lines << 'Line 1'
                lines << 'Line 2'
                lines << 'Line 3'
                return lines.size()
            }}
            """;

        String rendered = processor.process(template, null, Map.of());

        assertTrue(rendered.contains("prefix=3"));
    }

    @Test
    void shouldExecuteMultipleGroovyBlocksInSameTemplate() {
        String template = """
            first={{=
                def a = 5
                return a * 2
            }}
            second={{=
                def b = [1, 2, 3]
                return b.sum()
            }}
            third={{=
                return 'done'
            }}
            """;

        String rendered = processor.process(template, null, Map.of());

        assertTrue(rendered.contains("first=10"));
        assertTrue(rendered.contains("second=6"));
        assertTrue(rendered.contains("third=done"));
    }

    @Test
    void shouldExecuteGroovyBlockWithRangeOperations() {
        String template = """
            range={{=
                def sum = 0
                (1..10).each { sum += it }
                return sum
            }}""";

        String rendered = processor.process(template, null, Map.of());

        assertEquals("range=55", rendered);
    }
}
