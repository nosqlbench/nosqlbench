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

import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests demonstrating complex MetricsQL queries.
 * Tests combinations of selectors, rollups, aggregations, transforms, and binary operations.
 *
 * <p>Comprehensive test coverage for real-world query scenarios.</p>
 */
@Tag("mql")
@Tag("unit")
class MetricsQLEndToEndTest {

    private Connection conn;
    private MetricsQLQueryParser parser;

    @BeforeEach
    void setUp() throws Exception {
        // Load test database
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        conn = DriverManager.getConnection(jdbcUrl);

        // Enable regex support
        RegexHelper.enableRegex(conn);

        // Create parser
        parser = new MetricsQLQueryParser();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Test
    void testCompleteQueryWorkflow() throws Exception {
        // Test a complete workflow: selector → transform → binary op
        // Query: abs(patterns_total) + 100
        String query = "abs(patterns_total) + 100";
        SQLFragment fragment = parser.parse(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("ABS"));
        assertTrue(fragment.getSql().contains("+"));

        // Execute
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Complex query should return results");
                double value = rs.getDouble("value");
                assertTrue(value >= 100, "Value should be at least 100 (due to +100)");
            }
        }
    }

    @Test
    void testRollupWithTransform() throws Exception {
        // Test combining rollup and transform: round(rate(patterns_total[5m]))
        String query = "round(rate(patterns_total[5m]))";
        SQLFragment fragment = parser.parse(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("ROUND"));
        assertTrue(fragment.getSql().contains("LAG"));

        // Execute
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Rate calculation rounded to integer
                    double value = rs.getDouble("value");
                    assertEquals(value, Math.round(value), 0.01,
                        "ROUND should return integer value");
                }
            }
        }
    }

    @Test
    void testTransformOfRollup() throws Exception {
        // Test transforming a rollup result: abs(rate(patterns_total[5m]))
        String query = "abs(rate(patterns_total[5m]))";
        SQLFragment fragment = parser.parse(query);

        assertNotNull(fragment);
        // Should have both ABS transform and rate calculation
        assertTrue(fragment.getSql().toUpperCase().contains("ABS"));
        assertTrue(fragment.getSql().toUpperCase().contains("LAG"));

        // Execute
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double value = rs.getDouble("value");
                    assertTrue(value >= 0, "Absolute value should be non-negative");
                }
            }
        }
    }

    @Test
    void testValidationWithErrors() {
        // Test validation with invalid queries (parse-time errors)
        String[] invalidQueries = {
            "metric{",                    // Unclosed brace
            "sum(metric) by",             // Incomplete by clause
            "metric{label=}"              // Missing label value
        };

        for (String query : invalidQueries) {
            assertFalse(parser.validate(query),
                "Query should be invalid: " + query);

            java.util.List<String> errors = parser.validateWithErrors(query);
            assertFalse(errors.isEmpty(),
                "Should have errors for: " + query);
        }

        // Note: Some errors are caught at transform time rather than parse time:
        // - rate(metric) - parses OK, fails when missing time range
        // - quantile_over_time(metric[5m]) - parses OK, fails when missing quantile param
        // This is expected behavior - parser validates syntax, transformer validates semantics
    }

    @Test
    void testValidComplexQueries() {
        // Test that complex valid queries parse successfully
        String[] validQueries = {
            "patterns_total",
            "patterns_total{pattern=\"linear\"}",
            "rate(patterns_total[5m])",
            "sum(patterns_total) by (pattern)",
            "abs(patterns_total)",
            "patterns_total + 100",
            "round(rate(patterns_total[5m]))",
            "abs(patterns_total) + 100"
        };

        for (String query : validQueries) {
            assertTrue(parser.validate(query),
                "Query should be valid: " + query);

            SQLFragment fragment = parser.parse(query);
            assertNotNull(fragment, "Should parse: " + query);
            assertNotNull(fragment.getSql(), "Should have SQL: " + query);
            assertFalse(fragment.getSql().contains("ERROR"),
                "SQL should not contain errors: " + query);
        }
    }

    @Test
    void testSecurityAllQueries() {
        // Verify all query types use parameterized SQL
        String[] queries = {
            "patterns_total",
            "rate(patterns_total[5m])",
            "sum(patterns_total)",
            "abs(patterns_total)",
            "patterns_total + 100"
        };

        for (String query : queries) {
            SQLFragment fragment = parser.parse(query);
            assertTrue(fragment.getSql().contains("?"),
                "Query should use parameters: " + query);
            assertFalse(fragment.getSql().contains("'patterns_total'"),
                "Query should not have literal metric names: " + query);
        }
    }

    @Test
    void testErrorMessagesAreHelpful() {
        // Verify error messages provide useful guidance
        String badQuery = "metric{";
        java.util.List<String> errors = parser.validateWithErrors(badQuery);

        assertFalse(errors.isEmpty());
        String errorMsg = errors.get(0);

        assertTrue(errorMsg.contains("syntax") || errorMsg.contains("error"),
            "Error message should mention syntax issue");
        assertTrue(errorMsg.contains("metric") || errorMsg.contains("pattern"),
            "Error message should provide examples");
    }

    @Test
    void testQueryPerformance() throws Exception {
        // Verify queries complete in reasonable time
        String[] queries = {
            "patterns_total",
            "rate(patterns_total[5m])",
            "sum(patterns_total)",
            "abs(rate(patterns_total[5m]))",
            "patterns_total + 100"
        };

        for (String query : queries) {
            long start = System.currentTimeMillis();

            SQLFragment fragment = parser.parse(query);

            try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
                for (int i = 0; i < fragment.getParameters().size(); i++) {
                    ps.setObject(i + 1, fragment.getParameters().get(i));
                }
                ps.executeQuery();
            }

            long duration = System.currentTimeMillis() - start;
            assertTrue(duration < 1000,
                "Query should complete in <1s: " + query + " took " + duration + "ms");
        }
    }
}
