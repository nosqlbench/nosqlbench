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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for canonical label-specific grouping behavior.
 * Verifies that aggregations with "by (label)" extract only specified labels
 * and collapse others, matching VictoriaMetrics/PromQL semantics.
 */
@Tag("mql")
@Tag("unit")
class CanonicalLabelGroupingTest {
    private Connection conn;

    @BeforeEach
    void setUp() throws Exception {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");

        createSchema();
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
            stmt.execute("INSERT INTO sample_name (id, sample) VALUES (1, 'http_requests')");

            // Create label keys
            stmt.execute("INSERT INTO label_key (id, name) VALUES (1, 'env')");
            stmt.execute("INSERT INTO label_key (id, name) VALUES (2, 'svc')");
            stmt.execute("INSERT INTO label_key (id, name) VALUES (3, 'region')");

            // Create label values
            stmt.execute("INSERT INTO label_value (id, value) VALUES (1, 'prod')");
            stmt.execute("INSERT INTO label_value (id, value) VALUES (2, 'staging')");
            stmt.execute("INSERT INTO label_value (id, value) VALUES (3, 'api')");
            stmt.execute("INSERT INTO label_value (id, value) VALUES (4, 'web')");
            stmt.execute("INSERT INTO label_value (id, value) VALUES (5, 'us-east')");
            stmt.execute("INSERT INTO label_value (id, value) VALUES (6, 'us-west')");

            // Create label sets: prod/api/us-east, prod/web/us-east, prod/api/us-west, staging/api/us-east
            String[] hashes = {
                "{env=prod,region=us-east,svc=api}",
                "{env=prod,region=us-east,svc=web}",
                "{env=prod,region=us-west,svc=api}",
                "{env=staging,region=us-east,svc=api}"
            };
            for (int i = 0; i < hashes.length; i++) {
                stmt.execute(String.format("INSERT INTO label_set (id, hash) VALUES (%d, '%s')",
                    i + 1, hashes[i]));
            }

            // Link labels to label sets
            // Label set 1: prod, api, us-east
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (1, 1, 1)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (1, 2, 3)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (1, 3, 5)");

            // Label set 2: prod, web, us-east
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (2, 1, 1)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (2, 2, 4)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (2, 3, 5)");

            // Label set 3: prod, api, us-west
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (3, 1, 1)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (3, 2, 3)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (3, 3, 6)");

            // Label set 4: staging, api, us-east
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (4, 1, 2)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (4, 2, 3)");
            stmt.execute("INSERT INTO label_set_membership (label_set_id, label_key_id, label_value_id) VALUES (4, 3, 5)");

            // Create metric instances
            for (int i = 1; i <= 4; i++) {
                stmt.execute(String.format(
                    "INSERT INTO metric_instance (id, sample_name_id, label_set_id) VALUES (%d, 1, %d)",
                    i, i));
            }

            // Insert sample values
            long baseTime = System.currentTimeMillis();
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) VALUES (1, %d, 100.0)",
                baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) VALUES (2, %d, 200.0)",
                baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) VALUES (3, %d, 150.0)",
                baseTime));
            stmt.execute(String.format(
                "INSERT INTO sample_value (metric_instance_id, timestamp_ms, value) VALUES (4, %d, 50.0)",
                baseTime));
        }
    }

    @Test
    void testGroupBySingleLabel() throws Exception {
        // sum(http_requests) by (env)
        // Should return: {env=prod}: 450, {env=staging}: 50
        String query = "sum(http_requests) by (env)";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<String> labels = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                while (rs.next()) {
                    labels.add(rs.getString("labels"));
                    values.add(rs.getDouble("value"));
                }

                assertEquals(2, labels.size(), "Should return 2 groups (prod and staging)");

                // Check that labels contain only env (not svc or region)
                for (String labelStr : labels) {
                    assertTrue(labelStr.contains("env="),
                        "Labels should contain env");
                    assertFalse(labelStr.contains("svc="),
                        "Labels should NOT contain svc (should be collapsed)");
                    assertFalse(labelStr.contains("region="),
                        "Labels should NOT contain region (should be collapsed)");
                }

                // Check sums
                boolean hasProd = false;
                boolean hasStaging = false;
                for (int i = 0; i < labels.size(); i++) {
                    if (labels.get(i).contains("env=prod")) {
                        hasProd = true;
                        assertEquals(450.0, values.get(i), 0.1,
                            "prod sum should be 100+200+150=450");
                    } else if (labels.get(i).contains("env=staging")) {
                        hasStaging = true;
                        assertEquals(50.0, values.get(i), 0.1,
                            "staging sum should be 50");
                    }
                }
                assertTrue(hasProd && hasStaging, "Should have both prod and staging groups");
            }
        }
    }

    @Test
    void testGroupByMultipleLabels() throws Exception {
        // sum(http_requests) by (env, svc)
        // Should return: {env=prod, svc=api}: 250, {env=prod, svc=web}: 200, {env=staging, svc=api}: 50
        String query = "sum(http_requests) by (env, svc)";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<String> labels = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                while (rs.next()) {
                    labels.add(rs.getString("labels"));
                    values.add(rs.getDouble("value"));
                }

                assertEquals(3, labels.size(),
                    "Should return 3 groups (prod/api, prod/web, staging/api)");

                // Check that labels contain only env and svc (not region)
                for (String labelStr : labels) {
                    assertTrue(labelStr.contains("env="),
                        "Labels should contain env");
                    assertTrue(labelStr.contains("svc="),
                        "Labels should contain svc");
                    assertFalse(labelStr.contains("region="),
                        "Labels should NOT contain region (should be collapsed)");
                }

                // Check sums
                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i);
                    double value = values.get(i);

                    if (label.contains("env=prod") && label.contains("svc=api")) {
                        assertEquals(250.0, value, 0.1,
                            "prod/api sum should be 100+150=250");
                    } else if (label.contains("env=prod") && label.contains("svc=web")) {
                        assertEquals(200.0, value, 0.1,
                            "prod/web sum should be 200");
                    } else if (label.contains("env=staging") && label.contains("svc=api")) {
                        assertEquals(50.0, value, 0.1,
                            "staging/api sum should be 50");
                    }
                }
            }
        }
    }

    @Test
    void testAvgWithGrouping() throws Exception {
        // avg(http_requests) by (svc)
        // Should return: {svc=api}: (100+150+50)/3=100, {svc=web}: 200
        String query = "avg(http_requests) by (svc)";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<String> labels = new ArrayList<>();
                List<Double> values = new ArrayList<>();

                while (rs.next()) {
                    labels.add(rs.getString("labels"));
                    values.add(rs.getDouble("value"));
                }

                assertEquals(2, labels.size(), "Should return 2 groups (api and web)");

                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i);
                    double value = values.get(i);

                    if (label.contains("svc=api")) {
                        assertEquals(100.0, value, 0.1,
                            "api average should be (100+150+50)/3=100");
                    } else if (label.contains("svc=web")) {
                        assertEquals(200.0, value, 0.1,
                            "web average should be 200");
                    }
                }
            }
        }
    }

    @Test
    void testCountWithGrouping() throws Exception {
        // count(http_requests) by (env)
        // Should return: {env=prod}: 3, {env=staging}: 1
        String query = "count(http_requests) by (env)";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int prodCount = 0;
                int stagingCount = 0;

                while (rs.next()) {
                    String label = rs.getString("labels");
                    int count = (int) rs.getDouble("value");

                    if (label.contains("env=prod")) {
                        prodCount = count;
                    } else if (label.contains("env=staging")) {
                        stagingCount = count;
                    }
                }

                assertEquals(3, prodCount, "prod should have 3 metrics");
                assertEquals(1, stagingCount, "staging should have 1 metric");
            }
        }
    }

    @Test
    void testMaxMinWithGrouping() throws Exception {
        // max(http_requests) by (region)
        // Should return: {region=us-east}: 200, {region=us-west}: 150
        String query = "max(http_requests) by (region)";
        SQLFragment fragment = parseAndTransform(query);

        try (PreparedStatement ps = conn.prepareStatement(fragment.getSql())) {
            for (int i = 0; i < fragment.getParameters().size(); i++) {
                ps.setObject(i + 1, fragment.getParameters().get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    String label = rs.getString("labels");
                    double value = rs.getDouble("value");
                    count++;

                    assertTrue(label.contains("region="),
                        "Labels should contain region");
                    assertFalse(label.contains("env="),
                        "Labels should NOT contain env");

                    if (label.contains("region=us-east")) {
                        assertEquals(200.0, value, 0.1,
                            "us-east max should be 200");
                    } else if (label.contains("region=us-west")) {
                        assertEquals(150.0, value, 0.1,
                            "us-west max should be 150");
                    }
                }

                assertEquals(2, count, "Should return 2 regions");
            }
        }
    }
}
