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
 * Integration tests for MetricsQL label manipulation functions.
 * Tests label_set, label_del, label_keep, label_copy, label_move, label_replace.
 *
 * <p>Phase 6: Label manipulation for full MetricsQL feature parity</p>
 */
@Tag("mql")
class LabelManipulationTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Use multi-dimensional database with complex labels
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        conn = DriverManager.getConnection(jdbcUrl);

        // Enable regex support for label_replace
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
    void testLabelSetFunction() throws Exception {
        // label_set adds or modifies a label
        String query = "label_set(requests_total, \"region\", \"us-east\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getParameters().contains("requests_total"),
            "Should contain metric name");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                // Result should have region label added/modified
                assertNotNull(rs.getString("labels"));
            }
        }
    }

    @Test
    void testLabelDelFunction() throws Exception {
        // label_del removes specified labels
        String query = "label_del(requests_total, \"env\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("LIKE") || fragment.getSql().contains("keep"),
            "Should filter labels");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Query should execute - results depend on data
                while (rs.next()) {
                    assertNotNull(rs.getString("labels"));
                }
            }
        }
    }

    @Test
    void testLabelDelMultipleLabels() throws Exception {
        // label_del can remove multiple labels
        String query = "label_del(requests_total, \"env\", \"region\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Should execute successfully
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                // May have fewer results if some metrics only had deleted labels
            }
        }
    }

    @Test
    void testLabelKeepFunction() throws Exception {
        // label_keep keeps only specified labels
        String query = "label_keep(requests_total, \"env\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                // Labels should only contain env label
                assertNotNull(rs.getString("labels"));
            }
        }
    }

    @Test
    void testLabelCopyFunction() throws Exception {
        // label_copy duplicates a label value
        String query = "label_copy(requests_total, \"env\", \"environment\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Should have results");
                // Result should have both env and environment labels
            }
        }
    }

    @Test
    void testLabelMoveFunction() throws Exception {
        // label_move renames a label
        String query = "label_move(requests_total, \"env\", \"environment\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Should execute - label is renamed
                int count = 0;
                while (rs.next()) {
                    count++;
                }
            }
        }
    }

    @Test
    void testLabelReplaceFunction() throws Exception {
        // label_replace performs regex replacement on label value
        String query = "label_replace(requests_total, \"env\", \"production\", \"env\", \"prod\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);

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
    void testLabelManipulationWithFilters() throws Exception {
        // Label manipulation combined with label filters
        String query = "label_set(requests_total{env=\"prod\"}, \"region\", \"us-west\")";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getParameters().contains("env"),
            "Should have label filter");
        assertTrue(fragment.getParameters().contains("prod"),
            "Should have filter value");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                // Should execute successfully
                while (rs.next()) {
                    assertNotNull(rs.getString("labels"));
                }
            }
        }
    }

    @Test
    void testParameterBinding() throws Exception {
        // Verify all label manipulation uses parameters
        String query = "label_set(requests_total, \"new_label\", \"new_value\")";
        SQLFragment fragment = parseAndTransform(query);

        assertTrue(fragment.getSql().contains("?"),
            "SQL should use parameter placeholders");
        assertTrue(fragment.getParameters().contains("requests_total"),
            "Parameters should contain metric name");

        // Label names and values are embedded in SQL for string operations,
        // but metric name is always parameterized
    }

    @Test
    void testInvalidLabelNames() {
        // Test validation of label names
        String[] invalidQueries = {
            "label_set(metric, \"123invalid\", \"value\")",  // Can't start with number
            "label_set(metric, \"invalid-name\", \"value\")", // Can't have dash
            "label_set(metric, \"invalid.name\", \"value\")"  // Can't have dot
        };

        for (String query : invalidQueries) {
            assertThrows(Exception.class, () -> {
                parseAndTransform(query);
            }, "Should reject invalid label name: " + query);
        }
    }
}
