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
 * Integration test for MetricsQL selector transformation to SQL.
 * Tests against a real SQLite database with test data.
 *
 * <p>Phase 2: Tests basic selectors with label filtering</p>
 * <p>Uses simple_counter.db: activity_ops_total with activity={read,write} labels</p>
 */
@Tag("mql")
class MetricsQLSelectorIntegrationTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Load test database: activity_ops_total with labels: activity={read,write}, host=server1
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        conn = DriverManager.getConnection(jdbcUrl);

        // Enable regex support for pattern matching tests
        RegexHelper.enableRegex(conn);
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
    void testSimpleSelector() throws Exception {
        String query = "activity_ops_total";
        SQLFragment fragment = parseAndTransform(query);

        // Execute the generated SQL
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            // Bind parameters
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    assertNotNull(rs.getObject("timestamp"));
                    assertNotNull(rs.getObject("value"));
                    assertNotNull(rs.getString("labels"));
                }

                // Should get 2 results (latest value for read and write)
                assertEquals(2, rowCount, "Should return latest value for each label combination");
            }
        }
    }

    @Test
    void testSelectorWithExactLabelMatch() throws Exception {
        String query = "activity_ops_total{activity=\"read\"}";
        SQLFragment fragment = parseAndTransform(query);

        // Execute the generated SQL
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            // Bind parameters
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    String labels = rs.getString("labels");
                    assertNotNull(labels);
                    assertTrue(labels.contains("activity=read"), "Labels should contain activity=read");
                }

                // Should get 1 result (only read activity)
                assertEquals(1, rowCount, "Should return only metrics with activity=read");
            }
        }
    }

    @Test
    void testSelectorWithMultipleLabels() throws Exception {
        String query = "activity_ops_total{activity=\"read\", host=\"server1\"}";
        SQLFragment fragment = parseAndTransform(query);

        // Execute the generated SQL
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            // Bind parameters
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    String labels = rs.getString("labels");
                    assertNotNull(labels);
                    assertTrue(labels.contains("activity=read"));
                    assertTrue(labels.contains("host=server1"));
                }

                // Should get 1 result (read on server1)
                assertEquals(1, rowCount, "Should return only metrics matching both labels");
            }
        }
    }

    @Test
    void testSelectorWithNotEqual() throws Exception {
        String query = "activity_ops_total{activity!=\"write\"}";
        SQLFragment fragment = parseAndTransform(query);

        // Execute the generated SQL
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            // Bind parameters
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    String labels = rs.getString("labels");
                    assertNotNull(labels);
                    assertFalse(labels.contains("activity=write"), "Should not include activity=write");
                }

                // Should get 1 result (only read, excluding write)
                assertEquals(1, rowCount, "Should exclude metrics with activity=write");
            }
        }
    }

    @Test
    void testParameterBinding() throws Exception {
        String query = "activity_ops_total{activity=\"read\"}";
        SQLFragment fragment = parseAndTransform(query);

        // Verify parameters are used (not concatenated)
        assertTrue(fragment.getSql().contains("?"),
            "SQL should use parameter placeholders");
        assertFalse(fragment.getSql().contains("'read'"),
            "SQL should not contain literal values - prevents SQL injection");

        // Verify parameters list contains expected values
        assertTrue(fragment.getParameters().contains("activity_ops_total"));
        assertTrue(fragment.getParameters().contains("activity"));
        assertTrue(fragment.getParameters().contains("read"));
    }

    @Test
    void testSQLInjectionPrevention() {
        // Try to inject SQL through metric name
        String maliciousMetric = "http_requests'; DROP TABLE sample_value; --";

        boolean inputWasRejected = false;

        try {
            String query = maliciousMetric;
            SQLFragment fragment = parseAndTransform(query);

            // If parsing succeeded, try to execute the query
            try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
                for (int i = 0; i < fragment.getParameters().size(); i++) {
                    ps.setObject(i + 1, fragment.getParameters().get(i));
                }
                ps.executeQuery();
            }

            // If we get here, the input was accepted. Verify it was safely parameterized
            // Either the malicious string is bound as a parameter (safe) or the SQL uses placeholders
            assertTrue(fragment.getSql().contains("?"),
                "If malicious input is accepted, SQL must use parameterized queries");

        } catch (IllegalArgumentException e) {
            // Expected: validation should reject invalid metric names
            assertTrue(e.getMessage().contains("Invalid metric name"));
            inputWasRejected = true;
        } catch (Exception e) {
            // Parse errors or other exceptions are also valid rejection mechanisms
            // This is acceptable protection against SQL injection
            inputWasRejected = true;
        }

        // Key assertion: Verify tables still exist (SQL injection was prevented)
        assertDoesNotThrow(() -> {
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM sample_value")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) > 0, "Table should still have data");
            }
        }, "Database tables should still exist - SQL injection was prevented");
    }
}
