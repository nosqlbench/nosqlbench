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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests YAML-safe embedding of multi-line Groovy expressions.
 * Demonstrates that expressions can be embedded in YAML using standard YAML
 * string formats (pipe literal, quoted strings, folded scalar) without breaking
 * YAML parsing.
 */
@Tag("unit")
class YamlSafeExpressionsTest {

    private final GroovyExpressionProcessor processor = new GroovyExpressionProcessor();
    private final Load yamlLoader = new Load(LoadSettings.builder().build());

    @Test
    void shouldParsePipeLiteralWithMultilineExpression() {
        String yaml = """
            key: |
              {{=
              def data = []
              for (int i = 1; i <= 3; i++) {
                  data << i * i
              }
              return data.join('-')
              }}
            """;

        // First verify YAML parses correctly
        Map<String, Object> parsed = (Map<String, Object>) yamlLoader.loadFromString(yaml);
        assertNotNull(parsed);
        assertTrue(parsed.containsKey("key"));

        // Get the value and process expressions
        String template = (String) parsed.get("key");
        String result = processor.process(template, null, Map.of());

        assertEquals("1-4-9\n", result);
    }

    @Test
    void shouldParseQuotedStringWithExpression() {
        String yaml = """
            value: "{{= 10 + 20 }}"
            """;

        Map<String, Object> parsed = (Map<String, Object>) yamlLoader.loadFromString(yaml);

        String template = (String) parsed.get("value");
        String result = processor.process(template, null, Map.of());

        assertEquals("30", result);
    }

    @Test
    void shouldParseFoldedScalarWithExpression() {
        // Note: Folded scalar (>) converts newlines to spaces, so the expression must be valid on one line
        String yaml = """
            key: >
              {{= "test value".toUpperCase() }}
            """;

        Map<String, Object> parsed = (Map<String, Object>) yamlLoader.loadFromString(yaml);

        String template = (String) parsed.get("key");
        String result = processor.process(template, null, Map.of());

        assertTrue(result.contains("TEST VALUE"));
    }

    @Test
    void shouldHandleComplexMultilineWithAssignments() {
        String yaml = """
            content: |
              {{computedValue =
                def sum = 0
                [1, 2, 3, 4, 5].each { sum += it }
                sum * 2
              }}
              Result: {{@computedValue}}
            """;

        Map<String, Object> parsed = (Map<String, Object>) yamlLoader.loadFromString(yaml);

        String template = (String) parsed.get("content");
        String result = processor.process(template, null, Map.of());

        assertTrue(result.contains("Result: 30"));
    }

    @Test
    void shouldHandleGroovyCollectionSyntax() {
        String yaml = """
            script: |
              {{=
              def config = [
                  name: 'test',
                  count: 100,
                  tags: ['tag1', 'tag2']
              ]
              return "Config: ${config.name}, count=${config.count}"
              }}
            """;

        Map<String, Object> parsed = (Map<String, Object>) yamlLoader.loadFromString(yaml);

        String template = (String) parsed.get("script");
        String result = processor.process(template, null, Map.of());

        assertTrue(result.contains("Config: test, count=100"));
    }

    @Test
    void shouldLoadAndProcessYamlWorkloadFile() throws Exception {
        // Load the test YAML workload file
        InputStream input = getClass().getResourceAsStream("/workloads/yaml_safe_multiline.yaml");
        assertNotNull(input, "Test workload file should exist");

        Map<String, Object> workload = (Map<String, Object>) yamlLoader.loadFromInputStream(input);
        assertNotNull(workload);

        // Verify the YAML structure parsed correctly
        assertTrue(workload.containsKey("scenarios"));
        Map<String, Object> scenarios = (Map<String, Object>) workload.get("scenarios");
        assertNotNull(scenarios);

        // Test pipe literal scenario
        Map<String, Object> pipeLiteral = (Map<String, Object>) scenarios.get("pipe_literal");
        String script = (String) pipeLiteral.get("script");
        String result = processor.process(script, null, Map.of());
        assertTrue(result.contains("1-4-9"));

        // Test quoted inline scenario
        Map<String, Object> quotedInline = (Map<String, Object>) scenarios.get("quoted_inline");
        String value = (String) quotedInline.get("value");
        result = processor.process(value, null, Map.of());
        assertEquals("30", result);
    }
}
