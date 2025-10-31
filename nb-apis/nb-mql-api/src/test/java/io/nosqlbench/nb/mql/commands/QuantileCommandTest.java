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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
class QuantileCommandTest {

    @Test
    void testQuantileP95AllOperations() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.LATENCY_TIMERS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            QuantileCommand command = new QuantileCommand();

            Map<String, Object> params = Map.of(
                "metric", "operation_latency",
                "quantile", 0.95
            );
            QueryResult result = command.execute(conn, params);

            // Should have p95 values for all operations across all snapshots
            assertTrue(result.rowCount() > 0, "Should have quantile results");

            // Verify we have the expected columns
            assertEquals(4, result.columns().size());
            assertTrue(result.columns().contains("timestamp"));
            assertTrue(result.columns().contains("quantile"));
            assertTrue(result.columns().contains("quantile_value"));
            assertTrue(result.columns().contains("labels"));

            // All quantiles should be 0.95
            for (Map<String, Object> row : result.rows()) {
                double q = (double) row.get("quantile");
                assertEquals(0.95, q, 0.001);
            }

            System.out.println("\n=== Quantile Query: p95 All Operations ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testQuantileP99SpecificOperation() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.LATENCY_TIMERS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            QuantileCommand command = new QuantileCommand();

            Map<String, Object> params = Map.of(
                "metric", "operation_latency",
                "quantile", 0.99,
                "labels", Map.of("operation", "select")
            );
            QueryResult result = command.execute(conn, params);

            assertTrue(result.rowCount() > 0, "Should have p99 for select operation");

            // All should be for select operation
            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                assertTrue(labels.contains("operation=select"));
            }

            System.out.println("\n=== Quantile Query: p99 Select Operation ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testQuantileP50Median() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.LATENCY_TIMERS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            QuantileCommand command = new QuantileCommand();

            Map<String, Object> params = Map.of(
                "metric", "operation_latency",
                "quantile", 0.50
            );
            QueryResult result = command.execute(conn, params);

            assertTrue(result.rowCount() > 0, "Should have median values");

            System.out.println("\n=== Quantile Query: p50 (Median) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testQuantileMultipleOperationsP95() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.LATENCY_TIMERS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            QuantileCommand command = new QuantileCommand();

            Map<String, Object> params = Map.of(
                "metric", "operation_latency",
                "quantile", 0.95
            );
            QueryResult result = command.execute(conn, params);

            // Should have results for all 4 operations (select, insert, update, delete)
            // across 6 snapshots = 24 total
            assertEquals(24, result.rowCount(), "Should have 24 quantile values (4 ops * 6 snapshots)");
        }
    }

    @Test
    void testValidationMissingMetric() {
        QuantileCommand command = new QuantileCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("quantile", 0.95)));
    }

    @Test
    void testValidationMissingQuantile() {
        QuantileCommand command = new QuantileCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test")));
    }

    @Test
    void testValidationQuantileOutOfRange() {
        QuantileCommand command = new QuantileCommand();

        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "quantile", -0.1)));

        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "quantile", 1.5)));
    }

    @Test
    void testValidationValidQuantiles() throws Exception {
        QuantileCommand command = new QuantileCommand();

        command.validate(Map.of("metric", "test", "quantile", 0.0));
        command.validate(Map.of("metric", "test", "quantile", 0.5));
        command.validate(Map.of("metric", "test", "quantile", 1.0));
    }
}
