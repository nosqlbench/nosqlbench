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
class TopKCommandTest {

    @Test
    void testTop3() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            TopKCommand command = new TopKCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "n", 3
            );
            QueryResult result = command.execute(conn, params);

            // Should return exactly 3 rows
            assertEquals(3, result.rowCount(), "Should return top 3");

            // Verify columns
            assertEquals(2, result.columns().size());
            assertTrue(result.columns().contains("value"));
            assertTrue(result.columns().contains("labels"));

            // First row should have highest value (11010)
            double firstValue = (double) result.rows().get(0).get("value");
            assertEquals(11010.0, firstValue, 0.1, "Top value should be 11010");

            System.out.println("\n=== TopK Query: Top 3 ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testTop1() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            TopKCommand command = new TopKCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "n", 1
            );
            QueryResult result = command.execute(conn, params);

            assertEquals(1, result.rowCount(), "Should return top 1");

            double value = (double) result.rows().get(0).get("value");
            assertEquals(11010.0, value, 0.1);

            System.out.println("\n=== TopK Query: Top 1 ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testTop5WithLabelFilter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase("examples.db");

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            TopKCommand command = new TopKCommand();

            Map<String, Object> params = Map.of(
                "metric", "api_requests_total",
                "n", 5,
                "labels", Map.of("status", "200")
            );
            QueryResult result = command.execute(conn, params);

            // Should return 5 rows (limited by n=5, all have status=200 - there are 9 total)
            assertEquals(5, result.rowCount(), "Should return 5 (limited by n)");

            // All should have status=200
            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                assertTrue(labels.contains("status=200"));
            }

            System.out.println("\n=== TopK Query: Top 5 (filtered by status=200) ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testTop10MultiDimensional() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            TopKCommand command = new TopKCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "n", 10
            );
            QueryResult result = command.execute(conn, params);

            // Should return exactly 10 rows
            assertEquals(10, result.rowCount(), "Should return top 10");

            // Verify values are in descending order
            double previousValue = Double.MAX_VALUE;
            for (Map<String, Object> row : result.rows()) {
                double value = (double) row.get("value");
                assertTrue(value <= previousValue, "Values should be in descending order");
                previousValue = value;
            }

            System.out.println("\n=== TopK Query: Top 10 from Multi-Dimensional ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testValidationMissingMetric() {
        TopKCommand command = new TopKCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("n", 5)));
    }

    @Test
    void testValidationMissingN() {
        TopKCommand command = new TopKCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test")));
    }

    @Test
    void testValidationNegativeN() {
        TopKCommand command = new TopKCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "n", -1)));
    }

    @Test
    void testValidationZeroN() {
        TopKCommand command = new TopKCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "n", 0)));
    }

    @Test
    void testValidationValidN() throws Exception {
        TopKCommand command = new TopKCommand();
        command.validate(Map.of("metric", "test", "n", 1));
        command.validate(Map.of("metric", "test", "n", 100));
    }
}
