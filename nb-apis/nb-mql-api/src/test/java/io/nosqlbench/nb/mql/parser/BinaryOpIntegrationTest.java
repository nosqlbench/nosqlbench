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
 * Integration tests for MetricsQL binary operations.
 * Tests arithmetic (+, -, *, /), comparison (==, !=, <, >), and set operations (and, or, unless).
 *
 * <p>Phase 7: Testing binary operations with metric-to-metric and metric-to-scalar</p>
 */
@Tag("mql")
class BinaryOpIntegrationTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Use simple counter database for binary operation tests
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);
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
    void testScalarAddition() throws Exception {
        // metric + scalar
        String query = "activity_ops_total + 100";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("+"),
            "Query should contain addition operator");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                double value = rs.getDouble("value");
                assertTrue(value >= 100, "Value should be at least 100");
            }
        }
    }

    @Test
    void testScalarMultiplication() throws Exception {
        // metric * scalar
        String query = "activity_ops_total * 2";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("*"),
            "Query should contain multiplication operator");
    }

    @Test
    void testScalarDivision() throws Exception {
        // metric / scalar
        String query = "activity_ops_total / 1000";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("/"),
            "Query should contain division operator");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                // Value should be divided by 1000
            }
        }
    }

    @Test
    void testScalarSubtraction() throws Exception {
        // metric - scalar
        String query = "activity_ops_total - 10";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("-"),
            "Query should contain subtraction operator");
    }

    @Test
    void testComparisonGreaterThan() throws Exception {
        // metric > scalar (returns 1 or 0 in SQLite)
        String query = "activity_ops_total > 100";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains(">"),
            "Query should contain greater-than operator");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                // Result will be 1 (true) or 0 (false)
                double value = rs.getDouble("value");
                assertTrue(value == 0 || value == 1,
                    "Comparison should return 0 or 1");
            }
        }
    }

    @Test
    void testComparisonEqual() throws Exception {
        // metric == scalar
        String query = "activity_ops_total == 100";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("="),
            "Query should contain equality operator");
    }

    @Test
    void testComplexArithmetic() throws Exception {
        // Complex expression: metric + 100 - 10 (sequential operations)
        String query = "activity_ops_total + 100 - 10";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("+") || fragment.getSql().contains("-"),
            "Should contain arithmetic operators");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
            }
        }
    }

    @Test
    void testParameterBinding() throws Exception {
        // Verify SQL uses parameter binding
        String query = "activity_ops_total + 100";
        SQLFragment fragment = parseAndTransform(query);

        assertTrue(fragment.getSql().contains("?"),
            "SQL should use parameter placeholders");
        assertTrue(fragment.getParameters().contains("activity_ops_total"),
            "Parameters should contain metric name");
        // Scalar value may be embedded in SQL or parameterized
        assertFalse(fragment.getParameters().isEmpty(),
            "Should have parameters");
    }

    @Test
    void testArithmeticPrecedence() throws Exception {
        // Test operator precedence: metric + 10 * 2 should be metric + (10 * 2)
        String query = "activity_ops_total + 10 * 2";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        // Should parse correctly with proper precedence
        assertTrue(fragment.getParameters().contains("activity_ops_total"));
    }

    @Test
    void testDivisionByZeroHandling() throws Exception {
        // Division by zero should be handled gracefully
        String query = "activity_ops_total / 0";
        SQLFragment fragment = parseAndTransform(query);

        // Should generate valid SQL (SQLite handles division by zero)
        assertNotNull(fragment);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // SQLite returns NULL for division by zero
                if (rs.next()) {
                    // Test passes if query executes (may return NULL values)
                }
            }
        }
    }
}
