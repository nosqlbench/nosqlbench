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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("mql")
@Tag("unit")
class AggregateCommandTest {

    @Test
    void testSumNoGrouping() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "function", "sum"
            );
            QueryResult result = command.execute(conn, params);

            // Should return 1 row with total sum
            assertEquals(1, result.rowCount());
            assertEquals(1, result.columns().size());
            assertEquals("sum", result.columns().get(0));

            System.out.println("\n=== Aggregate: Sum (No Grouping) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSumByActivity() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "function", "sum",
                "by", List.of("activity")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 2 rows (read and write)
            assertEquals(2, result.rowCount(), "Should have 2 activities");
            assertEquals(2, result.columns().size());
            assertTrue(result.columns().contains("activity"));
            assertTrue(result.columns().contains("sum"));

            System.out.println("\n=== Aggregate: Sum by Activity ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testAvgByRegion() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "function", "avg",
                "by", List.of("region")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 2 rows (us-east and us-west)
            assertEquals(2, result.rowCount());
            assertTrue(result.columns().contains("region"));
            assertTrue(result.columns().contains("avg"));

            System.out.println("\n=== Aggregate: Avg by Region ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testMaxByHost() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "function", "max",
                "by", List.of("host")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 3 rows (server1, server2, server3)
            assertEquals(3, result.rowCount(), "Should have 3 hosts");

            System.out.println("\n=== Aggregate: Max by Host ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testCountByMultipleLabels() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "function", "count",
                "by", List.of("activity", "region")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 4 rows (2 activities * 2 regions)
            assertEquals(4, result.rowCount(), "Should have 4 combinations");
            assertEquals(3, result.columns().size());
            assertTrue(result.columns().contains("activity"));
            assertTrue(result.columns().contains("region"));
            assertTrue(result.columns().contains("count"));

            System.out.println("\n=== Aggregate: Count by Activity and Region ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSumWithLabelFilter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "function", "sum",
                "labels", Map.of("env", "prod")
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount());

            System.out.println("\n=== Aggregate: Sum (env=prod only) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testSumExamplesDatabase() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            AggregateCommand command = new AggregateCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "function", "sum",
                "by", List.of("status")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 3 rows (200, 404, 500)
            assertEquals(3, result.rowCount());

            System.out.println("\n=== Aggregate: Sum by Status Code ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testValidationMissingMetric() {
        AggregateCommand command = new AggregateCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("function", "sum")));
    }

    @Test
    void testValidationMissingFunction() {
        AggregateCommand command = new AggregateCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test")));
    }

    @Test
    void testValidationInvalidFunction() {
        AggregateCommand command = new AggregateCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "function", "invalid")));
    }

    @Test
    void testValidationValidFunctions() throws Exception {
        AggregateCommand command = new AggregateCommand();

        command.validate(Map.of("metric", "test", "function", "sum"));
        command.validate(Map.of("metric", "test", "function", "avg"));
        command.validate(Map.of("metric", "test", "function", "max"));
        command.validate(Map.of("metric", "test", "function", "min"));
        command.validate(Map.of("metric", "test", "function", "count"));
    }
}
