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
@Tag("unit")
class InstantCommandTest {

    @Test
    void testInstantQuerySimpleCounter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            InstantCommand command = new InstantCommand();

            Map<String, Object> params = Map.of("metric", "activity_ops_total");
            QueryResult result = command.execute(conn, params);

            // Should return 2 rows (read and write activities)
            assertEquals(2, result.rowCount(), "Expected 2 label sets");

            // Check columns
            assertEquals(3, result.columns().size());
            assertTrue(result.columns().contains("timestamp"));
            assertTrue(result.columns().contains("value"));
            assertTrue(result.columns().contains("labels"));

            // Verify we got both read and write
            boolean hasRead = false;
            boolean hasWrite = false;
            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                double value = (double) row.get("value");

                if (labels.contains("activity=read")) {
                    hasRead = true;
                    assertEquals(90.0, value, 0.01, "Expected final read value to be 90");
                } else if (labels.contains("activity=write")) {
                    hasWrite = true;
                    assertEquals(135.0, value, 0.01, "Expected final write value to be 135");
                }
            }

            assertTrue(hasRead, "Should have read activity");
            assertTrue(hasWrite, "Should have write activity");

            // Print formatted output for visual verification
            System.out.println("\n=== Instant Query Result ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testInstantQueryWithLabelFilter() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.SIMPLE_COUNTER);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            InstantCommand command = new InstantCommand();

            Map<String, Object> params = Map.of(
                "metric", "activity_ops_total",
                "labels", Map.of("activity", "read")
            );
            QueryResult result = command.execute(conn, params);

            // Should return only 1 row (read activity)
            assertEquals(1, result.rowCount(), "Expected 1 label set with activity=read filter");

            Map<String, Object> row = result.rows().get(0);
            String labels = (String) row.get("labels");
            double value = (double) row.get("value");

            assertTrue(labels.contains("activity=read"));
            assertEquals(90.0, value, 0.01);
        }
    }

    @Test
    void testInstantQueryMultiDimensional() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            InstantCommand command = new InstantCommand();

            Map<String, Object> params = Map.of("metric", "requests_total");
            QueryResult result = command.execute(conn, params);

            // Should return 24 rows (24 unique label combinations)
            assertEquals(24, result.rowCount(), "Expected 24 unique label sets");

            System.out.println("\n=== Multi-Dimensional Instant Query ===");
            System.out.println(new TableFormatter().format(result));
        }
    }

    @Test
    void testInstantQueryWithMultipleLabelFilters() throws Exception {
        Path dbPath = TestDatabaseLoader.getDatabase(TestDatabaseLoader.TestDatabase.MULTI_DIMENSIONAL);

        try (Connection conn = MetricsDatabaseReader.connect(dbPath)) {
            InstantCommand command = new InstantCommand();

            Map<String, Object> params = Map.of(
                "metric", "requests_total",
                "labels", Map.of(
                    "activity", "write",
                    "region", "us-east",
                    "env", "prod"
                )
            );
            QueryResult result = command.execute(conn, params);

            // Should return only rows matching all 3 label filters
            // With 3 hosts, should get 3 results
            assertEquals(3, result.rowCount(),
                "Expected 3 label sets (one per host) matching activity=write, region=us-east, env=prod");

            // Verify all results have the correct labels
            for (Map<String, Object> row : result.rows()) {
                String labels = (String) row.get("labels");
                assertTrue(labels.contains("activity=write"));
                assertTrue(labels.contains("region=us-east"));
                assertTrue(labels.contains("env=prod"));
            }
        }
    }

    @Test
    void testValidationMissingMetric() {
        InstantCommand command = new InstantCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of()));
    }

    @Test
    void testValidationEmptyMetric() {
        InstantCommand command = new InstantCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "")));
    }

    @Test
    void testValidationInvalidLabelsType() {
        InstantCommand command = new InstantCommand();
        assertThrows(InvalidQueryException.class, () ->
            command.validate(Map.of("metric", "test", "labels", "not a map")));
    }
}
