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

import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
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
 * Integration tests for MetricsQL rollup functions.
 * Tests rate(), increase(), avg_over_time(), and other rollup functions
 * against real SQLite database with time series data.
 *
 * <p>Phase 3: Testing rollup functions with window calculations</p>
 */
@Tag("mql")
class RollupFunctionIntegrationTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Load test database with predictable growth patterns for rate testing
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        conn = DriverManager.getConnection(jdbcUrl);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    private SQLFragment parseAndTransform(String metricsQL) {
        MetricsQLLexer lexer = new MetricsQLLexer(CharStreams.fromString(metricsQL));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MetricsQLParser parser = new MetricsQLParser(tokens);
        MetricsQLTransformer transformer = new MetricsQLTransformer();
        return transformer.visit(parser.query());
    }

    @Test
    void testRateFunctionBasic() throws Exception {
        // rate() calculates per-second rate of increase for counters
        String query = "rate(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("LAG("),
            "Rate calculation should use window function LAG");

        // Execute the query
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Rate query should return results");

                // Verify we have the expected columns
                assertNotNull(rs.getObject("timestamp"));
                assertNotNull(rs.getObject("value"));
                assertNotNull(rs.getString("labels"));

                // Rate should be a positive number for monotonically increasing counter
                double rate = rs.getDouble("value");
                assertTrue(rate >= 0, "Rate should be non-negative for counter");
            }
        }
    }

    @Test
    void testRateFunctionWithLabels() throws Exception {
        // Test rate() with label filtering
        String query = "rate(patterns_total{pattern=\"linear\"}[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getParameters().contains("patterns_total"));
        assertTrue(fragment.getParameters().contains("pattern"));
        assertTrue(fragment.getParameters().contains("linear"));

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String labels = rs.getString("labels");
                    assertTrue(labels.contains("pattern=linear"),
                        "Labels should contain pattern=linear filter");
                }
            }
        }
    }

    @Test
    void testIncreaseFunctionBasic() throws Exception {
        // increase() calculates total increase over time window with counter reset detection
        String query = "increase(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        // Updated: Now uses LAG window functions with counter reset detection
        assertTrue(fragment.getSql().contains("LAG(value)"),
            "Increase should use LAG window function for reset detection");
        assertTrue(fragment.getSql().contains("SUM(increase)"),
            "Increase should sum incremental changes");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Increase query should return results");

                double increase = rs.getDouble("value");
                assertTrue(increase >= 0, "Increase should be non-negative for counter");
            }
        }
    }

    @Test
    void testAvgOverTimeFunction() throws Exception {
        // avg_over_time() calculates average value over time window
        String query = "avg_over_time(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("AVG(value)"),
            "avg_over_time should use AVG aggregate");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble("value");
                    assertTrue(avg >= 0, "Average should be non-negative");
                }
            }
        }
    }

    @Test
    void testSumOverTimeFunction() throws Exception {
        // sum_over_time() calculates sum of values over time window
        String query = "sum_over_time(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("SUM(value)"),
            "sum_over_time should use SUM aggregate");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double sum = rs.getDouble("value");
                    assertTrue(sum >= 0, "Sum should be non-negative");
                }
            }
        }
    }

    @Test
    void testMinOverTimeFunction() throws Exception {
        // min_over_time() finds minimum value over time window
        String query = "min_over_time(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("MIN(value)"),
            "min_over_time should use MIN aggregate");
    }

    @Test
    void testMaxOverTimeFunction() throws Exception {
        // max_over_time() finds maximum value over time window
        String query = "max_over_time(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("MAX(value)"),
            "max_over_time should use MAX aggregate");
    }

    @Test
    void testCountOverTimeFunction() throws Exception {
        // count_over_time() counts number of samples over time window
        String query = "count_over_time(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("COUNT(*)"),
            "count_over_time should use COUNT aggregate");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("value");
                    assertTrue(count > 0, "Count should be positive");
                }
            }
        }
    }

    @Test
    void testFunctionWithoutTimeRangeThrowsError() {
        // Rollup functions require a time range
        String query = "rate(patterns_total)";

        assertThrows(IllegalArgumentException.class, () -> {
            parseAndTransform(query);
        }, "Rollup function without time range should throw error");
    }

    @Test
    void testParameterBinding() throws Exception {
        // Verify SQL uses parameter binding for security
        String query = "rate(patterns_total[5m])";
        SQLFragment fragment = parseAndTransform(query);

        assertTrue(fragment.getSql().contains("?"),
            "SQL should use parameter placeholders");
        assertFalse(fragment.getSql().contains("'patterns_total'"),
            "SQL should not contain literal values");

        assertTrue(fragment.getParameters().contains("patterns_total"),
            "Parameters should contain metric name");
    }

    @Test
    void testDifferentTimeWindows() throws Exception {
        // Test different time window formats
        String[] timeWindows = {"30s", "5m", "1h"};

        for (String window : timeWindows) {
            String query = "rate(patterns_total[" + window + "])";
            SQLFragment fragment = parseAndTransform(query);

            assertNotNull(fragment, "Query with " + window + " should transform");

            // Verify query executes without errors
            try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
                for (int i = 0; i < fragment.getParameters().size(); i++) {
                    ps.setObject(i + 1, fragment.getParameters().get(i));
                }
                ps.executeQuery(); // Just verify it doesn't throw
            }
        }
    }
}
