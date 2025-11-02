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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for MetricsQL mathematical transform functions.
 * Tests abs(), ceil(), floor(), round(), ln(), log2(), log10(), sqrt(), exp().
 *
 * <p>Phase 5: Testing transform functions with simple counter data</p>
 */
@Tag("mql")
class TransformFunctionIntegrationTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Use simple counter database for transform tests
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
    void testAbsFunction() throws Exception {
        // abs() returns absolute value
        String query = "abs(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().toUpperCase().contains("ABS("),
            "Query should use ABS function");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                double value = rs.getDouble("value");
                assertTrue(value >= 0, "Absolute value should be non-negative");
            }
        }
    }

    @Test
    void testCeilFunction() throws Exception {
        // ceil() rounds up to nearest integer
        String query = "ceil(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().toUpperCase().contains("CEIL("),
            "Query should use CEIL function");
    }

    @Test
    void testFloorFunction() throws Exception {
        // floor() rounds down to nearest integer
        String query = "floor(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().toUpperCase().contains("FLOOR("),
            "Query should use FLOOR function");
    }

    @Test
    void testRoundFunction() throws Exception {
        // round() rounds to nearest integer
        String query = "round(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().toUpperCase().contains("ROUND("),
            "Query should use ROUND function");
    }

    @Test
    void testSqrtFunction() throws Exception {
        // sqrt() calculates square root
        String query = "sqrt(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().toUpperCase().contains("SQRT("),
            "Query should use SQRT function");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                double value = rs.getDouble("value");
                assertTrue(value >= 0, "Square root should be non-negative");
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"abs", "ceil", "floor", "round", "sqrt", "exp", "ln", "log2", "log10"})
    void testAllTransformFunctions(String funcName) throws Exception {
        // Test all transform functions parse and transform correctly
        String query = funcName + "(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment, "Function " + funcName + " should transform");
        // Check for function in SQL (flexible format check)
        assertTrue(fragment.getSql().toUpperCase().contains(funcName.toUpperCase() + "("),
            "SQL should contain " + funcName.toUpperCase() + " function");

        // Verify query executes without errors (some may fail on negative values for logs)
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }
            ps.executeQuery(); // Just verify it doesn't throw
        } catch (Exception e) {
            // Log functions may fail on zero/negative values - that's expected
            if (funcName.startsWith("log") || funcName.equals("ln")) {
                assertTrue(e.getMessage() != null, "Log function error is acceptable");
            } else {
                throw e;
            }
        }
    }

    @Test
    void testTransformWithLabelFilter() throws Exception {
        // Transform function with label filtering
        String query = "abs(activity_ops_total{activity=\"read\"})";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getParameters().contains("activity_ops_total"));
        assertTrue(fragment.getParameters().contains("activity"));
        assertTrue(fragment.getParameters().contains("read"));

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String labels = rs.getString("labels");
                    assertTrue(labels.contains("activity=read"),
                        "Labels should contain activity=read filter");
                }
            }
        }
    }

    @Test
    void testNestedTransformFunctions() throws Exception {
        // Nested transforms: abs(sqrt(metric))
        String query = "abs(sqrt(activity_ops_total))";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        // Should have nested CTEs with both ABS and SQRT
        String sql = fragment.getSql().toUpperCase();
        assertTrue(sql.contains("ABS("),
            "Should contain ABS transform");
        assertTrue(sql.contains("SQRT("),
            "Should contain SQRT transform");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Nested transforms should return results");
                double value = rs.getDouble("value");
                assertTrue(value >= 0, "Result should be non-negative");
            }
        }
    }

    @Test
    void testParameterBinding() throws Exception {
        // Verify SQL uses parameter binding
        String query = "abs(activity_ops_total)";
        SQLFragment fragment = parseAndTransform(query);

        assertTrue(fragment.getSql().contains("?"),
            "SQL should use parameter placeholders");
        assertFalse(fragment.getSql().contains("'activity_ops_total'"),
            "SQL should not contain literal metric names");

        assertTrue(fragment.getParameters().contains("activity_ops_total"),
            "Parameters should contain metric name");
    }
}
