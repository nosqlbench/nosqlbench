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

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IncreaseCommandTest {

    @Test
    void testIncreaseLinearPattern() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            IncreaseCommand command = new IncreaseCommand();

            Map<String, Object> params = Map.of(
                "metric", "patterns_total",
                "window", "1h",
                "labels", Map.of("pattern", "linear")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 1 row for the linear pattern label set
            assertEquals(1, result.rowCount(), "Should have 1 result for linear pattern");

            Map<String, Object> row = result.rows().get(0);
            double increase = (double) row.get("increase");

            // Linear pattern: 120 snapshots * 10 increment = 1190 total increase
            assertEquals(1190.0, increase, 1.0, "Total increase should be ~1190");

            System.out.println("\n=== Increase Query: Linear Pattern ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testIncreaseExponentialPattern() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            IncreaseCommand command = new IncreaseCommand();

            Map<String, Object> params = Map.of(
                "metric", "patterns_total",
                "window", "1h",
                "labels", Map.of("pattern", "exponential")
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount());

            Map<String, Object> row = result.rows().get(0);
            double increase = (double) row.get("increase");

            // Exponential: 100 * (1.01^120) ≈ 230
            assertTrue(increase > 200, "Exponential increase should be > 200");
            assertTrue(increase < 300, "Exponential increase should be < 300");

            System.out.println("\n=== Increase Query: Exponential Pattern ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testIncreaseStepPattern() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.RATE_CALCULATIONS);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            IncreaseCommand command = new IncreaseCommand();

            Map<String, Object> params = Map.of(
                "metric", "patterns_total",
                "window", "1h",
                "labels", Map.of("pattern", "step")
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount());

            Map<String, Object> row = result.rows().get(0);
            double increase = (double) row.get("increase");

            // Step pattern: starts at 100, ends at 1200 (12 steps * 100)
            assertEquals(1100.0, increase, 1.0, "Step increase should be 1100");

            System.out.println("\n=== Increase Query: Step Pattern ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testIncreaseExamplesDatabase() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            IncreaseCommand command = new IncreaseCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "window", "1h",
                "labels", Map.of("method", "GET", "endpoint", "/api/users", "status", "200")
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount());

            Map<String, Object> row = result.rows().get(0);
            double increase = (double) row.get("increase");

            // Increase from first to last snapshot (values increment by 100 per snapshot, 4 intervals)
            assertTrue(increase > 0, "Should have positive increase");

            System.out.println("\n=== Increase Query: Examples DB ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testIncreaseMultipleLabels() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            IncreaseCommand command = new IncreaseCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "window", "1h",
                "labels", Map.of("status", "200")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 9 label sets (3 methods × 3 endpoints with status=200)
            assertEquals(9, result.rowCount(), "Should have 9 label sets with status=200");

            System.out.println("\n=== Increase Query: All Successful Requests ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testValidationMissingMetric() {
        IncreaseCommand command = new IncreaseCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("window", "5m")));
    }

    @Test
    void testValidationMissingWindow() {
        IncreaseCommand command = new IncreaseCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test")));
    }

    @Test
    void testValidationBothWindowAndLast() {
        IncreaseCommand command = new IncreaseCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "window", "5m", "last", "10m")));
    }
}
