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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for counter reset detection in rate() and increase() functions.
 * Equivalent to VictoriaMetrics TestRemoveCounterResets functionality.
 */
@Tag("mql")
@Tag("unit")
class CounterResetTest {
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Create in-memory SQLite database
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        // Create schema using DDL statements directly
        createSchema();

        // Insert test data with counter resets
        insertTestData();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    private void createSchema() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE sample_name (id INTEGER PRIMARY KEY, sample TEXT NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE label_set (id INTEGER PRIMARY KEY, hash TEXT NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE label_key (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE label_value (id INTEGER PRIMARY KEY, value TEXT NOT NULL UNIQUE)");
            stmt.execute("CREATE TABLE label_set_membership (label_set_id INTEGER, label_key_id INTEGER, label_value_id INTEGER)");
            stmt.execute("CREATE TABLE metric_instance (id INTEGER PRIMARY KEY, sample_name_id INTEGER, label_set_id INTEGER)");
            stmt.execute("CREATE TABLE sample_value (id INTEGER PRIMARY KEY AUTOINCREMENT, metric_instance_id INTEGER, timestamp_ms INTEGER, value REAL)");
        }
    }

    private SQLFragment parseAndTransform(String metricsQL) {
        MetricsQLLexer lexer = new MetricsQLLexer(CharStreams.fromString(metricsQL));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MetricsQLParser parser = new MetricsQLParser(tokens);
        MetricsQLTransformer transformer = new MetricsQLTransformer();
        return transformer.visit(parser.query());
    }

    private void insertTestData() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // Create metric
            stmt.execute("INSERT INTO sample_name (id, sample) VALUES (1, 'test_counter')");
            stmt.execute("INSERT INTO label_set (id, hash) VALUES (1, '{}')");
            stmt.execute("INSERT INTO metric_instance (id, sample_name_id, label_set_id) " +
                        "VALUES (1, 1, 1)");

            // Insert values with counter resets
            // Pattern: 100 -> 150 -> 200 -> 50 (RESET) -> 100 -> 150
            long baseTime = System.currentTimeMillis();

            // Normal increases
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 150.0)", baseTime + 60000)); // +1min, increase=50
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 200.0)", baseTime + 120000)); // +2min, increase=50

            // Counter reset
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 50.0)", baseTime + 180000)); // +3min, RESET (value < prev)

            // Continue after reset
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime + 240000)); // +4min, increase=50
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 150.0)", baseTime + 300000)); // +5min, increase=50
        }
    }

    @Test
    void testRateDetectsCounterReset() throws Exception {
        // rate() should detect the counter reset and not return negative values
        String query = "rate(test_counter[10m])";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasNegativeRate = false;
                while (rs.next()) {
                    double rate = rs.getDouble("value");
                    if (rate < 0) {
                        hasNegativeRate = true;
                    }
                }
                assertFalse(hasNegativeRate,
                    "rate() should not return negative values when counter resets");
            }
        }
    }

    @Test
    void testIncreaseHandlesCounterReset() throws Exception {
        // increase() should handle counter resets correctly
        // Total should be: 50 + 50 + 50 + 50 + 50 = 250 (not 150 - 100 = 50)
        String query = "increase(test_counter[10m])";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should return results");
                double increase = rs.getDouble("value");

                // With reset detection: 50+50+50+50+50 = 250
                // Without reset detection: MAX(150) - MIN(50) = 100
                assertTrue(increase > 150,
                    "increase() with reset should be > 150 (got " + increase + ")");
                assertEquals(250.0, increase, 10.0,
                    "increase() should sum all increments including across reset");
            }
        }
    }

    @Test
    void testMultipleResets() throws Exception {
        // Test with multiple counter resets
        try (Statement stmt = conn.createStatement()) {
            // Clear and insert data with multiple resets
            stmt.execute("DELETE FROM sample_value");

            long baseTime = System.currentTimeMillis();
            // Pattern: 100 -> 50 (reset) -> 100 -> 30 (reset) -> 80
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 50.0)", baseTime + 60000)); // Reset
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime + 120000));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 30.0)", baseTime + 180000)); // Reset again
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 80.0)", baseTime + 240000));
        }

        String query = "increase(test_counter[10m])";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                double increase = rs.getDouble("value");

                // Expected: 50 (reset) + 50 + 30 (reset) + 50 = 180
                // Without reset detection: MAX(100) - MIN(30) = 70
                assertTrue(increase > 100,
                    "Should handle multiple resets correctly (got " + increase + ")");
            }
        }
    }

    @Test
    void testNoResetMonotonicallyIncreasing() throws Exception {
        // Verify normal behavior without resets still works
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM sample_value");

            long baseTime = System.currentTimeMillis();
            // No resets: 100 -> 150 -> 200 -> 250
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 150.0)", baseTime + 60000));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 200.0)", baseTime + 120000));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 250.0)", baseTime + 180000));
        }

        String query = "increase(test_counter[10m])";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                double increase = rs.getDouble("value");

                // Expected: 50 + 50 + 50 = 150
                assertEquals(150.0, increase, 5.0,
                    "Should correctly calculate increase without resets");
            }
        }
    }

    @Test
    void testRateAfterReset() throws Exception {
        // Verify rate calculation immediately after a reset
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM sample_value");

            long baseTime = System.currentTimeMillis();
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 1000.0)", baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime + 60000)); // Reset to 100
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 160.0)", baseTime + 120000)); // +60 in 60s = 1/sec
        }

        String query = "rate(test_counter[10m])";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Should have rates for transitions
                int count = 0;
                boolean hasValidRates = true;
                while (rs.next()) {
                    double rate = rs.getDouble("value");
                    count++;
                    // All rates should be >= 0 (no negative rates from reset)
                    if (rate < 0) {
                        hasValidRates = false;
                    }
                }
                assertTrue(count > 0, "Should return rate values");
                assertTrue(hasValidRates, "All rates should be non-negative");
            }
        }
    }

    @Test
    void testResetToZero() throws Exception {
        // Test reset specifically to zero
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM sample_value");

            long baseTime = System.currentTimeMillis();
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 100.0)", baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 0.0)", baseTime + 60000)); // Reset to 0
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) " +
                "VALUES (1, %d, 50.0)", baseTime + 120000));
        }

        String query = "increase(test_counter[10m])";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                double increase = rs.getDouble("value");

                // Expected: 0 (reset from 100) + 50 = 50
                // The first transition 100->0 is a reset, so we use 0 as the increase
                // Then 0->50 = 50
                assertTrue(increase >= 40 && increase <= 60,
                    "Should handle reset to zero correctly (got " + increase + ")");
            }
        }
    }
}
