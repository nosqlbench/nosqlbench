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
import io.nosqlbench.nb.mql.query.InvalidQueryException;
import io.nosqlbench.nb.mql.query.QueryResult;
import io.nosqlbench.nb.mql.schema.MetricsDatabaseReader;
import io.nosqlbench.nb.mql.testdata.TestDatabaseLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
class RangeCommandTest {

    @Test
    void testRangeQueryAllData() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RangeCommand command = new RangeCommand();

            // Query all data using a very large window
            Map<String, Object> params = Map.of(
                "metric", "activity_ops_total",
                "window", "1h"
            );
            QueryResult result = command.execute(conn, params);

            // Should return all 20 samples (10 snapshots × 2 label sets)
            assertEquals(20, result.rowCount(), "Should return all samples");

            // Verify columns
            assertEquals(3, result.columns().size());
            assertTrue(result.columns().contains("timestamp"));
            assertTrue(result.columns().contains("value"));
            assertTrue(result.columns().contains("labels"));

            System.out.println("\n=== Range Query All Data ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testRangeQueryWithLabelFilter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RangeCommand command = new RangeCommand();

            Map<String, Object> params = Map.of(
                "metric", "activity_ops_total",
                "window", "1h",
                "labels", Map.of("activity", "read")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 10 samples (just read activity)
            assertEquals(10, result.rowCount(), "Should return only read activity samples");

            // Verify all have activity=read
            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                assertTrue(labels.contains("activity=read"), "All results should be activity=read");
                assertFalse(labels.contains("activity=write"), "Should not contain write");
            }

            System.out.println("\n=== Range Query Filtered (read only) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testRangeQueryVerifyTimeSeries() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RangeCommand command = new RangeCommand();

            Map<String, Object> params = Map.of(
                "metric", "activity_ops_total",
                "window", "1h",
                "labels", Map.of("activity", "read")
            );
            QueryResult result = command.execute(conn, params);

            // Verify values form expected linear sequence: 0, 10, 20, ..., 90
            List<Double> values = result.rows().stream()
                .map(row -> (Double) row.get("value"))
                .sorted()
                .toList();

            assertEquals(10, values.size());
            for (int i = 0; i < 10; i++) {
                assertEquals(i * 10.0, values.get(i), 0.01,
                    "Value at index " + i + " should be " + (i * 10));
            }
        }
    }

    @Test
    void testRangeQueryExamplesDatabase() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RangeCommand command = new RangeCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "window", "1h",  // Get all 5 snapshots
                "labels", Map.of("method", "GET", "endpoint", "/api/users", "status", "200")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 5 snapshots
            assertEquals(5, result.rowCount(), "Should return all 5 snapshots");

            // Verify values are monotonically increasing: 600, 700, 800, 900, 1000
            List<Double> values = result.rows().stream()
                .map(row -> (Double) row.get("value"))
                .toList();

            assertEquals(600.0, values.get(0), 0.01);
            assertEquals(700.0, values.get(1), 0.01);
            assertEquals(800.0, values.get(2), 0.01);
            assertEquals(900.0, values.get(3), 0.01);
            assertEquals(1000.0, values.get(4), 0.01);

            System.out.println("\n=== Range Query Examples DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testRangeQueryAlternativeLastSyntax() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            RangeCommand command = new RangeCommand();

            // Test --last as alternative to --window
            Map<String, Object> params = Map.of(
                "metric", "activity_ops_total",
                "last", "1h"
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(20, result.rowCount(), "Should return all samples with --last");
        }
    }

    @Test
    void testValidationMissingMetric() {
        RangeCommand command = new RangeCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("window", "5m")));
    }

    @Test
    void testValidationMissingTimeRange() {
        RangeCommand command = new RangeCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test")));
    }

    @Test
    void testValidationMultipleTimeRangeMethods() {
        RangeCommand command = new RangeCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "window", "5m", "last", "10m")));
    }
}
