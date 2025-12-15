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
 * Integration tests for MetricsQL regex label matching.
 * Tests =~ (regex match) and !~ (negative regex match) operators.
 *
 * <p>Phase 8: Advanced feature - regex label matching</p>
 */
@Tag("mql")
class RegexLabelMatchingTest {

    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        // Load test database with various label values for regex testing
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);
        String jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        conn = DriverManager.getConnection(jdbcUrl);

        // Enable regex support on the connection
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
    void testRegexLabelMatch() throws Exception {
        // Test regex label matching: =~
        String query = "activity_ops_total{activity=~\"re.*\"}";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("REGEXP"),
            "Regex matching should use REGEXP operator");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                boolean foundRead = false;
                while (rs.next()) {
                    String labels = rs.getString("labels");
                    if (labels.contains("activity=read")) {
                        foundRead = true;
                    }
                    assertFalse(labels.contains("activity=write"),
                        "Pattern 're.*' should not match 'write'");
                }
                assertTrue(foundRead, "Should match activity=read (starts with 're')");
            }
        }
    }

    @Test
    void testNegativeRegexLabelMatch() throws Exception {
        // Test negative regex label matching: !~
        String query = "activity_ops_total{activity!~\"wr.*\"}";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getSql().contains("REGEXP"),
            "Negative regex should use REGEXP operator");
        assertTrue(fragment.getSql().contains("NOT IN"),
            "Negative regex should use NOT IN");

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String labels = rs.getString("labels");
                    assertFalse(labels.contains("activity=write"),
                        "Should not match activity=write (starts with 'wr')");
                }
            }
        }
    }

    @Test
    void testRegexWithAlternation() throws Exception {
        // Test regex with alternation: ^(read|write)$
        String query = "activity_ops_total{activity=~\"^(read|write)$\"}";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);
        assertTrue(fragment.getParameters().contains("activity"));
        assertTrue(fragment.getParameters().contains("^(read|write)$"));

        // Execute and verify (should match both read and write)
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                }
                assertEquals(2, count, "Should match both read and write");
            }
        }
    }

    @Test
    void testRegexCaseInsensitive() throws Exception {
        // Test case-insensitive regex
        String query = "activity_ops_total{activity=~\"(?i)READ\"}";
        SQLFragment fragment = parseAndTransform(query);

        assertNotNull(fragment);

        // Execute and verify
        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                boolean foundMatch = false;
                while (rs.next()) {
                    foundMatch = true;
                    String labels = rs.getString("labels");
                    assertTrue(labels.contains("activity=read"),
                        "Case-insensitive pattern should match 'read'");
                }
                assertTrue(foundMatch, "Should find at least one match");
            }
        }
    }

    @Test
    void testRegexParameterBinding() throws Exception {
        // Verify regex patterns are parameterized
        String query = "activity_ops_total{activity=~\"test.*\"}";
        SQLFragment fragment = parseAndTransform(query);

        assertTrue(fragment.getSql().contains("?"),
            "SQL should use parameter placeholders");
        assertTrue(fragment.getParameters().contains("activity"),
            "Parameters should contain label name");
        assertTrue(fragment.getParameters().contains("test.*"),
            "Parameters should contain regex pattern");
    }
}
