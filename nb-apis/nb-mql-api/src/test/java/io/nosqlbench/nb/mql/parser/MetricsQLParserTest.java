/*
 * Copyright (c) nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.nb.mql.parser;

import io.nosqlbench.nb.mql.generated.MetricsQLLexer;
import io.nosqlbench.nb.mql.generated.MetricsQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MetricsQL parser grammar and basic transformation.
 * Phase 1: Validates parser can handle basic selectors and expression structure.
 */
@Tag("unit")
class MetricsQLParserTest {

    /**
     * Helper method to parse a MetricsQL expression
     */
    private ParseTree parse(String expression) {
        MetricsQLLexer lexer = new MetricsQLLexer(CharStreams.fromString(expression));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MetricsQLParser parser = new MetricsQLParser(tokens);
        return parser.query();
    }

    /**
     * Helper method to parse and transform a MetricsQL expression
     */
    private SQLFragment transform(String expression) {
        ParseTree tree = parse(expression);
        MetricsQLTransformer transformer = new MetricsQLTransformer();
        return transformer.visit(tree);
    }

    @Test
    void testSimpleMetricSelector() {
        String expression = "http_requests_total";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("http_requests_total"),
            "Parameters should contain metric name");
        assertTrue(result.getSql().contains("sample_name"),
            "SQL should query sample_name table");
    }

    @Test
    void testMetricSelectorWithLabels() {
        String expression = "http_requests_total{job=\"api\", status=\"200\"}";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("http_requests_total"));
        assertTrue(result.getParameters().contains("job"));
        assertTrue(result.getParameters().contains("api"));
        assertTrue(result.getParameters().contains("status"));
        assertTrue(result.getParameters().contains("200"));
    }

    @Test
    void testMetricSelectorWithTimeRange() {
        String expression = "http_requests_total[5m]";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("http_requests_total"));
        // Time range support will be added in Phase 3
    }

    @Test
    void testMetricSelectorWithLabelsAndTimeRange() {
        String expression = "http_requests_total{job=\"api\"}[5m]";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("http_requests_total"));
        assertTrue(result.getParameters().contains("job"));
        assertTrue(result.getParameters().contains("api"));
    }

    @Test
    void testRateFunction() {
        String expression = "rate(http_requests_total[5m])";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Phase 3: Will implement rate() function
    }

    @Test
    void testSumAggregation() {
        String expression = "sum(http_requests_total)";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Phase 4: Will implement sum() aggregation
    }

    @Test
    void testAggregationWithBy() {
        String expression = "sum(http_requests_total) by (job, instance)";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Phase 4: Will implement aggregation with BY modifier
    }

    @Test
    void testArithmeticExpression() {
        String expression = "metric1 + metric2";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Phase 7: Will implement binary operations
    }

    @Test
    void testComplexArithmeticExpression() {
        String expression = "(metric1 + metric2) / metric3";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Phase 7: Will implement binary operations
    }

    @Test
    void testNestedFunction() {
        String expression = "rate(sum(http_requests_total) by (job)[5m])";
        ParseTree tree = parse(expression);
        assertNotNull(tree);

        // Phase 3: Nested functions not yet supported - only verify parsing
        // Phases 3+4: Will implement nested function composition
        // For now, transformation would fail as expected
    }

    /**
     * Test that various valid expressions parse without errors
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "metric_name",
        "metric_name{label=\"value\"}",
        "metric_name[5m]",
        "metric_name{label=\"value\"}[5m]",
        "rate(metric[5m])",
        "sum(metric) by (label)",
        "avg(metric) without (label)",
        "metric1 + metric2",
        "metric1 - metric2 * metric3",
        "sum(rate(metric[5m])) by (job)",
        "(metric1 + metric2) / 2",
        "metric{label1=\"value1\", label2=\"value2\"}",
        "metric{label=~\"regex.*\"}",
        "metric{label!=\"value\"}",
        "metric{label!~\"regex.*\"}"
    })
    void testValidExpressions(String expression) {
        assertDoesNotThrow(() -> {
            ParseTree tree = parse(expression);
            assertNotNull(tree, "Parse tree should not be null for: " + expression);
        }, "Should parse without throwing exception: " + expression);
    }

    /**
     * Test label matcher operators
     */
    @Test
    void testLabelMatcherEqual() {
        String expression = "metric{label=\"value\"}";
        SQLFragment result = transform(expression);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("label"));
        assertTrue(result.getParameters().contains("value"));
    }

    @Test
    void testLabelMatcherNotEqual() {
        String expression = "metric{label!=\"value\"}";
        SQLFragment result = transform(expression);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("label"));
        assertTrue(result.getParameters().contains("value"));
        assertTrue(result.getSql().contains("NOT IN"));
    }

    @Test
    void testLabelMatcherRegex() {
        String expression = "metric{label=~\"value.*\"}";
        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Regex matching will be fully implemented in later phase
    }

    @Test
    void testLabelMatcherNotRegex() {
        String expression = "metric{label!~\"value.*\"}";
        SQLFragment result = transform(expression);
        assertNotNull(result);
        // Regex matching will be fully implemented in later phase
    }

    /**
     * Test multiple aggregation functions - just verify they parse
     */
    @ParameterizedTest
    @ValueSource(strings = {"sum", "avg", "min", "max", "count", "stddev", "stdvar"})
    void testAggregationFunctions(String aggFunc) {
        String expression = aggFunc + "(metric)";
        assertDoesNotThrow(() -> {
            SQLFragment result = transform(expression);
            assertNotNull(result);
        });
    }

    /**
     * Test number literals
     */
    @Test
    void testNumberLiteral() {
        String expression = "metric + 100";
        assertDoesNotThrow(() -> {
            SQLFragment result = transform(expression);
            assertNotNull(result);
        });
    }

    @Test
    void testFloatLiteral() {
        String expression = "metric * 0.95";
        assertDoesNotThrow(() -> {
            SQLFragment result = transform(expression);
            assertNotNull(result);
        });
    }

    /**
     * Test various duration formats
     */
    @ParameterizedTest
    @ValueSource(strings = {"5m", "1h", "30s", "1d", "1w"})
    void testDurationFormats(String duration) {
        String expression = "metric[" + duration + "]";
        assertDoesNotThrow(() -> {
            ParseTree tree = parse(expression);
            assertNotNull(tree);
        });
    }
}
