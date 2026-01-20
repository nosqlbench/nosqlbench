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

package io.nosqlbench.nb.mql.commands;

import io.nosqlbench.nb.mql.format.TableFormatter;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
@Tag("unit")
class MetricsCommandTest {

    @Test
    void testMetricsGroupByName() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetricsCommand command = new MetricsCommand();

            QueryResult result = command.execute(conn, Map.of("group-by", "name"));

            assertTrue(result.rowCount() > 0, "Should have metrics");

            System.out.println("\n=== Metrics: Group by Name ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testMetricsGroupByLabelSet() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetricsCommand command = new MetricsCommand();

            QueryResult result = command.execute(conn, Map.of("group-by", "labelset"));

            assertTrue(result.rowCount() > 0, "Should have label sets");

            System.out.println("\n=== Metrics: Group by Label Set ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testMetricsDefaultGrouping() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetricsCommand command = new MetricsCommand();

            QueryResult result = command.execute(conn, Map.of());

            assertTrue(result.rowCount() > 0, "Should have metrics");
        }
    }

    @Test
    void testMetricsWithKeepLabelsWildcard() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("simple_counter.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetricsCommand command = new MetricsCommand();

            // With '*', no labels should be elided
            QueryResult result = command.execute(conn, Map.of("keep-labels", "*"));

            assertTrue(result.rowCount() > 0, "Should have metrics");

            // Check that there's no COMMON LABELS row
            long commonLabelsRows = result.rows().stream()
                .filter(row -> "COMMON LABELS".equals(row.get("group")))
                .count();
            assertEquals(0, commonLabelsRows, "Should have no common labels row when using '*'");

            System.out.println("\n=== Metrics: Keep All Labels (*) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testMetricsWithCustomKeepLabels() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("simple_counter.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            MetricsCommand command = new MetricsCommand();

            // Keep nothing (empty string), so host should be elided
            QueryResult result = command.execute(conn, Map.of("keep-labels", ""));

            assertTrue(result.rowCount() > 0, "Should have metrics");

            // Check that there IS a COMMON LABELS row (host should be elided)
            long commonLabelsRows = result.rows().stream()
                .filter(row -> "COMMON LABELS".equals(row.get("group")))
                .count();
            assertTrue(commonLabelsRows > 0, "Should have common labels row when not keeping any labels");

            System.out.println("\n=== Metrics: Keep No Labels (empty) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }
}
