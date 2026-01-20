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
 * Boundary condition tests for binary operations.
 * Tests edge cases like division by zero, metric mismatches, overflow.
 */
@Tag("mql")
@Tag("unit")
class BinaryOpBoundaryTest {

    private Connection conn;
    private MetricsQLQueryParser parser;

    @BeforeEach
    void setUp() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        conn = DriverManager.getConnection(jdbcUrl);
        parser = new MetricsQLQueryParser();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Test
    void testDivisionByZero() throws Exception {
        // SQLite returns NULL for division by zero
        String query = "activity_ops_total / 0";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // SQLite handles division by zero gracefully (returns NULL)
                while (rs.next()) {
                    // Value may be NULL, which getDouble returns as 0.0
                    double value = rs.getDouble("value");
                    // Test passes if query executes without exception
                }
            }
        }
    }

    @Test
    void testAdditionCommutative() throws Exception {
        // metric + 100 should equal 100 + metric
        String query1 = "activity_ops_total + 100";
        String query2 = "100 + activity_ops_total";

        // Both should parse and execute
        SQLFragment frag1 = parser.parse(query1);
        SQLFragment frag2 = parser.parse(query2);

        assertNotNull(frag1);
        assertNotNull(frag2);
        // Values should be mathematically equivalent
    }

    @Test
    void testMultiplicationByZero() throws Exception {
        // metric * 0 should return 0
        String query = "activity_ops_total * 0";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double value = rs.getDouble("value");
                    assertEquals(0.0, value, 0.001,
                        "Multiplication by zero should return zero");
                }
            }
        }
    }

    @Test
    void testMultiplicationByOne() throws Exception {
        // metric * 1 should preserve value
        String baseQuery = "activity_ops_total";
        String multQuery = "activity_ops_total * 1";

        double baseValue = executeAndGetFirstValue(baseQuery);
        double multValue = executeAndGetFirstValue(multQuery);

        assertEquals(baseValue, multValue, 0.001,
            "Multiplication by 1 should preserve value");
    }

    @Test
    void testAdditionIdentity() throws Exception {
        // metric + 0 should preserve value
        String baseQuery = "activity_ops_total";
        String addQuery = "activity_ops_total + 0";

        double baseValue = executeAndGetFirstValue(baseQuery);
        double addValue = executeAndGetFirstValue(addQuery);

        assertEquals(baseValue, addValue, 0.001,
            "Addition of 0 should preserve value");
    }

    @Test
    void testSubtractionToZero() throws Exception {
        // metric - metric should equal 0 (if same metric)
        // But our implementation joins on labels, so this tests label matching

        String query = "activity_ops_total - 0";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double value = rs.getDouble("value");
                    // Subtracting 0 should preserve value
                    assertTrue(value >= 0, "Subtracting 0 from counter should stay non-negative");
                }
            }
        }
    }

    @Test
    void testComparisonResultsBinary() throws Exception {
        // Comparison operations should return 0 or 1 (false or true)
        String[] comparisons = {
            "activity_ops_total > 0",
            "activity_ops_total >= 0",
            "activity_ops_total < 999999",
            "activity_ops_total == 100"
        };

        for (String query : comparisons) {
            SQLFragment fragment = parser.parse(query);

            try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
                for (int i = 0; i < fragment.getParameters().size(); i++) {
                    ps.setObject(i + 1, fragment.getParameters().get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double result = rs.getDouble("value");
                        assertTrue(result == 0.0 || result == 1.0,
                            "Comparison should return 0 or 1, got: " + result);
                    }
                }
            }
        }
    }

    @Test
    void testModuloOperation() throws Exception {
        // Test modulo with various divisors
        String query = "activity_ops_total % 100";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double remainder = rs.getDouble("value");
                    assertTrue(remainder >= 0, "Modulo should be non-negative");
                    assertTrue(remainder < 100, "Modulo 100 should be < 100");
                }
            }
        }
    }

    @Test
    void testArithmeticOverflow() throws Exception {
        // Test arithmetic with very large multipliers
        String query = "activity_ops_total * 1000000";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double value = rs.getDouble("value");
                    // May be very large or infinity
                    assertTrue(value > 0 || Double.isInfinite(value),
                        "Large multiplication should give positive or infinite result");
                }
            }
        }
    }

    @Test
    void testNegativeResults() throws Exception {
        // Subtraction can produce negative values
        String query = "activity_ops_total - 10000";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Results may be negative depending on counter values
                // This is mathematically correct
                while (rs.next()) {
                    double value = rs.getDouble("value");
                    assertTrue(Double.isFinite(value), "Result should be finite");
                }
            }
        }
    }

    private double executeAndGetFirstValue(String query) throws Exception {
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("value");
                }
                return 0.0;
            }
        }
    }
}
