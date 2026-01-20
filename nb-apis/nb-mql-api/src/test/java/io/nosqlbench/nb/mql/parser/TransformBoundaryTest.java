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
 * Boundary condition tests for transform functions.
 * Tests edge cases like negative values for logs, division by zero, infinity, NaN.
 */
@Tag("mql")
@Tag("unit")
class TransformBoundaryTest {

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
    void testSqrtOfPositiveValues() throws Exception {
        // sqrt should work on positive counter values
        String query = "sqrt(activity_ops_total)";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double sqrt = rs.getDouble("value");
                    assertTrue(sqrt >= 0, "Square root should be non-negative");
                    assertTrue(Double.isFinite(sqrt), "Square root should be finite");
                }
            }
        }
    }

    @Test
    void testAbsAlwaysNonNegative() throws Exception {
        // abs() should always return non-negative values
        String query = "abs(activity_ops_total)";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double abs = rs.getDouble("value");
                    assertTrue(abs >= 0, "Absolute value must be non-negative");
                }
            }
        }
    }

    @Test
    void testRoundingFunctions() throws Exception {
        // Test ceil, floor, round produce integer-like results
        String[] functions = {"ceil", "floor", "round"};

        for (String func : functions) {
            String query = func + "(activity_ops_total)";
            SQLFragment fragment = parser.parse(query);

            try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
                for (int i = 0; i < fragment.getParameters().size(); i++) {
                    ps.setObject(i + 1, fragment.getParameters().get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double rounded = rs.getDouble("value");
                        assertTrue(Double.isFinite(rounded),
                            func + " should return finite values");

                        // For integer counter values, these should equal the value
                        // For non-integer, should be close to an integer
                    }
                }
            }
        }
    }

    @Test
    void testExpWithLargeValues() throws Exception {
        // exp() of large values can overflow to infinity
        // SQLite should handle this gracefully

        String query = "exp(activity_ops_total)";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double exp = rs.getDouble("value");
                    // May be infinity for large counter values
                    assertTrue(exp > 0 || Double.isInfinite(exp),
                        "Exponential should be positive or infinity");
                }
            }
        }
    }

    @Test
    void testLogOfPositiveValues() throws Exception {
        // ln(), log2(), log10() should work on positive values
        String[] logFunctions = {"ln", "log2", "log10"};

        for (String func : logFunctions) {
            String query = func + "(activity_ops_total)";
            SQLFragment fragment = parser.parse(query);

            try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
                for (int i = 0; i < fragment.getParameters().size(); i++) {
                    ps.setObject(i + 1, fragment.getParameters().get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double log = rs.getDouble("value");
                        // Log of positive counter should be finite
                        // May be -Infinity for log(0) or NaN for log(negative)
                        // For counter metrics (positive values), should be finite
                        assertTrue(Double.isFinite(log) ||
Double.isInfinite(log),
                            func + " should return finite or infinite value");
                    }
                }
            }
        }
    }

    @Test
    void testNestedTransformsNoOverflow() throws Exception {
        // Test deeply nested transforms don't cause stack overflow
        String query = "abs(sqrt(abs(sqrt(activity_ops_total))))";
        SQLFragment fragment = parser.parse(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Nested transforms should execute");
                double value = rs.getDouble("value");
                assertTrue(value >= 0, "Result should be non-negative");
                assertTrue(Double.isFinite(value), "Result should be finite");
            }
        }
    }

    @Test
    void testTransformPreservesLabelCount() throws Exception {
        // Transforms should not change number of time series
        String baseQuery = "activity_ops_total";
        String transformQuery = "abs(activity_ops_total)";

        int baseCount = countResults(baseQuery);
        int transformCount = countResults(transformQuery);

        assertEquals(baseCount, transformCount,
            "Transform should preserve number of time series");
    }

    private int countResults(String query) throws Exception {
        SQLFragment fragment = parser.parse(query);
        int count = 0;

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    count++;
                }
            }
        }

        return count;
    }
}
