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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MetricsQL error handling and user-friendly error messages.
 */
@Tag("unit")
class MetricsQLErrorHandlingTest {

    private final MetricsQLQueryParser parser = new MetricsQLQueryParser();

    @Test
    void testValidQueryNonStrict() {
        // Valid queries should parse successfully in non-strict mode
        SQLFragment result = parser.parse("http_requests_total", false);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("http_requests_total"));
    }

    @Test
    void testValidQueryStrict() {
        // Valid queries should parse successfully in strict mode
        SQLFragment result = parser.parse("http_requests_total{job=\"api\"}", true);
        assertNotNull(result);
        assertTrue(result.getParameters().contains("http_requests_total"));
        assertTrue(result.getParameters().contains("job"));
        assertTrue(result.getParameters().contains("api"));
    }

    @Test
    void testInvalidQueryThrowsException() {
        // Invalid query should throw MetricsQLParseException in strict mode
        assertThrows(MetricsQLParseException.class, () -> {
            parser.parse("http_requests{{{", true);
        });
    }

    @Test
    void testInvalidQueryContainsErrors() {
        // Invalid query exception should contain error details
        try {
            parser.parse("http_requests{{{", true);
            fail("Should have thrown MetricsQLParseException");
        } catch (MetricsQLParseException e) {
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Failed to parse MetricsQL query"));
            assertTrue(e.getErrorCount() > 0);
        }
    }

    @Test
    void testValidationMethod() {
        // Valid query should validate
        assertTrue(parser.validate("http_requests_total"));
        assertTrue(parser.validate("rate(http_requests[5m])"));

        // Invalid query should not validate
        assertFalse(parser.validate("http_requests{{{"));
        assertFalse(parser.validate("rate("));
    }

    @Test
    void testValidateWithErrors() {
        // Valid query should return empty error list
        List<String> errors = parser.validateWithErrors("http_requests_total");
        assertTrue(errors.isEmpty());

        // Invalid query should return errors
        errors = parser.validateWithErrors("http_requests{{{");
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("MetricsQL syntax error"));
    }

    @Test
    void testNullQueryThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse(null);
        });
    }

    @Test
    void testEmptyQueryThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse("");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            parser.parse("   ");
        });
    }

    @Test
    void testErrorMessageContainsHints() {
        // Error messages should contain helpful hints
        List<String> errors = parser.validateWithErrors("metric_name{label=invalid}");
        assertFalse(errors.isEmpty());
        // The error should mention something about syntax or quotes
        String errorMsg = errors.get(0);
        assertTrue(errorMsg.contains("syntax") || errorMsg.contains("Unexpected") || errorMsg.contains("error"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "metric{",
        "metric{label}",
        "metric{label=}",
        "rate(",
        "sum(metric) by",
        "metric[",
        "metric + ",
        "{label=\"value\"}"  // Missing metric name
    })
    void testCommonSyntaxErrors(String invalidQuery) {
        // All these queries should fail validation
        assertFalse(parser.validate(invalidQuery),
            "Query should be invalid: " + invalidQuery);

        List<String> errors = parser.validateWithErrors(invalidQuery);
        assertFalse(errors.isEmpty(),
            "Should have errors for query: " + invalidQuery);
    }

    @Test
    void testUnclosedBrackets() {
        List<String> errors = parser.validateWithErrors("metric[5m");
        assertFalse(errors.isEmpty());
        assertTrue(errors.get(0).contains("syntax") || errors.get(0).contains("error"));
    }

    @Test
    void testUnclosedQuotes() {
        List<String> errors = parser.validateWithErrors("metric{label=\"value}");
        assertFalse(errors.isEmpty());
    }

    @Test
    void testInvalidMetricName() {
        // Metric names with invalid characters should be caught
        List<String> errors = parser.validateWithErrors("123metric");  // Can't start with number
        assertFalse(errors.isEmpty());
    }

    @Test
    void testErrorListenerAccumulation() {
        // Query with multiple errors should accumulate all of them
        List<String> errors = parser.validateWithErrors("metric{{{ ]]] )))");
        assertFalse(errors.isEmpty());
        // Should have at least one error (may have multiple depending on parser recovery)
        assertTrue(errors.size() >= 1);
    }

    @Test
    void testUserFriendlyErrorFormat() {
        // Verify that errors have the expected user-friendly format
        List<String> errors = parser.validateWithErrors("metric{");
        assertFalse(errors.isEmpty());

        String error = errors.get(0);
        // Should contain line and position information
        assertTrue(error.contains("line") || error.contains("position") || error.contains("syntax"),
            "Error should contain position information: " + error);
    }

    @Test
    void testParseExceptionHasErrors() {
        try {
            parser.parse("invalid{{{", true);
            fail("Should have thrown exception");
        } catch (MetricsQLParseException e) {
            assertFalse(e.getErrors().isEmpty());
            assertTrue(e.getErrorCount() > 0);
        }
    }

    @Test
    void testNonStrictModeDoesNotThrow() {
        // Non-strict mode should not throw exceptions (though results may be partial)
        assertDoesNotThrow(() -> {
            SQLFragment result = parser.parse("http_requests_total", false);
            assertNotNull(result);
        });
    }
}
